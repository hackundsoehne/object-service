-- MySQL dump 10.13  Distrib 5.6.28, for debian-linux-gnu (x86_64)
--
-- Host: localhost    Database: crowdcontrol
-- ------------------------------------------------------
-- Server version	5.7.11

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
-- Dumping data for table `Algorithm_Task_Chooser`
--

LOCK TABLES `Algorithm_Task_Chooser` WRITE;
/*!40000 ALTER TABLE `Algorithm_Task_Chooser` DISABLE KEYS */;
INSERT INTO `Algorithm_Task_Chooser` VALUES ('anti_spoof','This AntiSpoof algorithm divides the runtime of the experiment into 3 phases. In the first phase the workers are only allowed to work on creative tasks. Then in the second phase, the worker can work on both the creative and the rating task. The last phase consists only of the assignment to rate. You can set the duration of the first phase either as an absolute number or as an percentage of the total needed answers of the experiment. The duration of the second phase is defined as the difference of the first phase and the total number of answers needed. The third phase will then run until all the remaining ratings got collected.');
/*!40000 ALTER TABLE `Algorithm_Task_Chooser` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `Algorithm_Task_Chooser_Param`
--

LOCK TABLES `Algorithm_Task_Chooser_Param` WRITE;
/*!40000 ALTER TABLE `Algorithm_Task_Chooser_Param` DISABLE KEYS */;
INSERT INTO `Algorithm_Task_Chooser_Param` VALUES (1,'Set the amount of answers in the first phase. To set the absolute number just type the number followed by ab, example \"150ab\" for 150 answers. To set the parameter to a percentage type first the percentage followed by pc, example \"30pc\" for 30 percent.','^(([0-9]+ab)|((100|[0-9]?[0-9])pc))$','anti_spoof','1');
/*!40000 ALTER TABLE `Algorithm_Task_Chooser_Param` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2016-03-17 10:52:16
