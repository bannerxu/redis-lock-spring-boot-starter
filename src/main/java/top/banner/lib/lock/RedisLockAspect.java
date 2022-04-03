package top.banner.lib.lock;


import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import top.banner.lib.expression.KeyResolver;

import java.lang.reflect.Method;

/**
 * @author lgh
 */
@Aspect
public class RedisLockAspect {
    private static final Logger log = LoggerFactory.getLogger(RedisLockAspect.class);


    private static final String REDISSON_LOCK_PREFIX = "redisson_lock:";
    private static final String COLON = ":";

    @Autowired
    @Qualifier("defaultRedisson")
    private RedissonClient redissonClient;

    @Autowired
    @Qualifier("idempotentKeyResolver")
    private KeyResolver keyResolver;


    @Around("@annotation(redisLock)")
    public Object around(ProceedingJoinPoint joinPoint, RedisLock redisLock) throws Throwable {
        String spEL = redisLock.key();
        String lockName = redisLock.name();

        final String key = getRedisKey(joinPoint, lockName, spEL);
        RLock rLock = redissonClient.getLock(key);

        rLock.lock(redisLock.expire(), redisLock.timeUnit());

        Object result;
        try {
            //执行方法
            result = joinPoint.proceed();

        } finally {
            rLock.unlock();
        }
        return result;
    }

    /**
     * 将spel表达式转换为字符串
     *
     * @param joinPoint 切点
     * @return redisKey
     */
    private String getRedisKey(ProceedingJoinPoint joinPoint, String lockName, String spEL) {
        final Method method = keyResolver.getMethod(joinPoint);
        final String resolver = keyResolver.resolver(method, spEL, joinPoint);
        return REDISSON_LOCK_PREFIX + (lockName.equals("") ? method.getName() : lockName) + COLON + resolver;
    }

}