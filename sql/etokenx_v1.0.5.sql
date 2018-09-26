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
