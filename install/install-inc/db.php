<?php

if(!defined('INCLUDE_OK')){
    die();
}

function drop_tables($prefix){
    @mysql_query("DROP TABLE IF EXISTS `".$prefix."option`;");
    @mysql_query("DROP TABLE IF EXISTS `".$prefix."version`;");
    @mysql_query("DROP TABLE IF EXISTS `".$prefix."proxy`;");
    @mysql_query("DROP TABLE IF EXISTS `".$prefix."event`;");
    @mysql_query("DROP TABLE IF EXISTS `".$prefix."rank`;");
    @mysql_query("DROP TABLE IF EXISTS `".$prefix."check`;");
    @mysql_query("DROP TABLE IF EXISTS `".$prefix."keyword`;");
    @mysql_query("DROP TABLE IF EXISTS `".$prefix."target`;");
    @mysql_query("DROP TABLE IF EXISTS `".$prefix."run`;");
    @mysql_query("DROP TABLE IF EXISTS `".$prefix."group`;");
    @mysql_query("DROP TABLE IF EXISTS `".$prefix."user`;");
}

function create_tables($prefix){

// clear the last mysql_error()
return mysql_query("SELECT 1 FROM dual") &&
mysql_query("
CREATE TABLE `".$prefix."group` (
    `idGroup` int(11) NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `name` text,
    `module` text,
    `options` text,
    `position` int(11) DEFAULT NULL,
    `user` text
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
") &&

mysql_query("
CREATE TABLE `".$prefix."keyword` (
    `idKeyword` int(11) NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `idGroup` int(11) DEFAULT NULL,
    `name` text,
    CONSTRAINT `".$prefix."fk_keyword_idGroup` FOREIGN KEY (`idGroup`) REFERENCES `".$prefix."group` (`idGroup`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
") &&

mysql_query("
CREATE TABLE `".$prefix."target` (
    `idTarget` int(11) NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `idGroup` int(11) DEFAULT NULL,
    `name` text,
    `info` text,
    CONSTRAINT `".$prefix."fk_target_idGroup` FOREIGN KEY (`idGroup`) REFERENCES `".$prefix."group` (`idGroup`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
") &&

mysql_query("
CREATE TABLE `".$prefix."run` (
    `idRun` int(11) NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `dateStart` datetime NOT NULL,
    `dateStop` datetime DEFAULT NULL,
    `logs` mediumtext,
    `pid` int(11) NOT NULL,
    `haveError` tinyint(4) DEFAULT '0',
    `id` text
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
") &&

mysql_query("
CREATE TABLE `".$prefix."check` (
    `idCheck` int(11) NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `idGroup` int(11) DEFAULT NULL,
    `idRun` int(11) DEFAULT NULL,
    `date` datetime DEFAULT NULL,
    CONSTRAINT `".$prefix."fk_check_idGroup` FOREIGN KEY (`idGroup`) REFERENCES `".$prefix."group` (`idGroup`) ON DELETE CASCADE,
    CONSTRAINT `".$prefix."fk_check_idRun` FOREIGN KEY (`idRun`) REFERENCES `".$prefix."run` (`idRun`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
") &&

mysql_query("
CREATE TABLE `".$prefix."rank` (
    `idCheck` int(11) NOT NULL DEFAULT '0',
    `idTarget` int(11) NOT NULL DEFAULT '0',
    `idKeyword` int(11) NOT NULL DEFAULT '0',
    `position` int(11) DEFAULT NULL,
    `url` text,
    PRIMARY KEY (`idCheck`,`idTarget`,`idKeyword`),
    CONSTRAINT `".$prefix."fk_rank_idCheck` FOREIGN KEY (`idCheck`) REFERENCES `".$prefix."check` (`idCheck`) ON DELETE CASCADE,
    CONSTRAINT `".$prefix."fk_rank_idKeyword` FOREIGN KEY (`idKeyword`) REFERENCES `".$prefix."keyword` (`idKeyword`) ON DELETE CASCADE,
    CONSTRAINT `".$prefix."fk_rank_idTarget` FOREIGN KEY (`idTarget`) REFERENCES `".$prefix."target` (`idTarget`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
") &&

mysql_query("
CREATE TABLE `".$prefix."event` (
    `idEvent` int(11) NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `idTarget` int(11) DEFAULT NULL,
    `date` date DEFAULT NULL,
    `event` text,
    CONSTRAINT `".$prefix."fk_event_idTarget` FOREIGN KEY (`idTarget`) REFERENCES `".$prefix."target` (`idTarget`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
") &&

mysql_query("
CREATE TABLE `".$prefix."option` (
    `name` varchar(255) NOT NULL PRIMARY KEY,
    `value` text
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
") &&

mysql_query("
CREATE TABLE `".$prefix."proxy` (
    `id` int(11) NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `type` text,
    `ip` text,
    `port` text,
    `user` text,
    `password` text
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
") &&

mysql_query("
CREATE TABLE `".$prefix."version` (
    `version` TEXT
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
") &&

mysql_query("
CREATE TABLE `".$prefix."user` (
    `name` TEXT,
    `pass` TEXT,
    `acces` TEXT,
    `buttons` TEXT,
    `role` TEXT,
    `id` int(11) NOT NULL AUTO_INCREMENT PRIMARY KEY
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
") &&

mysql_query("INSERT INTO `".$prefix."version` VALUE ('".SQL_VERSION."')");
}

function upgrade_tables($prefix,$oldversion){
    
    if($oldversion == null){
        $oldversion=1;
    }
    
    // < 2
    if($oldversion < 2){
        
        // the version table may not exists
        $q="CREATE TABLE `".$prefix."version` (
                `version` TEXT
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8;";
        @mysql_query($q);
        
        // doesn't have foreign keys for check <-> run
        $q="ALTER TABLE `".$prefix."check` ADD COLUMN
            `idRun` int(11) DEFAULT NULL
            AFTER idGroup";
        if(!mysql_query($q)){
            return $q;
        }

        // add constraint
        $q="ALTER TABLE `".$prefix."check` ADD CONSTRAINT
            `".$prefix."fk_check_idRun` FOREIGN KEY (`idRun`) REFERENCES `".$prefix."run` (`idRun`) ON DELETE CASCADE";
        if(!mysql_query($q)){
            return $q;
        }
        
    }
    
    // finally set the version
    $q="DELETE FROM `".$prefix."version`";
    if(!mysql_query($q)){
        return $q;
    }
    
    $q="INSERT INTO `".$prefix."version` VALUES('".SQL_VERSION."')";
    if(!mysql_query($q)){
        return $q;
    }
    return true;
}

?>