package org.seckill.dao.cache;

import com.dyuproject.protostuff.LinkedBuffer;
import com.dyuproject.protostuff.ProtostuffIOUtil;
import com.dyuproject.protostuff.runtime.RuntimeSchema;
import org.seckill.entity.Seckill;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * Protostuff的使用
 * 基于谷歌Protocal Buffer的序列化库
 * 对象序列化城Protocol Buffer之后可读性差，但是相比xml，json，它占用小，速度快。适合做数据存储或 RPC 数据交换格式。
 * 相对我们常用的json来说，Protocol Buffer门槛更高，因为需要编写.proto文件，再把它编译成目标语言，这样使用起来就很麻烦。
 * 但是现在有了protostuff之后，就不需要依赖.proto文件了，他可以直接对POJO进行序列化和反序列化，使用起来非常简单。
 *
 */

public class RedisDao {

    private final Logger logger= LoggerFactory.getLogger(this.getClass());

    private JedisPool jedisPool;

    public RedisDao(String ip,int port){
        jedisPool = new JedisPool(ip,port);
    }

    //RuntimeSchema是一个高效序列化第三方工具类，比自带的序列化速度快，内存更省
    //存取的时候是通过序列化工具类把对象序列化成一个字节数组，再把这个数据存到redis中
    private RuntimeSchema<Seckill> schema=RuntimeSchema.createFrom(Seckill.class);

    public Seckill getSeckill(long seckillId){      //从redis中获取
        //redis操作逻辑
        try{
            Jedis jedis=jedisPool.getResource();
            try{
                //redis key
                String key = "seckill:"+seckillId;
                //并没有实现内部序列化操作
                //get->byte[]->反序列化->Object(Seckill)
                //采用自定义序列化
                //protostuff:pojo
                byte[] bytes = jedis.get(key.getBytes());  //获取的是字节数组，然后通过schema将字节数组转对象
                //缓存重获取
                if(bytes!=null){
                    /**
                     * 取得时候也是这样，获取出字节数组，在通过序列化工具反序列化到对象中：
                     * 注意：先通过序列化生成一个空对象，然后在进行字节数组反向赋值。
                     */
                    //空对象
                    Seckill seckill=schema.newMessage();
                    ProtostuffIOUtil.mergeFrom(bytes,seckill,schema);
                    //seckill 被反序列化
                    return seckill;
                }
            }finally {
                jedis.close();
            }
        }catch (Exception e){
            logger.error(e.getMessage(),e);
        }
        return null;
    }

    public String putSeckill(Seckill seckill){  //存入redis
        //序列化过程： set Object(Seckill) ->byte[] -> redis
        try{
            Jedis jedis=jedisPool.getResource();
            try {
                String key="seckill:"+seckill.getSeckillId();
                /**
                 *  LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE)   外加一个缓冲区，加快序列化速度
                 *  ProtostuffIOUtil.toByteArray   对象转字节数组
                 */
                byte[] bytes=ProtostuffIOUtil.toByteArray(seckill,schema,
                        LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE));
                int timeout=60*60;//缓存一小时 ， 对象在redis中的生存时间
                String result = jedis.setex(key.getBytes(),timeout,bytes);  //存入Redis
                return result;
            }finally {
                jedis.close();
            }
        }catch (Exception e){
            logger.error(e.getMessage(),e);
        }
        return null;
    }



}
