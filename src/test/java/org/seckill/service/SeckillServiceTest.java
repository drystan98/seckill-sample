package org.seckill.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.seckill.dto.Exposer;
import org.seckill.dto.SeckillExecution;
import org.seckill.entity.Seckill;
import org.seckill.exception.RepeatKillException;
import org.seckill.exception.SeckillCloseException;
import org.seckill.exception.SeckillException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({
        "classpath:spring/spring-dao.xml",
        "classpath:spring/spring-service.xml"})
public class SeckillServiceTest {

    private final Logger logger= LoggerFactory.getLogger(this.getClass());

    @Autowired
    private SeckillService seckillService;

    @Test
    public void getSeckillList() {
        List<Seckill> list=seckillService.getSeckillList();
        logger.info("list={}",list);
    }

    @Test
    public void getById() {
        long id=1000L;
        Seckill seckill=seckillService.getById(id);
        logger.info("seckill={}",seckill);
    }

    @Test
    public void exportSeckillUrl() {
        long id=1000L;
        Exposer exposer=seckillService.exportSeckillUrl(id);
        logger.info("exposer={}",exposer);

        /**
         * Exposer{
         * exposed=true,
         * md5='c112fd8e3b439da10ab433839c23ef6b',
         * seckillId=1000,
         * now=0, start=0, end=0}
         */
    }

    @Test
    public void executeSeckill() {
        long id=1000L;
        long phone=16643114100L;
        String md5="c112fd8e3b439da10ab433839c23ef6b";
        SeckillExecution execution=seckillService.executeSeckill(id,phone,md5);
        logger.info("result={}",execution);

        /**
         * result=SeckillExecution{
         * seckillId=1000,
         * state=1,
         * stateInfo='秒杀成功',
         * successKilled=
         * SuccessKilled{
         * seckillId=1000,
         * userPhone=16643114400,
         * state=0,
         * createTime=Wed Nov 06 09:12:45 CST 2019}}
         */
    }

    //测试代码完整逻辑，可重复执行
    //集成测试的完整性
    @Test
    public void testSeckillLogic() throws Exception{
        long id=1001;
        Exposer exposer=seckillService.exportSeckillUrl(id);
        if(exposer.isExposed()){
            logger.info("exposer={}",exposer);
            long phone=13562911232L;
            String md5=exposer.getMd5();
            try{
                SeckillExecution execution=seckillService.executeSeckill(id,phone,md5);
                /**
                 * 秒杀成功
                 * result=SeckillExecution{seckillId=1000, state=1, stateInfo='秒杀成功', successKilled=SuccessKilled{seckillId=1000, userPhone=13562911232, state=0, createTime=Wed Nov 06 09:35:08 CST 2019}}
                 */
                logger.info("result={}",execution);
            }catch (RepeatKillException e){
                /**
                 *  seckill repeated
                 */
                logger.info(e.getMessage());
            }catch (SeckillCloseException e){
                /**
                 * 秒杀关闭
                 *  exposer=Exposer{exposed=false, md5='null', seckillId=1002, now=1573004263998, start=1572796800000, end=1572883200000}
                 */
                logger.info(e.getMessage());
            }
        }else{
            //秒杀未开启
            /**
             * exposer=Exposer{exposed=false, md5='null', seckillId=1003, now=1573004062431, start=1573142400000, end=1573833600000}
             */
            logger.warn("exposer={}",exposer);
        }
    }

    @Test
    public void executeSeckillProcedure(){
        long seckillId=1002;
        long phone=15643114555L;
        Exposer exposer = seckillService.exportSeckillUrl(seckillId);
        if(exposer.isExposed()){
            String md5 = exposer.getMd5();
            SeckillExecution execution = seckillService.executeSeckillProcedure(seckillId,phone,md5);
            logger.info(execution.getStateInfo());
        }
    }
}