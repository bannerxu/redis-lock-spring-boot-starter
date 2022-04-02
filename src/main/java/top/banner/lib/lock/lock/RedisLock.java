package top.banner.lib.lock.lock;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * 使用redis进行分布式锁
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RedisLock {

    /**
     * redis锁 名字
     */
    String name() default "";

    /**
     * redis锁 key 支持spel表达式
     */
    String key() default "";

    /**
     * 过期秒数,默认为30秒
     *
     * @return 轮询锁的时间
     */
    int expire() default 30;

    /**
     * 超时时间单位
     *
     * @return 秒
     */
    TimeUnit timeUnit() default TimeUnit.SECONDS;
}
