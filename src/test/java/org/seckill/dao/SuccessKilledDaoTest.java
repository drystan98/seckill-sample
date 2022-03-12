package org.seckill.dao;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.seckill.entity.SuccessKilled;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

import static org.junit.Assert.*;
/**
 * 配置spring与junit整合，为了junit启动时加载springIOC容器
 * spring-test,jnit
 */
@RunWith(SpringJUnit4ClassRunner.class)
//告诉junit spring 配置文件
@ContextConfiguration({"classpath:spring/spring-dao.xml"})
public class SuccessKilledDaoTest {

    @Resource
    private SuccessKilledDao successKilledDao;

    @Test
    public void insertSuccessKilled() {
        long id=1001L;
        long phone=15643114410L;
        int insertCount = successKilledDao.insertSuccessKilled(id,phone);
        System.out.println("insertCount="+insertCount);

        /**
         * 第一次测试通过
         *22:17:59.744 [main] DEBUG o.m.s.t.SpringManagedTransaction - JDBC Connection [com.mchange.v2.c3p0.impl.NewProxyConnection@304bb45b] will not be managed by Spring
         * 22:17:59.766 [main] DEBUG o.s.d.S.insertSuccessKilled - ==>  Preparing: insert ignore into success_killed(seckill_id,user_phone) values (?,?);
         * 22:17:59.824 [main] DEBUG o.s.d.S.insertSuccessKilled - ==> Parameters: 1000(Long), 15643114410(Long)
         * 22:17:59.967 [main] DEBUG o.s.d.S.insertSuccessKilled - <==    Updates: 1
         * 22:17:59.980 [main] DEBUG org.mybatis.spring.SqlSessionUtils - Closing non transactional SqlSession [org.apache.ibatis.session.defaults.DefaultSqlSession@3f191845]
         * insertCount=1
         */

        /**
         * 第二次测试通过
         * 22:19:29.797 [main] DEBUG o.m.s.t.SpringManagedTransaction - JDBC Connection [com.mchange.v2.c3p0.impl.NewProxyConnection@304bb45b] will not be managed by Spring
         * 22:19:29.821 [main] DEBUG o.s.d.S.insertSuccessKilled - ==>  Preparing: insert ignore into success_killed(seckill_id,user_phone) values (?,?);
         * 22:19:29.879 [main] DEBUG o.s.d.S.insertSuccessKilled - ==> Parameters: 1000(Long), 15643114410(Long)
         * 22:19:29.882 [main] DEBUG o.s.d.S.insertSuccessKilled - <==    Updates: 0
         * 22:19:29.902 [main] DEBUG org.mybatis.spring.SqlSessionUtils - Closing non transactional SqlSession [org.apache.ibatis.session.defaults.DefaultSqlSession@3f191845]
         * insertCount=0
         */
    }

    @Test
    public void queryByIdWithSeckill() {
        long id=1001L;
        long phone=15643114410L;
        SuccessKilled successKilled = successKilledDao.queryByIdWithSeckill(id,phone);
        System.out.println(successKilled);
        System.out.println(successKilled.getSeckill());

        /**
         * 错误 create_time没有默认值
         * org.springframework.dao.TransientDataAccessResourceException:
         * Error attempting to get column 'create_time' from result set.  Cause: java.sql.SQLException: Zero date value prohibited
         *          * ; SQL []; Zero date value prohibited; nested exception is java.sql.SQLException: Zero date value prohibited
         */

        /**
         * 测试1通过
         * 22:29:50.446 [main] DEBUG o.m.s.t.SpringManagedTransaction - JDBC Connection [com.mchange.v2.c3p0.impl.NewProxyConnection@4a8355dd] will not be managed by Spring
         * 22:29:50.473 [main] DEBUG o.s.d.S.queryByIdWithSeckill - ==>  Preparing: select sk.seckill_id, sk.user_phone, sk.create_time, sk.state, s.seckill_id "seckill.seckill_id", s.name "seckill.name", s.number "seckill.number", s.start_time "seckill.start_time", s.end_time "seckill.end_time", s.create_time "seckill.create_time" from success_killed sk inner join seckill s on sk.seckill_id = s.seckill_id where sk.seckill_id = ?;
         * 22:29:50.518 [main] DEBUG o.s.d.S.queryByIdWithSeckill - ==> Parameters: 1000(Long)
         * 22:29:50.557 [main] DEBUG o.s.d.S.queryByIdWithSeckill - <==      Total: 1
         * 22:29:50.567 [main] DEBUG org.mybatis.spring.SqlSessionUtils - Closing non transactional SqlSession [org.apache.ibatis.session.defaults.DefaultSqlSession@3f191845]
         * SuccessKilled{seckillId=1000, userPhone=15643114410, state=-1, createTime=Tue Nov 05 22:29:41 CST 2019}
         * Seckill{seckillId=1000, name='1000元秒杀iphone6', number=100, startTime=Sun Nov 01 00:00:00 CST 2015, endTime=Mon Nov 02 00:00:00 CST 2015, createTime=Tue Nov 05 09:38:53 CST 2019}
         */

        /**
         * 测试2通过
         * 22:32:14.609 [main] DEBUG o.m.s.t.SpringManagedTransaction - JDBC Connection [com.mchange.v2.c3p0.impl.NewProxyConnection@4a8355dd] will not be managed by Spring
         * 22:32:14.630 [main] DEBUG o.s.d.S.queryByIdWithSeckill - ==>  Preparing: select sk.seckill_id, sk.user_phone, sk.create_time, sk.state, s.seckill_id "seckill.seckill_id", s.name "seckill.name", s.number "seckill.number", s.start_time "seckill.start_time", s.end_time "seckill.end_time", s.create_time "seckill.create_time" from success_killed sk inner join seckill s on sk.seckill_id = s.seckill_id where sk.seckill_id = ?;
         * 22:32:14.688 [main] DEBUG o.s.d.S.queryByIdWithSeckill - ==> Parameters: 1001(Long)
         * 22:32:14.726 [main] DEBUG o.s.d.S.queryByIdWithSeckill - <==      Total: 1
         * 22:32:14.737 [main] DEBUG org.mybatis.spring.SqlSessionUtils - Closing non transactional SqlSession [org.apache.ibatis.session.defaults.DefaultSqlSession@3f191845]
         * SuccessKilled{seckillId=1001, userPhone=15643114410, state=0, createTime=Tue Nov 05 22:32:05 CST 2019}
         * Seckill{seckillId=1001, name='500元秒杀ipad2', number=200, startTime=Sun Nov 01 00:00:00 CST 2015, endTime=Mon Nov 02 00:00:00 CST 2015, createTime=Tue Nov 05 09:38:53 CST 2019}
         */
    }
}