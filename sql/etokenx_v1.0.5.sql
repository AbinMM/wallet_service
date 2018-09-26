CREATE TABLE `user_eos_account` (
  `id` BIGINT(11) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `uid` BIGINT(20) DEFAULT NULL COMMENT '用户UID',
  `eos_account` VARCHAR(50) COLLATE utf8mb4_bin NOT NULL COMMENT '用户EOS账号',
  `create_date` DATETIME NOT NULL COMMENT '创建日期',
  `update_date` DATETIME NOT NULL COMMENT '修改日期',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uid` (`uid`,`eos_account`),
  KEY `uid_2` (`uid`),
  KEY `eos_account` (`eos_account`),
  KEY `create_date` (`create_date`),
  KEY `update_date` (`update_date`)
) ENGINE=INNODB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

ALTER TABLE coins ADD recommend_level INT DEFAULT 1 COMMENT '推荐指数';
ALTER TABLE coins ADD project_creative VARCHAR(100) DEFAULT NULL COMMENT '项目创新';
ALTER TABLE coins ADD investment_value VARCHAR(100) DEFAULT NULL COMMENT '投资价值';
ALTER TABLE coins ADD crowdfunding_date VARCHAR(100) DEFAULT NULL COMMENT '众筹日期';

/*Table structure for table `activity_info` */
CREATE TABLE `activity_info` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(200) NOT NULL COMMENT '活动名称',
  `status` varchar(50) DEFAULT NULL COMMENT '活动状态',
  `description` text NOT NULL COMMENT '活动描述',
  `start_date` datetime NOT NULL COMMENT '开始时间',
  `end_date` datetime NOT NULL COMMENT '结束时间',
  PRIMARY KEY (`id`),
  KEY `status` (`status`),
  KEY `start_date` (`start_date`),
  KEY `end_date` (`end_date`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;

/*Table structure for table `activity_stage` */
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
  PRIMARY KEY (`id`),
  KEY `activity_id` (`activity_id`),
  KEY `status` (`status`),
  KEY `start_date` (`start_date`),
  KEY `end_date` (`end_date`),
  KEY `seq` (`seq`),
  KEY `is_paid` (`is_paid`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8;

/*Table structure for table `activity_stage_user` */
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
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;
