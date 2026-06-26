-- MySQL dump 10.13  Distrib 8.0.44, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: ssafy_home
-- ------------------------------------------------------
-- Server version	8.0.44

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `analysis_snapshot`
--

DROP TABLE IF EXISTS `analysis_snapshot`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `analysis_snapshot` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `label` varchar(120) NOT NULL,
  `latitude` double NOT NULL,
  `longitude` double NOT NULL,
  `radius_m` int NOT NULL,
  `commercial_count` int NOT NULL,
  `traffic_event_count` int NOT NULL,
  `score` int NOT NULL,
  `risk_level` varchar(30) NOT NULL,
  `source` varchar(20) NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=68 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `bus_city_codes`
--

DROP TABLE IF EXISTS `bus_city_codes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `bus_city_codes` (
  `city_code` varchar(5) NOT NULL,
  `city_name` varchar(80) NOT NULL,
  `synced_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`city_code`),
  KEY `idx_bus_city_codes_name` (`city_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `bus_stops`
--

DROP TABLE IF EXISTS `bus_stops`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `bus_stops` (
  `node_id` varchar(30) NOT NULL,
  `node_name` varchar(120) NOT NULL,
  `node_no` varchar(30) DEFAULT NULL,
  `city_code` varchar(5) NOT NULL,
  `latitude` double NOT NULL,
  `longitude` double NOT NULL,
  `synced_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`node_id`),
  KEY `idx_bus_stops_city` (`city_code`),
  KEY `idx_bus_stops_lat_lng` (`latitude`,`longitude`),
  KEY `idx_bus_stops_lng_lat` (`longitude`,`latitude`),
  KEY `idx_bus_stops_city_name` (`city_code`,`node_name`),
  KEY `idx_bus_stops_node_name` (`node_name`),
  CONSTRAINT `fk_bus_stops_city` FOREIGN KEY (`city_code`) REFERENCES `bus_city_codes` (`city_code`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `dongcodes`
--

DROP TABLE IF EXISTS `dongcodes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `dongcodes` (
  `dong_code` varchar(10) NOT NULL COMMENT '踰뺤젙?숈퐫?',
  `sido_name` varchar(30) DEFAULT NULL COMMENT '?쒕룄?대쫫',
  `gugun_name` varchar(30) DEFAULT NULL COMMENT '援ш뎔?대쫫',
  `dong_name` varchar(30) DEFAULT NULL COMMENT '?숈씠由',
  PRIMARY KEY (`dong_code`),
  KEY `idx_dongcodes_sido_gugun_dong` (`sido_name`,`gugun_name`,`dong_name`),
  KEY `idx_dongcodes_region_code` (`sido_name`,`gugun_name`,`dong_name`,`dong_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='踰뺤젙?숈퐫?쒗뀒?대툝';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `favorite_deals`
--

DROP TABLE IF EXISTS `favorite_deals`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `favorite_deals` (
  `user_id` varchar(50) NOT NULL,
  `deal_no` int NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`,`deal_no`),
  KEY `fk_favorite_deals_housedeals` (`deal_no`),
  CONSTRAINT `fk_favorite_deals_housedeals` FOREIGN KEY (`deal_no`) REFERENCES `housedeals` (`no`) ON DELETE CASCADE,
  CONSTRAINT `fk_favorite_deals_members` FOREIGN KEY (`user_id`) REFERENCES `members` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `favorite_rental_notices`
--

DROP TABLE IF EXISTS `favorite_rental_notices`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `favorite_rental_notices` (
  `user_id` varchar(50) NOT NULL,
  `notice_id` varchar(40) NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`,`notice_id`),
  KEY `idx_favorite_rental_notices_notice` (`notice_id`),
  CONSTRAINT `fk_favorite_rental_notices_member` FOREIGN KEY (`user_id`) REFERENCES `members` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `favorite_transfers`
--

DROP TABLE IF EXISTS `favorite_transfers`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `favorite_transfers` (
  `user_id` varchar(50) NOT NULL,
  `transfer_id` int NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`,`transfer_id`),
  KEY `idx_favorite_transfers_transfer` (`transfer_id`),
  CONSTRAINT `fk_favorite_transfers_member` FOREIGN KEY (`user_id`) REFERENCES `members` (`user_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_favorite_transfers_transfer` FOREIGN KEY (`transfer_id`) REFERENCES `transfers` (`transfer_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `housedeals`
--

DROP TABLE IF EXISTS `housedeals`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `housedeals` (
  `no` int NOT NULL AUTO_INCREMENT COMMENT '嫄곕옒踰덊샇',
  `apt_seq` varchar(20) DEFAULT NULL COMMENT '?꾪뙆?몄퐫?',
  `apt_dong` varchar(40) DEFAULT NULL COMMENT '?꾪뙆?몃룞',
  `floor` varchar(3) DEFAULT NULL COMMENT '?꾪뙆?몄링',
  `deal_year` int DEFAULT NULL COMMENT '嫄곕옒?꾨룄',
  `deal_month` int DEFAULT NULL COMMENT '嫄곕옒?',
  `deal_day` int DEFAULT NULL COMMENT '嫄곕옒?',
  `exclu_use_ar` decimal(7,2) DEFAULT NULL COMMENT '?꾪뙆?몃㈃?',
  `deal_amount` varchar(10) DEFAULT NULL COMMENT '嫄곕옒媛?꺽',
  PRIMARY KEY (`no`),
  KEY `apt_seq_to_house_info_idx` (`apt_seq`),
  KEY `idx_housedeals_apt_date_no` (`apt_seq`,`deal_year` DESC,`deal_month` DESC,`deal_day` DESC,`no` DESC),
  KEY `idx_housedeals_date_no` (`deal_year` DESC,`deal_month` DESC,`deal_day` DESC,`no` DESC),
  CONSTRAINT `apt_seq_to_house_info` FOREIGN KEY (`apt_seq`) REFERENCES `houseinfos` (`apt_seq`)
) ENGINE=InnoDB AUTO_INCREMENT=7957804 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='二쇳깮嫄곕옒?뺣낫?뚯씠釉';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `houseinfos`
--

DROP TABLE IF EXISTS `houseinfos`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `houseinfos` (
  `apt_seq` varchar(20) NOT NULL COMMENT '?꾪뙆?몄퐫?',
  `sgg_cd` varchar(5) DEFAULT NULL COMMENT '?쒓뎔援ъ퐫?',
  `umd_cd` varchar(5) DEFAULT NULL COMMENT '?띾㈃?숈퐫?',
  `umd_nm` varchar(20) DEFAULT NULL COMMENT '?띾㈃?숈씠由',
  `jibun` varchar(10) DEFAULT NULL COMMENT '吏?쾲',
  `road_nm_sgg_cd` varchar(5) DEFAULT NULL COMMENT '?꾨줈紐낆떆援곌뎄肄붾뱶',
  `road_nm` varchar(20) DEFAULT NULL COMMENT '?꾨줈紐',
  `road_nm_bonbun` varchar(10) DEFAULT NULL COMMENT '?꾨줈紐낃린珥덈쾲?',
  `road_nm_bubun` varchar(10) DEFAULT NULL COMMENT '?꾨줈紐낆텛媛?쾲?',
  `apt_nm` varchar(40) DEFAULT NULL COMMENT '?꾪뙆?몄씠由',
  `build_year` int DEFAULT NULL COMMENT '以?났?꾨룄',
  `latitude` varchar(45) DEFAULT NULL COMMENT '?꾨룄',
  `longitude` varchar(45) DEFAULT NULL COMMENT '寃쎈룄',
  PRIMARY KEY (`apt_seq`),
  KEY `idx_houseinfos_region_seq` (`sgg_cd`,`umd_cd`,`apt_seq`),
  KEY `idx_houseinfos_apt_nm` (`apt_nm`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='二쇳깮?뺣낫?뚯씠釉';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `lh_notice_details`
--

DROP TABLE IF EXISTS `lh_notice_details`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `lh_notice_details` (
  `notice_id` varchar(40) NOT NULL,
  `contract_address` varchar(300) DEFAULT NULL,
  `contract_detail_address` varchar(300) DEFAULT NULL,
  `apply_start_date` varchar(20) DEFAULT NULL,
  `apply_end_date` varchar(20) DEFAULT NULL,
  `contact` varchar(100) DEFAULT NULL,
  `raw_json` text,
  `cached_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`notice_id`),
  CONSTRAINT `fk_lh_notice_details_notice` FOREIGN KEY (`notice_id`) REFERENCES `rental_notice_cache` (`notice_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `lh_notice_supplies`
--

DROP TABLE IF EXISTS `lh_notice_supplies`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `lh_notice_supplies` (
  `supply_id` bigint NOT NULL AUTO_INCREMENT,
  `notice_id` varchar(40) NOT NULL,
  `usage` varchar(120) DEFAULT NULL,
  `address` varchar(300) DEFAULT NULL,
  `area` varchar(80) DEFAULT NULL,
  `expected_amount` varchar(300) DEFAULT NULL,
  `house_type` varchar(120) DEFAULT NULL,
  `household_count` varchar(80) DEFAULT NULL,
  `raw_json` text,
  `cached_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`supply_id`),
  KEY `fk_lh_notice_supplies_notice` (`notice_id`),
  CONSTRAINT `fk_lh_notice_supplies_notice` FOREIGN KEY (`notice_id`) REFERENCES `rental_notice_cache` (`notice_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=4547 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `loan_products`
--

DROP TABLE IF EXISTS `loan_products`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `loan_products` (
  `product_code` varchar(80) NOT NULL,
  `loan_type` varchar(30) NOT NULL,
  `company_code` varchar(30) DEFAULT NULL,
  `company_name` varchar(120) NOT NULL,
  `product_name` varchar(200) NOT NULL,
  `join_way` varchar(300) DEFAULT NULL,
  `loan_incidental_expense` text,
  `early_repayment_fee` text,
  `delinquency_rate` text,
  `loan_limit` text,
  `disclosure_start_day` varchar(20) DEFAULT NULL,
  `disclosure_end_day` varchar(20) DEFAULT NULL,
  `submitted_at` varchar(30) DEFAULT NULL,
  `source` varchar(20) DEFAULT 'api',
  `cached_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`product_code`),
  KEY `idx_loan_products_type_company` (`loan_type`,`company_name`),
  KEY `idx_loan_products_type_name_company` (`loan_type`,`product_name`,`company_name`),
  KEY `idx_loan_products_cached_at` (`cached_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `loan_rate_options`
--

DROP TABLE IF EXISTS `loan_rate_options`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `loan_rate_options` (
  `rate_option_id` bigint NOT NULL AUTO_INCREMENT,
  `product_code` varchar(80) NOT NULL,
  `repayment_type_code` varchar(20) DEFAULT NULL,
  `repayment_type_name` varchar(80) DEFAULT NULL,
  `rate_type_code` varchar(20) DEFAULT NULL,
  `rate_type_name` varchar(80) DEFAULT NULL,
  `mortgage_type_code` varchar(20) DEFAULT NULL,
  `mortgage_type_name` varchar(80) DEFAULT NULL,
  `rate_min` decimal(5,2) DEFAULT NULL,
  `rate_max` decimal(5,2) DEFAULT NULL,
  `rate_avg` decimal(5,2) DEFAULT NULL,
  `cached_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`rate_option_id`),
  KEY `idx_loan_rate_options_product` (`product_code`),
  KEY `idx_loan_rate_options_product_rates` (`product_code`,`rate_min`,`rate_max`,`rate_avg`),
  KEY `idx_loan_rate_options_types` (`rate_type_code`,`mortgage_type_code`),
  KEY `idx_loan_rate_options_rate_min_product` (`rate_min`,`product_code`),
  CONSTRAINT `fk_loan_rate_options_product` FOREIGN KEY (`product_code`) REFERENCES `loan_products` (`product_code`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=95 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `member_financial_profiles`
--

DROP TABLE IF EXISTS `member_financial_profiles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `member_financial_profiles` (
  `user_id` varchar(50) NOT NULL,
  `available_assets` decimal(19,0) NOT NULL DEFAULT '0',
  `annual_income` decimal(19,0) NOT NULL DEFAULT '0',
  `monthly_savings` decimal(19,0) NOT NULL DEFAULT '0',
  `existing_loan_balance` decimal(19,0) NOT NULL DEFAULT '0',
  `existing_monthly_debt_payment` decimal(19,0) NOT NULL DEFAULT '0',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`),
  CONSTRAINT `fk_financial_profile_member` FOREIGN KEY (`user_id`) REFERENCES `members` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `members`
--

DROP TABLE IF EXISTS `members`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `members` (
  `user_id` varchar(50) NOT NULL,
  `password` varchar(255) NOT NULL,
  `name` varchar(255) NOT NULL,
  `email` varchar(255) NOT NULL,
  `phone` varchar(255) NOT NULL,
  PRIMARY KEY (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `notices`
--

DROP TABLE IF EXISTS `notices`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `notices` (
  `notice_id` int NOT NULL AUTO_INCREMENT,
  `title` varchar(200) NOT NULL,
  `content` text NOT NULL,
  `writer` varchar(50) NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `popup_enabled` tinyint(1) DEFAULT '0',
  `pinned` tinyint(1) DEFAULT '0',
  `visible` tinyint(1) DEFAULT '1',
  PRIMARY KEY (`notice_id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `openapi_sync_logs`
--

DROP TABLE IF EXISTS `openapi_sync_logs`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `openapi_sync_logs` (
  `sync_log_id` bigint NOT NULL AUTO_INCREMENT,
  `job_name` varchar(100) NOT NULL,
  `api_name` varchar(100) NOT NULL,
  `status` varchar(30) NOT NULL,
  `started_at` timestamp NULL DEFAULT NULL,
  `finished_at` timestamp NULL DEFAULT NULL,
  `fetched_count` int DEFAULT '0',
  `saved_count` int DEFAULT '0',
  `error_message` text,
  PRIMARY KEY (`sync_log_id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `rental_notice_cache`
--

DROP TABLE IF EXISTS `rental_notice_cache`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `rental_notice_cache` (
  `notice_id` varchar(40) NOT NULL,
  `title` varchar(300) NOT NULL,
  `region_name` varchar(80) DEFAULT NULL,
  `notice_type` varchar(80) DEFAULT NULL,
  `detail_type` varchar(80) DEFAULT NULL,
  `status` varchar(40) DEFAULT NULL,
  `notice_date` varchar(20) DEFAULT NULL,
  `close_date` varchar(20) DEFAULT NULL,
  `detail_url` varchar(1000) DEFAULT NULL,
  `ccr_cnnt_sys_ds_cd` varchar(10) DEFAULT NULL,
  `upp_ais_tp_cd` varchar(10) DEFAULT NULL,
  `ais_tp_cd` varchar(10) DEFAULT NULL,
  `spl_inf_tp_cd` varchar(10) DEFAULT NULL,
  `source` varchar(20) DEFAULT NULL,
  `cached_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`notice_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `rental_notice_email_logs`
--

DROP TABLE IF EXISTS `rental_notice_email_logs`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `rental_notice_email_logs` (
  `email_log_id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` varchar(50) NOT NULL,
  `notice_id` varchar(40) NOT NULL,
  `event_type` varchar(40) NOT NULL,
  `recipient_email` varchar(255) NOT NULL,
  `subject` varchar(300) NOT NULL,
  `sent_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`email_log_id`),
  UNIQUE KEY `uk_rental_notice_email_event` (`user_id`,`notice_id`,`event_type`),
  KEY `idx_rental_notice_email_logs_sent_at` (`sent_at`),
  CONSTRAINT `fk_rental_notice_email_logs_member` FOREIGN KEY (`user_id`) REFERENCES `members` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `transfer_comments`
--

DROP TABLE IF EXISTS `transfer_comments`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `transfer_comments` (
  `comment_id` int NOT NULL AUTO_INCREMENT,
  `transfer_id` int NOT NULL,
  `writer_id` varchar(50) NOT NULL,
  `content` text NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`comment_id`),
  KEY `idx_transfer_comments_transfer` (`transfer_id`,`created_at`,`comment_id`),
  KEY `idx_transfer_comments_writer` (`writer_id`),
  CONSTRAINT `fk_transfer_comments_member` FOREIGN KEY (`writer_id`) REFERENCES `members` (`user_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_transfer_comments_transfer` FOREIGN KEY (`transfer_id`) REFERENCES `transfers` (`transfer_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `transfer_images`
--

DROP TABLE IF EXISTS `transfer_images`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `transfer_images` (
  `image_id` int NOT NULL AUTO_INCREMENT,
  `transfer_id` int NOT NULL,
  `image_url` varchar(1000) NOT NULL,
  `sort_order` int NOT NULL DEFAULT '0',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`image_id`),
  KEY `fk_transfer_images_transfer` (`transfer_id`),
  CONSTRAINT `fk_transfer_images_transfer` FOREIGN KEY (`transfer_id`) REFERENCES `transfers` (`transfer_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `transfers`
--

DROP TABLE IF EXISTS `transfers`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `transfers` (
  `transfer_id` int NOT NULL AUTO_INCREMENT,
  `writer_id` varchar(50) NOT NULL,
  `title` varchar(200) NOT NULL,
  `content` text NOT NULL,
  `status` varchar(30) NOT NULL,
  `address` varchar(255) NOT NULL,
  `detail_address` varchar(255) DEFAULT NULL,
  `floor` varchar(30) DEFAULT NULL,
  `exclusive_area` decimal(10,2) DEFAULT NULL,
  `deposit_amount` int NOT NULL DEFAULT '0',
  `monthly_rent_amount` int NOT NULL DEFAULT '0',
  `maintenance_fee` int NOT NULL DEFAULT '0',
  `transfer_fee` int NOT NULL DEFAULT '0',
  `contract_end_date` date DEFAULT NULL,
  `move_in_date` date DEFAULT NULL,
  `contact_phone` varchar(50) DEFAULT NULL,
  `view_count` int NOT NULL DEFAULT '0',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`transfer_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Temporary view structure for view `v_bus_stops_with_city`
--

DROP TABLE IF EXISTS `v_bus_stops_with_city`;
/*!50001 DROP VIEW IF EXISTS `v_bus_stops_with_city`*/;
SET @saved_cs_client     = @@character_set_client;
/*!50503 SET character_set_client = utf8mb4 */;
/*!50001 CREATE VIEW `v_bus_stops_with_city` AS SELECT 
 1 AS `node_id`,
 1 AS `node_name`,
 1 AS `node_no`,
 1 AS `city_code`,
 1 AS `city_name`,
 1 AS `latitude`,
 1 AS `longitude`,
 1 AS `synced_at`*/;
SET character_set_client = @saved_cs_client;

--
-- Temporary view structure for view `v_loan_product_rate_options`
--

DROP TABLE IF EXISTS `v_loan_product_rate_options`;
/*!50001 DROP VIEW IF EXISTS `v_loan_product_rate_options`*/;
SET @saved_cs_client     = @@character_set_client;
/*!50503 SET character_set_client = utf8mb4 */;
/*!50001 CREATE VIEW `v_loan_product_rate_options` AS SELECT 
 1 AS `product_code`,
 1 AS `loan_type`,
 1 AS `company_code`,
 1 AS `company_name`,
 1 AS `product_name`,
 1 AS `join_way`,
 1 AS `loan_incidental_expense`,
 1 AS `early_repayment_fee`,
 1 AS `delinquency_rate`,
 1 AS `loan_limit`,
 1 AS `disclosure_start_day`,
 1 AS `disclosure_end_day`,
 1 AS `submitted_at`,
 1 AS `source`,
 1 AS `product_cached_at`,
 1 AS `rate_option_id`,
 1 AS `repayment_type_code`,
 1 AS `repayment_type_name`,
 1 AS `rate_type_code`,
 1 AS `rate_type_name`,
 1 AS `mortgage_type_code`,
 1 AS `mortgage_type_name`,
 1 AS `rate_min`,
 1 AS `rate_max`,
 1 AS `rate_avg`,
 1 AS `rate_cached_at`*/;
SET character_set_client = @saved_cs_client;

--
-- Dumping events for database 'ssafy_home'
--

--
-- Dumping routines for database 'ssafy_home'
--

--
-- Final view structure for view `v_bus_stops_with_city`
--

/*!50001 DROP VIEW IF EXISTS `v_bus_stops_with_city`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8mb4 */;
/*!50001 SET character_set_results     = utf8mb4 */;
/*!50001 SET collation_connection      = utf8mb4_0900_ai_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`ssafy`@`%` SQL SECURITY DEFINER */
/*!50001 VIEW `v_bus_stops_with_city` AS select `bs`.`node_id` AS `node_id`,`bs`.`node_name` AS `node_name`,`bs`.`node_no` AS `node_no`,`bs`.`city_code` AS `city_code`,`bc`.`city_name` AS `city_name`,`bs`.`latitude` AS `latitude`,`bs`.`longitude` AS `longitude`,`bs`.`synced_at` AS `synced_at` from (`bus_stops` `bs` join `bus_city_codes` `bc` on((`bc`.`city_code` = `bs`.`city_code`))) */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;

--
-- Final view structure for view `v_loan_product_rate_options`
--

/*!50001 DROP VIEW IF EXISTS `v_loan_product_rate_options`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8mb4 */;
/*!50001 SET character_set_results     = utf8mb4 */;
/*!50001 SET collation_connection      = utf8mb4_0900_ai_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`ssafy`@`%` SQL SECURITY DEFINER */
/*!50001 VIEW `v_loan_product_rate_options` AS select `lp`.`product_code` AS `product_code`,`lp`.`loan_type` AS `loan_type`,`lp`.`company_code` AS `company_code`,`lp`.`company_name` AS `company_name`,`lp`.`product_name` AS `product_name`,`lp`.`join_way` AS `join_way`,`lp`.`loan_incidental_expense` AS `loan_incidental_expense`,`lp`.`early_repayment_fee` AS `early_repayment_fee`,`lp`.`delinquency_rate` AS `delinquency_rate`,`lp`.`loan_limit` AS `loan_limit`,`lp`.`disclosure_start_day` AS `disclosure_start_day`,`lp`.`disclosure_end_day` AS `disclosure_end_day`,`lp`.`submitted_at` AS `submitted_at`,`lp`.`source` AS `source`,`lp`.`cached_at` AS `product_cached_at`,`lro`.`rate_option_id` AS `rate_option_id`,`lro`.`repayment_type_code` AS `repayment_type_code`,`lro`.`repayment_type_name` AS `repayment_type_name`,`lro`.`rate_type_code` AS `rate_type_code`,`lro`.`rate_type_name` AS `rate_type_name`,`lro`.`mortgage_type_code` AS `mortgage_type_code`,`lro`.`mortgage_type_name` AS `mortgage_type_name`,`lro`.`rate_min` AS `rate_min`,`lro`.`rate_max` AS `rate_max`,`lro`.`rate_avg` AS `rate_avg`,`lro`.`cached_at` AS `rate_cached_at` from (`loan_products` `lp` left join `loan_rate_options` `lro` on((`lro`.`product_code` = `lp`.`product_code`))) */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-06-23 16:16:35
