package org.seckill.dao;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.seckill.entity.Seckill;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

/**
 * 配置spring与junit整合，为了junit启动时加载springIOC容器
 * spring-test,jnit
 */
@RunWith(SpringJUnit4ClassRunner.class)
//告诉junit spring 配置文件
@ContextConfiguration({"classpath:spring/spring-dao.xml"})
public class SeckillDaoTest {

    //注入Dao实现类依赖
    @Resource
    private SeckillDao seckillDao;

    @Test
    public void queryById() {
        long id=1000;
        Seckill seckill=seckillDao.queryById(id);
        System.out.println(seckill.getName());
        System.out.println(seckill);
        /**
         * 测试通过
         * 1000元秒杀iphone6
         * Seckill{
         * seckillId=1000,
         * name='1000元秒杀iphone6',
         * number=100,
         * startTime=Sun Nov 01 00:00:00 CST 2015,
         * endTime=Mon Nov 02 00:00:00 CST 2015,
         * createTime=Tue Nov 05 09:38:53 CST 2019}
         */
    }

    @Test
    public void queryAll() {
        //java没有保存形参的记录：queryAll(int offset,int limit) -> queryAll(arg0,arg1)
        //告诉mybatis参数的名称
        List<Seckill> seckills=seckillDao.queryAll(0,100);
        for(Seckill  seckill : seckills){
            System.out.println(seckill);
        }
        /**
         * 错误
         *org.mybatis.spring.MyBatisSystemException:
         *  nested exception is org.apache.ibatis.binding.BindingException:
         *  Parameter 'offset' not found. Available parameters are [0, 1, param1, param2]
         */

        /**
         * 测试通过
         *Seckill{seckillId=1000, name='1000元秒杀iphone6', number=100, startTime=Sun Nov 01 00:00:00 CST 2015, endTime=Mon Nov 02 00:00:00 CST 2015, createTime=Tue Nov 05 09:38:53 CST 2019}
         * Seckill{seckillId=1001, name='500元秒杀ipad2', number=200, startTime=Sun Nov 01 00:00:00 CST 2015, endTime=Mon Nov 02 00:00:00 CST 2015, createTime=Tue Nov 05 09:38:53 CST 2019}
         * Seckill{seckillId=1002, name='300元秒杀小米4', number=300, startTime=Sun Nov 01 00:00:00 CST 2015, endTime=Mon Nov 02 00:00:00 CST 2015, createTime=Tue Nov 05 09:38:53 CST 2019}
         * Seckill{seckillId=1003, name='400元秒杀红米Note', number=400, startTime=Sun Nov 01 00:00:00 CST 2015, endTime=Mon Nov 02 00:00:00 CST 2015, createTime=Tue Nov 05 09:38:53 CST 2019}
         */
    }

    @Test
    public void reduceNumber() {
        Date killTime=new Date();
        int updateCount = seckillDao.reduceNumber(1000L,killTime);
        System.out.println("updateCount="+updateCount);
        /**
         * 错误
         * org.mybatis.spring.MyBatisSystemException: nested exception is org.apache.ibatis.binding.BindingException: Parameter 'seckillId' not found. Available parameters are [0, 1, param1, param2]
         */

        /**
         * 测试通过
         *22:12:03.346 [main] DEBUG o.m.s.t.SpringManagedTransaction - JDBC Connection [com.mchange.v2.c3p0.impl.NewProxyConnection@304bb45b] will not be managed by Spring
         * 22:12:03.365 [main] DEBUG o.s.dao.SeckillDao.reduceNumber - ==>  Preparing: update seckill set number =number -1 where seckill_id = ? and start_time <= ? and end_time >= ? and number > 0;
         * 22:12:03.461 [main] DEBUG o.s.dao.SeckillDao.reduceNumber - ==> Parameters: 1000(Long), 2019-11-05 22:12:02.857(Timestamp), 2019-11-05 22:12:02.857(Timestamp)
         * 22:12:03.493 [main] DEBUG o.s.dao.SeckillDao.reduceNumber - <==    Updates: 0
         * 22:12:03.493 [main] DEBUG org.mybatis.spring.SqlSessionUtils - Closing non transactional SqlSession [org.apache.ibatis.session.defaults.DefaultSqlSession@3f191845]
         * updateCount=0
         */
    }


}