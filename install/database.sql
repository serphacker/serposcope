/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `check`
--

DROP TABLE IF EXISTS `check`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `check` (
  `idCheck` int(11) NOT NULL AUTO_INCREMENT,
  `idGroup` int(11) DEFAULT NULL,
  `date` datetime DEFAULT NULL,
  PRIMARY KEY (`idCheck`),
  KEY `fk_check_idGroup` (`idGroup`),
  CONSTRAINT `fk_check_idGroup` FOREIGN KEY (`idGroup`) REFERENCES `group` (`idGroup`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `event`
--

DROP TABLE IF EXISTS `event`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `event` (
  `idEvent` int(11) NOT NULL AUTO_INCREMENT,
  `idTarget` int(11) DEFAULT NULL,
  `date` date DEFAULT NULL,
  `event` text,
  PRIMARY KEY (`idEvent`),
  KEY `fk_event_idTarget` (`idTarget`),
  CONSTRAINT `fk_event_idTarget` FOREIGN KEY (`idTarget`) REFERENCES `target` (`idTarget`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `group`
--

DROP TABLE IF EXISTS `group`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `group` (
  `idGroup` int(11) NOT NULL AUTO_INCREMENT,
  `name` text,
  `module` text,
  `options` text,
  `position` int(11) DEFAULT NULL,
  PRIMARY KEY (`idGroup`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `keyword`
--

DROP TABLE IF EXISTS `keyword`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `keyword` (
  `idKeyword` int(11) NOT NULL AUTO_INCREMENT,
  `idGroup` int(11) DEFAULT NULL,
  `name` text,
  PRIMARY KEY (`idKeyword`),
  KEY `fk_keyword_idGroup` (`idGroup`),
  CONSTRAINT `fk_keyword_idGroup` FOREIGN KEY (`idGroup`) REFERENCES `group` (`idGroup`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `option`
--

DROP TABLE IF EXISTS `option`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `option` (
  `name` varchar(255) NOT NULL,
  `value` text,
  PRIMARY KEY (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `proxy`
--

DROP TABLE IF EXISTS `proxy`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `proxy` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `type` text,
  `ip` text,
  `port` text,
  `user` text,
  `password` text,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `rank`
--

DROP TABLE IF EXISTS `rank`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `run`
--

DROP TABLE IF EXISTS `run`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `run` (
  `idRun` int(11) NOT NULL AUTO_INCREMENT,
  `dateStart` datetime NOT NULL,
  `dateStop` datetime DEFAULT NULL,
  `logs` mediumtext,
  `pid` int(11) NOT NULL,
  `haveError` tinyint(4) DEFAULT '0',
  PRIMARY KEY (`idRun`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `target`
--

DROP TABLE IF EXISTS `target`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `target` (
  `idTarget` int(11) NOT NULL AUTO_INCREMENT,
  `idGroup` int(11) DEFAULT NULL,
  `name` text,
  `info` text,
  PRIMARY KEY (`idTarget`),
  KEY `fk_target_idGroup` (`idGroup`),
  CONSTRAINT `fk_target_idGroup` FOREIGN KEY (`idGroup`) REFERENCES `group` (`idGroup`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2013-03-18 17:37:44
