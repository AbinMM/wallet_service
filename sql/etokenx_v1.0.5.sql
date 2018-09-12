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