CREATE DATABASE  IF NOT EXISTS `androidchat` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;
USE `androidchat`;
-- MySQL dump 10.13  Distrib 8.0.41, for Win64 (x86_64)
--
-- Host: localhost    Database: androidchat
-- ------------------------------------------------------
-- Server version	8.0.41

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `contact`
--

DROP TABLE IF EXISTS `contact`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `contact` (
  `contact_id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '当前用户ID',
  `peer_id` bigint NOT NULL COMMENT '联系人用户ID',
  `remark_name` varchar(50) DEFAULT NULL COMMENT '备注名',
  PRIMARY KEY (`contact_id`),
  UNIQUE KEY `uk_user_friend` (`user_id`,`peer_id`),
  KEY `idx_user_search` (`user_id`,`remark_name`),
  KEY `idx_user_sort` (`user_id`),
  KEY `fk_contact_peer` (`peer_id`),
  CONSTRAINT `fk_contact_peer` FOREIGN KEY (`peer_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_contact_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='通讯录联系人表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `contact`
--

LOCK TABLES `contact` WRITE;
/*!40000 ALTER TABLE `contact` DISABLE KEYS */;
INSERT INTO `contact` VALUES (1,1,2,'王2'),(3,2,3,'王三金'),(4,2,1,'王大金'),(9,1,3,'wang3'),(10,3,1,'wang'),(13,10,2,'wang2'),(14,2,10,'xinye');
/*!40000 ALTER TABLE `contact` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `conversation`
--

DROP TABLE IF EXISTS `conversation`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `conversation` (
  `conv_id` bigint NOT NULL AUTO_INCREMENT,
  `a_id` bigint DEFAULT NULL,
  `b_id` bigint DEFAULT NULL,
  `group_id` bigint DEFAULT NULL,
  `is_group` tinyint(1) DEFAULT NULL,
  `last_message` text,
  `timestamp` bigint DEFAULT NULL,
  PRIMARY KEY (`conv_id`),
  UNIQUE KEY `unique_pair` (`a_id`,`b_id`),
  KEY `user_id_idx` (`a_id`,`b_id`),
  KEY `fk_conversation_b` (`b_id`),
  CONSTRAINT `fk_conversation_a` FOREIGN KEY (`a_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_conversation_b` FOREIGN KEY (`b_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `conversation`
--

LOCK TABLES `conversation` WRITE;
/*!40000 ALTER TABLE `conversation` DISABLE KEYS */;
INSERT INTO `conversation` VALUES (1,1,2,NULL,0,'你好！有什么可以帮您的吗？',1748584826869),(2,1,3,NULL,0,'wang3',1748579560738),(12,NULL,NULL,1,1,'三脚猫',1748579654736),(15,2,10,NULL,0,'',1748543255000);
/*!40000 ALTER TABLE `conversation` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `friendrequest`
--

DROP TABLE IF EXISTS `friendrequest`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `friendrequest` (
  `request_id` bigint NOT NULL AUTO_INCREMENT,
  `from_user_id` bigint NOT NULL,
  `to_user_id` bigint NOT NULL,
  `request_msg` varchar(200) DEFAULT NULL COMMENT '验证消息',
  `status` tinyint DEFAULT '0' COMMENT '0=待处理，1=已同意，2=已拒绝',
  PRIMARY KEY (`request_id`),
  KEY `idx_from_user` (`from_user_id`),
  KEY `idx_to_user` (`to_user_id`,`from_user_id`),
  CONSTRAINT `fk_friendrequest_from_user` FOREIGN KEY (`from_user_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_friendrequest_to_user` FOREIGN KEY (`to_user_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `friendrequest`
--

LOCK TABLES `friendrequest` WRITE;
/*!40000 ALTER TABLE `friendrequest` DISABLE KEYS */;
INSERT INTO `friendrequest` VALUES (1,2,1,'加个好友啊',2),(2,3,1,NULL,1),(3,1,3,NULL,1),(4,1,3,NULL,1),(6,10,2,NULL,1);
/*!40000 ALTER TABLE `friendrequest` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `group`
--

DROP TABLE IF EXISTS `group`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `group` (
  `group_id` bigint NOT NULL AUTO_INCREMENT COMMENT '群组ID',
  `group_name` varchar(50) NOT NULL COMMENT '群名称',
  `avatar` varchar(255) DEFAULT NULL COMMENT '群头像URL',
  `announcement` text COMMENT '群公告',
  `description` varchar(500) DEFAULT NULL COMMENT '群描述',
  `creator_id` bigint NOT NULL COMMENT '创建者用户ID',
  `member_count` int DEFAULT '1' COMMENT '成员数量',
  `max_member_count` int DEFAULT '500' COMMENT '最大成员数',
  PRIMARY KEY (`group_id`),
  KEY `idx_creator` (`creator_id`),
  KEY `idx_search` (`group_name`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='群组基础信息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `group`
--

LOCK TABLES `group` WRITE;
/*!40000 ALTER TABLE `group` DISABLE KEYS */;
INSERT INTO `group` VALUES (1,'三脚猫','/upload/groups/avatar/david.png',NULL,NULL,1,3,100);
/*!40000 ALTER TABLE `group` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `groupmember`
--

DROP TABLE IF EXISTS `groupmember`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `groupmember` (
  `member_id` bigint NOT NULL,
  `group_id` bigint NOT NULL COMMENT '群组ID',
  `role` tinyint DEFAULT '0' COMMENT '0=普通成员，1=管理员，2=群主',
  `nickname` varchar(50) DEFAULT NULL COMMENT '群内昵称',
  `join_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
  `last_speak_time` timestamp NULL DEFAULT NULL COMMENT '最后发言时间',
  `is_muted` tinyint(1) DEFAULT '0' COMMENT '是否被禁言',
  `is_top` tinyint(1) DEFAULT '0' COMMENT '是否置顶该群',
  PRIMARY KEY (`member_id`,`group_id`),
  KEY `idx_group_role` (`group_id`,`role`),
  KEY `member_id_idx` (`member_id`),
  CONSTRAINT `member_id` FOREIGN KEY (`member_id`) REFERENCES `user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='群组成员表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `groupmember`
--

LOCK TABLES `groupmember` WRITE;
/*!40000 ALTER TABLE `groupmember` DISABLE KEYS */;
INSERT INTO `groupmember` VALUES (1,1,0,'wang','2025-04-21 15:18:18',NULL,0,0),(2,1,0,'wang2','2025-04-21 15:18:18',NULL,0,0),(3,1,0,'wang3','2025-04-21 15:18:18',NULL,0,0);
/*!40000 ALTER TABLE `groupmember` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `mediaresource`
--

DROP TABLE IF EXISTS `mediaresource`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `mediaresource` (
  `resource_id` bigint NOT NULL AUTO_INCREMENT,
  `message_id` bigint DEFAULT NULL COMMENT '关联的消息ID',
  `user_id` bigint NOT NULL COMMENT '上传用户',
  `file_type` tinyint DEFAULT NULL COMMENT '1=图片，2=视频，3=语音，4=文件',
  `file_url` varchar(255) NOT NULL,
  `file_size` int DEFAULT NULL COMMENT '文件大小(字节)',
  `duration` int DEFAULT NULL COMMENT '音视频时长(秒)',
  `width` int DEFAULT NULL COMMENT '图片/视频宽度',
  `height` int DEFAULT NULL COMMENT '图片/视频高度',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`resource_id`),
  KEY `idx_message` (`message_id`),
  KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `mediaresource`
--

LOCK TABLES `mediaresource` WRITE;
/*!40000 ALTER TABLE `mediaresource` DISABLE KEYS */;
/*!40000 ALTER TABLE `mediaresource` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `message`
--

DROP TABLE IF EXISTS `message`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `message` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `sender_id` bigint NOT NULL,
  `conv_id` bigint NOT NULL,
  `is_group` tinyint(1) NOT NULL DEFAULT '0',
  `content` text NOT NULL,
  `timestamp` bigint NOT NULL,
  `message_type` varchar(20) DEFAULT 'text',
  `media_url` text,
  PRIMARY KEY (`id`),
  KEY `sender_id_idx` (`sender_id`),
  KEY `conv_idx` (`conv_id`),
  CONSTRAINT `conv` FOREIGN KEY (`conv_id`) REFERENCES `conversation` (`conv_id`),
  CONSTRAINT `sender_id` FOREIGN KEY (`sender_id`) REFERENCES `user` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=207 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `message`
--

LOCK TABLES `message` WRITE;
/*!40000 ALTER TABLE `message` DISABLE KEYS */;
INSERT INTO `message` VALUES (1,1,1,0,'你好',1719391234,'text',NULL),(20,1,1,0,'你是wang2吗',1745213103157,'text',NULL),(22,1,1,0,'你在干嘛',1745213249888,'text',NULL),(23,2,1,0,'我在做毕设',1745213262576,'text',NULL),(24,1,1,0,'我也是',1745214959681,'text',NULL),(83,1,2,0,'你好',1746990414970,'text',NULL),(176,10,15,0,'你们已经是好友了，开始聊天吧',1748542685448,'text',NULL),(192,1,12,1,'三脚猫',1748579549802,'text',NULL),(193,1,2,0,'wang3',1748579560738,'text',NULL),(194,1,1,0,'wang2',1748579568293,'text',NULL),(195,1,1,0,'wang2',1748579589532,'text',NULL),(196,1,12,1,'三脚猫',1748579654736,'text',NULL),(197,2,1,0,'wang',1748579952972,'text',NULL),(198,2,1,0,'wang',1748579962025,'text',NULL),(199,2,1,0,'wagn',1748582403987,'text',NULL),(200,2,1,0,'hello',1748582431205,'text',NULL),(201,1,1,0,'王',1748584733645,'text',NULL),(202,1,1,0,'',1748584760177,'video','/upload/messages/videos/video-1748588036900-910221926.tmp'),(203,1,1,0,'',1748584772777,'image','/upload/messages/images/image-1748588049664-481432307.tmp'),(204,1,1,0,'',1748584781726,'voice','/upload/messages/voices/voice-1748588058570-367415762.tmp'),(205,2,1,0,'你好',1748584821099,'text',NULL),(206,1,1,0,'你好！有什么可以帮您的吗？',1748584826869,'text',NULL);
/*!40000 ALTER TABLE `message` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user` (
  `user_id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户唯一ID',
  `username` varchar(50) NOT NULL COMMENT '用户名(用于登录)',
  `password` varchar(255) NOT NULL COMMENT '加密后的密码',
  `avatar` varchar(255) DEFAULT NULL COMMENT '头像URL',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `gender` tinyint DEFAULT '0' COMMENT '0=未知，1=男，2=女',
  `region` varchar(100) DEFAULT NULL COMMENT '地区',
  `signature` varchar(255) DEFAULT NULL COMMENT '个性签名',
  `birth_date` varchar(255) DEFAULT NULL COMMENT '生日',
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `phone` (`phone`),
  UNIQUE KEY `email` (`email`),
  KEY `idx_username` (`username`),
  KEY `idx_phone` (`phone`),
  KEY `idx_email` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user`
--

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
INSERT INTO `user` VALUES (1,'wang','$2b$10$aKGr9zBrJgopJUbTOw/9dO33Z9FhD3JtOiKwf8Lk3.hVOrndFaLy2','/upload/users/avatar/DavidMiao.png','19229761736','344556943@qq.com',1,'百慕大','你好',NULL),(2,'wang2','$2b$10$j1gPariBY/34w60zJ7F.9Op3SomK8MhCkYMgIXnp6P.EDTUfXNfva','/upload/users/avatar/avatar-1744960055941-364607068.jpg',NULL,NULL,0,NULL,NULL,NULL),(3,'wang3','$2b$10$j1gPariBY/34w60zJ7F.9Op3SomK8MhCkYMgIXnp6P.EDTUfXNfva','/upload/users/avatar/avatar-1745033286116-367959096.jpg',NULL,NULL,0,NULL,NULL,NULL),(10,'xinye','$2b$10$aKGr9zBrJgopJUbTOw/9dO33Z9FhD3JtOiKwf8Lk3.hVOrndFaLy2','/upload/users/avatar/avatar-1748457103890-827918952.jpg',NULL,NULL,0,NULL,NULL,NULL);
/*!40000 ALTER TABLE `user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `usersetting`
--

DROP TABLE IF EXISTS `usersetting`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `usersetting` (
  `setting_id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `notification_enabled` tinyint(1) DEFAULT '1',
  `voice_enabled` tinyint(1) DEFAULT '1',
  `vibration_enabled` tinyint(1) DEFAULT '1',
  `theme` varchar(20) DEFAULT 'light',
  `font_size` tinyint DEFAULT '16',
  PRIMARY KEY (`setting_id`),
  UNIQUE KEY `user_id` (`user_id`),
  KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `usersetting`
--

LOCK TABLES `usersetting` WRITE;
/*!40000 ALTER TABLE `usersetting` DISABLE KEYS */;
/*!40000 ALTER TABLE `usersetting` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-06-02  8:48:47
