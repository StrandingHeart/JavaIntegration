/*
 Navicat Premium Data Transfer

 Source Server Type    : MySQL
 Source Server Version : 50725
 Source Schema         : test

 Target Server Type    : MySQL
 Target Server Version : 50725
 File Encoding         : 65001

 Date: 18/07/2020 14:07:09
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for test
-- ----------------------------
DROP TABLE IF EXISTS `test`;
CREATE TABLE `test` (
  `id` int(8) NOT NULL AUTO_INCREMENT,
  `name` varchar(32) DEFAULT NULL,
  `password` varchar(255) DEFAULT NULL,
  `hobby` varchar(255) DEFAULT NULL,
  `age` tinyint(3) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of test
-- ----------------------------
BEGIN;
INSERT INTO `test` VALUES (1, 'qwe', '111', '111', 1);
INSERT INTO `test` VALUES (2, 'zhangyong2', 'pass', 'hobby', 23);
INSERT INTO `test` VALUES (3, 'zhangyong2', 'pass', 'hobby', 23);
INSERT INTO `test` VALUES (4, 'zhangyong2', 'pass', 'hobby', 23);
INSERT INTO `test` VALUES (5, 'zhangyong2', 'pass', 'hobby', 23);
INSERT INTO `test` VALUES (6, 'zhangyong2', 'pass', 'hobby', 23);
COMMIT;

SET FOREIGN_KEY_CHECKS = 1;
