package org.seckill.service.impl;

import org.apache.commons.collections.MapUtils;
import org.seckill.dao.SeckillDao;
import org.seckill.dao.SuccessKilledDao;
import org.seckill.dao.cache.RedisDao;
import org.seckill.dto.Exposer;
import org.seckill.dto.SeckillExecution;
import org.seckill.entity.Seckill;
import org.seckill.entity.SuccessKilled;
import org.seckill.enums.SeckillStatEnum;
import org.seckill.exception.RepeatKillException;
import org.seckill.exception.SeckillCloseException;
import org.seckill.exception.SeckillException;
import org.seckill.service.SeckillService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//@Component @Service @Dao @Controller
@Service
public class SeckillServiceImpl implements SeckillService {

    //slf4j日志
    private Logger logger= LoggerFactory.getLogger(this.getClass());

    //在spring中获取dao的实例，注入service依赖
    @Autowired  //@Resource @Inject
    private SeckillDao seckillDao;

    @Autowired
    private SuccessKilledDao successKilledDao;

    @Autowired
    private RedisDao  redisDao;

    //md5盐值字符串，用于混淆md5
    private final String slat="ajsda;sas98da9sd0acjxkc";

    public List<Seckill> getSeckillList() {
        return seckillDao.queryAll(0,4);
    }

    public Seckill getById(long seckillId) {
        return seckillDao.queryById(seckillId);
    }

    /**
     *
     * 输出秒杀接口地址
     * 秒杀开启时输出接口地址，否则输出系统时间和秒杀时间
     * @param seckillId
     */
    public Exposer exportSeckillUrl(long seckillId) {
        //Seckill seckill=seckillDao.queryById(seckillId);
        //优化点：缓存优化,要把缓存优化放在dao层,超时的基础上维护一致性
        /**
         * 伪代码
         * get from cache
         * if null
         *      get db
         * else
         *      put cache
         * locgoin
         */
        //1:访问redis
        Seckill seckill = redisDao.getSeckill(seckillId);
        if(seckill == null){
            //2:访问数据库
            seckill=seckillDao.queryById(seckillId);
            if(seckill == null){
                return new Exposer(false,seckillId);
            }else {
                //3：放入redis
                redisDao.putSeckill(seckill);
            }
        }
        if(seckill==null){
            return new Exposer(false,seckillId);
        }
        //获取秒杀对象的开始结束时间
        Date startTime=seckill.getStartTime();
        Date endTime=seckill.getEndTime();
        //系统当前时间
        Date nowTime=new Date();
        if(startTime.getTime() > nowTime.getTime()    //开始时间大于当前时间（没有开始）
            || endTime.getTime() < nowTime.getTime()){  //结束时间小于当前时间（已经结束）
            return new Exposer(false,seckillId,nowTime.getTime(),startTime.getTime(),endTime.getTime());
        }
        //转化特定字符串的过程，不可逆
        String md5=getMD5(seckillId);
        return new Exposer(true,md5,seckillId);
    }

    private String getMD5(long seckillId){
        String base=seckillId+"/"+slat;
        //利用spring工具类生成md5
        String md5= DigestUtils.md5DigestAsHex(base.getBytes());
        return md5;
    }

    @Transactional
    /**
     * 使用注解控制事务方法的优点：
     * 1、开发团队达成一致，明确标注十五方法的编程风格。
     * 2、保证事务方法的执行时间尽可能短，不要穿插其他的网络操作 RPC/HTTP请求/或者 剥离到事务方法外部
     * 3、不是所有的方法都需要事务，如只有一条修改操作或者只读操作不需要事务控制
     */
    /**
     * 执行秒杀操作
     */
    public SeckillExecution executeSeckill(long seckillId, long userPhone, String md5)
            throws SeckillException, RepeatKillException, SeckillCloseException {
        if(md5==null || !md5.equals(getMD5(seckillId))){  //md5保证唯一性，防止修改秒杀数据
            throw new SeckillException("seckill data rewrite");
        }
            //执行秒杀逻辑：减库存+记录购买行为
            Date nowTime=new Date();
        try{
            //2、记录购买行为
            int insertCount=successKilledDao.insertSuccessKilled(seckillId,userPhone);
            //唯一验证：seckillId与userPhone
            if(insertCount<=0){
                //重复秒杀异常
                throw new RepeatKillException("seckill repeated");
            }else{
                //1、减库存，热点商品竞争
                int updateCount=seckillDao.reduceNumber(seckillId,nowTime);
                if(updateCount <= 0){
                    //没有更新到记录，秒杀结束，rollback回滚
                    throw new SeckillCloseException("seckill is close");
                }else {
                    //秒杀成功，commit提交
                    SuccessKilled successKilled=successKilledDao.queryByIdWithSeckill(seckillId,userPhone);
                    //状态信息用户数据字典
                    //return new SeckillExecution(seckillId,1,"秒杀成功",successKilled);
                    return new SeckillExecution(seckillId, SeckillStatEnum.SUCCESS,successKilled);
                }
            }
        }catch (SeckillCloseException e1){
            throw e1;
        }catch (RepeatKillException e2){
            throw e2;
        }catch (SeckillException e3){
            throw e3;
        }catch (Exception e){
            logger.error(e.getMessage(),e);
            //所有编译器异常转化为运行期异常，spring声明式事务会回滚rollback
            throw new SeckillException("seckill inner error "+e.getMessage());
        }

    }

    /**
     * 执行秒杀操作by存储过程
     * @param seckillId
     * @param userPhone
     * @param md5
     * @return
     * @throws SeckillException
     * @throws RepeatKillException
     * @throws SeckillCloseException
     */
    public SeckillExecution executeSeckillProcedure(long seckillId, long userPhone, String md5) {
        if(md5 == null || !md5.equals(getMD5(seckillId))){
            return new SeckillExecution(seckillId,SeckillStatEnum.DATA_REWRITE);
        }
        Date killTime = new Date();
        Map<String,Object> map=new HashMap<String,Object>();
        map.put("seckillId",seckillId);
        map.put("phone",userPhone);
        map.put("killTime",killTime);
        map.put("result",null);
        //执行存储过程，result被赋值
        try{
            seckillDao.killProcedure(map);  //使用存储过程执行秒杀
            //获取result
            int result = MapUtils.getInteger(map,"result",-2);
            if(result == 1){
                SuccessKilled sk=successKilledDao.queryByIdWithSeckill(seckillId,userPhone);
                return new SeckillExecution(seckillId,SeckillStatEnum.SUCCESS,sk);
            }else{
                return new SeckillExecution(seckillId,SeckillStatEnum.stateOf(result));
            }
        }catch (Exception e){
            logger.error(e.getMessage(),e);
            return new SeckillExecution(seckillId,SeckillStatEnum.INNER_ERROE);
        }
    }
}
