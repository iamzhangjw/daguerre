drop database if exists `daguerre`;
create database `daguerre` CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

drop table if exists `file`;
CREATE TABLE `file` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
    `uid` char(26) NOT NULL COMMENT '文件 unique id',
    `attach` char(10) DEFAULT NULL COMMENT '文件扩展，主要用来区分缩略图，为空表示原文件，图片为 thumb，视频为 cover',
    `store_path` varchar(64) NOT NULL COMMENT '存储路径',
    `original_name` varchar(256) DEFAULT NULL COMMENT '原始文件名',
    `type` varchar(10) NOT NULL COMMENT '文件类型',
    `bucket_name` varchar(16) DEFAULT NULL COMMENT '桶名称',
    `byte_length` bigint(15) NOT NULL COMMENT '文件大小',
    `chunk_size` int DEFAULT NULL COMMENT '分片大小',
    `chunk_count` tinyint DEFAULT '0' COMMENT '分片数量',
    `uploaded_chunk` tinyint DEFAULT NULL COMMENT '已上传分片索引',
    `uploaded_length` bigint(15) NOT NULL DEFAULT '0' COMMENT '文件大小',
    `completed` BOOLEAN NOT NULL DEFAULT FALSE COMMENT '文件上传/合并完成标记',
    `complete_at` bigint(13) DEFAULT NULL COMMENT '完成时间戳',
    `oss_id` varchar(36) DEFAULT NULL COMMENT 'oss id',
    `access_key` char(16) NOT NULL COMMENT 'access key',
    `expire_at` bigint(13) NOT NULL DEFAULT -1 COMMENT '失效时间戳',
    `create_at` bigint(13) NOT NULL COMMENT '创建时间戳',
    `deleted` BOOLEAN NOT NULL DEFAULT FALSE COMMENT '删除标记',
    `version` bigint(13) NOT NULL COMMENT '版本',
    PRIMARY KEY (`id`),
    UNIQUE KEY `i_uid` (`uid`, `attach`)
) PARTITION BY LINEAR KEY(access_key) PARTITIONS 16
ENGINE=InnoDB COMMENT='文件记录表';

drop table if exists `file_chunk`;
CREATE TABLE `file_chunk` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
    `uid` char(26) NOT NULL COMMENT '文件uid',
    `store_path` varchar(64) NOT NULL COMMENT '存储路径',
    `original_name` varchar(256) DEFAULT NULL COMMENT '原始文件名',
    `type` varchar(10) NOT NULL COMMENT '文件类型',
    `bucket_name` varchar(16) DEFAULT NULL COMMENT '桶名称',
    `byte_length` bigint(15) NOT NULL COMMENT '文件大小',
    `chunk_index` tinyint NOT NULL COMMENT '分片索引',
    `extra` varchar(128) DEFAULT NULL COMMENT '扩展数据',
    `create_at` bigint(13) NOT NULL COMMENT '创建时间戳',
    `deleted` BOOLEAN NOT NULL DEFAULT FALSE COMMENT '删除标记',
    `version` bigint(13) NOT NULL COMMENT '版本',
    PRIMARY KEY (`id`),
    KEY `i_uid` (`uid`)
) ENGINE=InnoDB COMMENT='文件记录表';

drop table if exists `file_url`;
CREATE TABLE `file_url` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
    `uid` char(26) NOT NULL COMMENT '文件uid',
    `attach` char(10) DEFAULT NULL COMMENT '文件扩展，主要用来区分缩略图，为空表示原文件，图片为 thumb，视频为 cover',
    `url` char(100) NOT NULL COMMENT '文件url',
    `url_query_params` varchar(300) NOT NULL COMMENT '文件url访问参数',
    `expire_at` bigint(13) NOT NULL COMMENT '到期时间戳',
    `create_at` bigint(13) NOT NULL COMMENT '创建时间戳',
    `deleted` BOOLEAN NOT NULL DEFAULT FALSE COMMENT '删除标记',
    `version` bigint(13) NOT NULL COMMENT '版本',
    PRIMARY KEY (`id`),
    KEY `i_uid` (`uid`)
) ENGINE=InnoDB COMMENT='文件访问url记录表';

drop table if exists `credential`;
CREATE TABLE `credential` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
    `access_key` char(16) NOT NULL COMMENT 'access key',
    `access_secret` char(32) NOT NULL COMMENT 'access secret',
    `algorithm` char(32) DEFAULT 'md5' COMMENT '签名算法，默认 md5',
    `endpoint` varchar(128) DEFAULT NULL COMMENT 'oss endpoint',
    `region` varchar(64) DEFAULT NULL COMMENT 'oss region',
    `bucket` varchar(64) NOT NULL COMMENT 'oss 桶',
    `oss_vendor` varchar(16) NOT NULL COMMENT 'oss 厂商',
    `oss_access_key` varchar(32) NOT NULL COMMENT 'oss access key',
    `oss_secret_key` varchar(32) NOT NULL COMMENT 'oss secret key',
    `create_at` bigint(13) NOT NULL COMMENT '创建时间戳',
    `modify_at` bigint(13) DEFAULT NULL COMMENT '修改时间戳',
    `deleted` BOOLEAN NOT NULL DEFAULT FALSE COMMENT '删除标记',
    `version` bigint(13) NOT NULL COMMENT '版本',
    PRIMARY KEY (`id`),
    UNIQUE KEY `i_key` (`access_key`)
) ENGINE=InnoDB;