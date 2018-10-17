/*
SQLyog Ultimate v12.5.0 (64 bit)
MySQL - 5.7.13 : Database - etoken
*********************************************************************
*/

/*!40101 SET NAMES utf8 */;

/*!40101 SET SQL_MODE=''*/;

/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
/*Table structure for table `activity_info` */

DROP TABLE IF EXISTS `activity_info`;

CREATE TABLE `activity_info` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(200) NOT NULL COMMENT '活动名称',
  `status` varchar(50) DEFAULT NULL COMMENT '活动状态',
  `description` text NOT NULL COMMENT '活动描述',
  `start_date` datetime NOT NULL COMMENT '开始时间',
  `end_date` datetime NOT NULL COMMENT '结束时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;

/*Table structure for table `activity_stage` */

DROP TABLE IF EXISTS `activity_stage`;

CREATE TABLE `activity_stage` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `activity_id` bigint(20) unsigned NOT NULL COMMENT '活动ID',
  `name` varchar(200) NOT NULL COMMENT '活动期名',
  `status` varchar(50) NOT NULL DEFAULT 'new' COMMENT '状态',
  `start_date` datetime NOT NULL COMMENT '开始时间',
  `end_date` datetime NOT NULL COMMENT '结束时间',
  `common_count` bigint(20) NOT NULL COMMENT '普通中奖人数',
  `common_min_qty` bigint(20) NOT NULL COMMENT '普通最小币数量',
  `common_max_qty` bigint(20) NOT NULL COMMENT '普通最大币数量',
  `lucky_count` bigint(20) NOT NULL COMMENT '幸运人数',
  `lucky_coin_qty` bigint(20) NOT NULL COMMENT '幸运奖币数量',
  `lucky_method` varchar(50) NOT NULL COMMENT '幸运奖分法',
  `token_name` varchar(50) NOT NULL DEFAULT 'EOS' COMMENT '币名',
  `token_contract` varchar(50) NOT NULL COMMENT '币合约账号',
  `precision_number` int(11) NOT NULL DEFAULT '4' COMMENT '币精度',
  `seq` int(11) NOT NULL DEFAULT '1' COMMENT '序号',
  `is_paid` varchar(10) NOT NULL DEFAULT 'n' COMMENT '是否发放奖励',
  `need_buy_eos` bigint(20) NOT NULL DEFAULT '0' COMMENT '最低购买EOS数量',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8;

/*Table structure for table `activity_stage_user` */

DROP TABLE IF EXISTS `activity_stage_user`;

CREATE TABLE `activity_stage_user` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `activity_id` bigint(20) unsigned NOT NULL COMMENT '活动ID',
  `activity_stage_id` bigint(20) unsigned NOT NULL COMMENT '阶段ID',
  `status` varchar(50) NOT NULL DEFAULT 'new' COMMENT '状态',
  `account_name` varchar(50) NOT NULL COMMENT '账号名',
  `is_winner` varchar(10) NOT NULL DEFAULT 'n' COMMENT '是否优胜者',
  `is_lucky` varchar(10) NOT NULL DEFAULT 'n' COMMENT '是否幸运者',
  `trx_id` varchar(200) NOT NULL COMMENT '交易ID',
  `trade_qty` decimal(12,4) NOT NULL DEFAULT '0.0000' COMMENT '交易数量',
  `trade_date` datetime NOT NULL COMMENT '交易时间',
  `win_qty` decimal(12,5) NOT NULL DEFAULT '0.00000' COMMENT '获胜数量',
  `lucky_qty` decimal(12,5) NOT NULL DEFAULT '0.00000' COMMENT '幸运数量',
  `create_date` datetime NOT NULL COMMENT '创建时间',
  `update_date` datetime NOT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `activity_stage_id` (`activity_stage_id`,`account_name`),
  UNIQUE KEY `trx_id` (`trx_id`),
  KEY `activity_id` (`activity_id`),
  KEY `trade_date` (`trade_date`),
  KEY `activity_stage_id_2` (`activity_stage_id`,`is_winner`),
  KEY `activity_stage_id_3` (`activity_stage_id`,`is_lucky`),
  KEY `activity_stage_id_4` (`activity_stage_id`,`status`)
) ENGINE=InnoDB AUTO_INCREMENT=152 DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `eos_account_order`;

CREATE TABLE `eos_account_order` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `order_no` varchar(50) NOT NULL COMMENT '订单号',
  `status` varchar(50) NOT NULL DEFAULT 'new' COMMENT '状态',
  `amount` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '订单金额',
  `account_name` varchar(100) NOT NULL COMMENT 'EOS账号',
  `owner_public_key` varchar(100) NOT NULL COMMENT 'Owner公钥',
  `active_public_key` varchar(100) NOT NULL COMMENT 'Active公钥',
  `ip` varchar(100) NOT NULL COMMENT 'IP地址',
  `transaction_id` varchar(100) DEFAULT NULL COMMENT '支付流水号',
  `notify_content` text COMMENT '通知内容',
  `notify_date` datetime DEFAULT NULL COMMENT '通知日期',
  `createdate` datetime NOT NULL COMMENT '创建日期',
  `updatedate` datetime NOT NULL COMMENT '修改日期',
  PRIMARY KEY (`id`),
  UNIQUE KEY `order_no` (`order_no`),
  KEY `account_name` (`account_name`),
  KEY `createdate` (`createdate`),
  KEY `account_name_2` (`account_name`,`owner_public_key`)
) ENGINE=InnoDB AUTO_INCREMENT=154 DEFAULT CHARSET=utf8 COMMENT='EOS账号订单';

DROP TABLE IF EXISTS `sys_notification`;

CREATE TABLE `sys_notification` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `picurl` varchar(50) NOT NULL COMMENT '图片地址',
  `url` varchar(50) NOT NULL COMMENT '跳转地址',
  `status` bigint(20) DEFAULT '0' COMMENT '状态，是否是活动状态，默认为0是活动状态',
  `seq` bigint(20) DEFAULT '0' COMMENT '排序',
  `starttime` datetime DEFAULT NULL,
  `endtime` datetime DEFAULT NULL,
  `createtime` datetime DEFAULT CURRENT_TIMESTAMP,
  `modifydate` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8 COMMENT='系统通知';

/*添加配置信息 */
insert  into `sysconf`(`name`,`desc`,`value`,`createdate`,`modifydate`) values 
('et_open','ET交易所是否开放','1','2018-09-18 13:50:35','2018-09-18 13:50:35'),
('eos_account_price','Eos账号费用','100','2018-10-17 05:44:29','2018-10-17 05:44:29');


/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
