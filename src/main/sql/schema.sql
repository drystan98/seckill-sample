-- 数据酷初始化脚本

-- 创建数据库
CREATE DATABASE seckill;

-- 使用数据库
use seckill;

-- 创建秒杀库存表
create table seckill(
`seckill_id` bigint NOT NULL AUTO_INCREMENT COMMENT '商品库存id',
`name` varchar(120) NOT NULL COMMENT '商品名称',
`number` int NOT NULL COMMENT '库存数量',
`start_time` timestamp NOT NULL COMMENT '秒杀开启时间',
`end_time` timestamp NOT NULL COMMENT '秒杀结束时间',
`create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
PRIMARY KEY (seckill_id),
KEY idx_start_time(start_time),
KEY idx_end_time(end_time),
KEY idx_create_time(create_time)
)ENGINE=InnoDB AUTO_INCREMENT=1000 DEFAULT CHARSET=utf8 COMMENT='秒杀库存表';
-- InnoDB支持事务，AUTO_INCREMENT初始自增

-- 初始化数据
insert into seckill(name, number, start_time, end_time)
values
    ('1000元秒杀iphone6',100,'2015-11-01 00:00:00','2015-11-02 00:00:00'),
    ('500元秒杀ipad2',200,'2015-11-01 00:00:00','2015-11-02 00:00:00'),
    ('300元秒杀小米4',300,'2015-11-01 00:00:00','2015-11-02 00:00:00'),
    ('400元秒杀红米Note',400,'2015-11-01 00:00:00','2015-11-02 00:00:00');

-- 秒杀成功明细表
-- 用户登录认证相关的信息
create table success_killed(
`seckill_id` bigint not null comment '秒杀商品id',
`user_phone` bigint not null comment '用户手机号',
`state` tinyint not null default -1 comment '状态标识:-1：无效  0：成功  1：已付款  2：已发货',
`create_time` timestamp not null default CURRENT_TIMESTAMP comment '创建时间',
PRIMARY KEY (seckill_id ,user_phone), /*联合主键*/
key idx_create_time(create_time)
)ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='秒杀成功明细表';

-- 删除表
drop table seckill;
drop table success_killed;

-- 连接数据库控制台
-- mysql -uroot -p20194680

-- 为什么手写？
-- 记录每次上线的DDL修改
-- 上线V1.1
ALTER TABLE seckill
DROP INDEX idx_create_time,
ADD INDEX idx_c_s(start_time,create_time);

-- 上线V1.2
-- ddl
