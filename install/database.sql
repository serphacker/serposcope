DROP TABLE IF EXISTS `option`;
DROP TABLE IF EXISTS `version`;
DROP TABLE IF EXISTS `run`;
DROP TABLE IF EXISTS `proxy`;
DROP TABLE IF EXISTS `event`;
DROP TABLE IF EXISTS `rank`;
DROP TABLE IF EXISTS `check`;
DROP TABLE IF EXISTS `keyword`;
DROP TABLE IF EXISTS `target`;
DROP TABLE IF EXISTS `group`;

CREATE TABLE `group` (
  `idGroup` int(11) NOT NULL AUTO_INCREMENT,
  `name` text,
  `module` text,
  `options` text,
  `position` int(11) DEFAULT NULL,
  PRIMARY KEY (`idGroup`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `keyword` (
  `idKeyword` int(11) NOT NULL AUTO_INCREMENT,
  `idGroup` int(11) DEFAULT NULL,
  `name` text,
  PRIMARY KEY (`idKeyword`),
  KEY `fk_keyword_idGroup` (`idGroup`),
  CONSTRAINT `fk_keyword_idGroup` FOREIGN KEY (`idGroup`) REFERENCES `group` (`idGroup`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `target` (
  `idTarget` int(11) NOT NULL AUTO_INCREMENT,
  `idGroup` int(11) DEFAULT NULL,
  `name` text,
  `info` text,
  PRIMARY KEY (`idTarget`),
  KEY `fk_target_idGroup` (`idGroup`),
  CONSTRAINT `fk_target_idGroup` FOREIGN KEY (`idGroup`) REFERENCES `group` (`idGroup`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `check` (
  `idCheck` int(11) NOT NULL AUTO_INCREMENT,
  `idGroup` int(11) DEFAULT NULL,
  `date` datetime DEFAULT NULL,
  PRIMARY KEY (`idCheck`),
  KEY `fk_check_idGroup` (`idGroup`),
  CONSTRAINT `fk_check_idGroup` FOREIGN KEY (`idGroup`) REFERENCES `group` (`idGroup`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `rank` (
  `idCheck` int(11) NOT NULL DEFAULT '0',
  `idTarget` int(11) NOT NULL DEFAULT '0',
  `idKeyword` int(11) NOT NULL DEFAULT '0',
  `position` int(11) DEFAULT NULL,
  `url` text,
  PRIMARY KEY (`idCheck`,`idTarget`,`idKeyword`),
  KEY `fk_rank_idTarget` (`idTarget`),
  KEY `fk_rank_idKeyword` (`idKeyword`),
  CONSTRAINT `fk_rank_idCheck` FOREIGN KEY (`idCheck`) REFERENCES `check` (`idCheck`) ON DELETE CASCADE,
  CONSTRAINT `fk_rank_idKeyword` FOREIGN KEY (`idKeyword`) REFERENCES `keyword` (`idKeyword`) ON DELETE CASCADE,
  CONSTRAINT `fk_rank_idTarget` FOREIGN KEY (`idTarget`) REFERENCES `target` (`idTarget`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `event` (
  `idEvent` int(11) NOT NULL AUTO_INCREMENT,
  `idTarget` int(11) DEFAULT NULL,
  `date` date DEFAULT NULL,
  `event` text,
  PRIMARY KEY (`idEvent`),
  KEY `fk_event_idTarget` (`idTarget`),
  CONSTRAINT `fk_event_idTarget` FOREIGN KEY (`idTarget`) REFERENCES `target` (`idTarget`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `option` (
  `name` varchar(255) NOT NULL,
  `value` text,
  PRIMARY KEY (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `proxy` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `type` text,
  `ip` text,
  `port` text,
  `user` text,
  `password` text,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `run` (
  `idRun` int(11) NOT NULL AUTO_INCREMENT,
  `dateStart` datetime NOT NULL,
  `dateStop` datetime DEFAULT NULL,
  `logs` mediumtext,
  `pid` int(11) NOT NULL,
  `haveError` tinyint(4) DEFAULT '0',
  PRIMARY KEY (`idRun`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `version` (
  `version` TEXT
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
