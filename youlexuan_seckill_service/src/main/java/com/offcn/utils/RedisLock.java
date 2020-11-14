package com.offcn.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class RedisLock {

    @Autowired
    private RedisTemplate redisTemplate;



    /**
     * 获取锁
     * @param key  锁的key
     * @param value 锁的值：时间戳
     * @return
     */
    public boolean lock(String key,String value){

        //setIfAbsent 设置值方法，如果设值成功，会返回一个true  如果设值失败，会返回一个false
        Boolean isset = redisTemplate.opsForValue().setIfAbsent(key, value);
        //如果设值成功，获取到锁
        if(isset){
            return true;
        }

        //首次设值失败，尝试确认锁是否过期了

        //读取锁里面存储值 值存储的是锁的过期时间
      String currentValue = (String) redisTemplate.opsForValue().get(key);

        //判断锁里面存储的值，和当前系统时间比对，看是否失效
        if(currentValue!=null&&Long.valueOf(currentValue)<System.currentTimeMillis()){
            //锁已经失效，尝试重新设值
            //getAndSet 获取原来旧值，设置一个新值
            String oldValue = (String) redisTemplate.opsForValue().getAndSet(key, value);

            //判断oldValue不为空，而且oldValue和currentValue相等，表示获取到了锁
            if(!StringUtils.isEmpty(oldValue)&&oldValue.equals(currentValue)){
                return true;
            }
        }


        return  false;

    }

    //释放锁
    public void unlock(String key,String value){

        try {
            //使用key，从redis获取对应锁的值
            String  currentValue= (String) redisTemplate.opsForValue().get(key);

            //比对要解锁 传递的值和当前锁存储值是否一致
            if(!StringUtils.isEmpty(currentValue)&&currentValue.equals(value)){
                //执行解锁，把rediskey对应值删除
                redisTemplate.opsForValue().getOperations().delete(key);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
