# redis-lock-spring-boot-starter

支持以 SpEL表达式的方式设置key。

- ## [分布式锁 RedisLock](src/main/java/top/banner/lib/lock/RedisLock.java)

### 使用方式

```java

@Service
public class TestService {
    @RedisLock(name = "lockAdd", key = "'lock'")
    public void lockAdd() {
        TimeUnit.MILLISECONDS.sleep(random.nextInt(100));
        log.info("b => {}", b++);
    }
}

```

```java

@RunWith(SpringRunner.class)
@SpringBootTest
public class RedisLockApplicationTests() {
    @Test
    public void LockAdd() {

        final long l = System.currentTimeMillis();
        final ScheduledExecutorService pool = Executors.newScheduledThreadPool(1000);

        for (int i = 0; i < 1000; i++) {
            pool.submit(() -> {
                try {
                    testService.lockAdd();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
        }
        testService.awaitAfterShutdown(pool);

        log.info("耗时：{}", System.currentTimeMillis() - l);
    }
}
```

### 测试结果

> 无锁情况

```shell
2022-04-04 01:32:39.346  INFO 17564 --- [ol-1-thread-289] t.banner.redislock.service.TestService   : b => 946
2022-04-04 01:32:39.345  INFO 17564 --- [ol-1-thread-622] t.banner.redislock.service.TestService   : b => 944
2022-04-04 01:32:39.348  INFO 17564 --- [ol-1-thread-592] t.banner.redislock.service.TestService   : b => 948
2022-04-04 01:32:39.343  INFO 17564 --- [ol-1-thread-643] t.banner.redislock.service.TestService   : b => 937
2022-04-04 01:32:39.346  INFO 17564 --- [ool-1-thread-17] t.banner.redislock.service.TestService   : b => 945
2022-04-04 01:32:39.345  INFO 17564 --- [ol-1-thread-153] t.banner.redislock.service.TestService   : b => 942
2022-04-04 01:32:39.359  INFO 17564 --- [ol-1-thread-627] t.banner.redislock.service.TestService   : b => 965
2022-04-04 01:32:39.359  INFO 17564 --- [ol-1-thread-662] t.banner.redislock.service.TestService   : b => 964
2022-04-04 01:32:39.359  INFO 17564 --- [ol-1-thread-979] t.banner.redislock.service.TestService   : b => 961
2022-04-04 01:32:39.359  INFO 17564 --- [ol-1-thread-301] t.banner.redislock.service.TestService   : b => 960
2022-04-04 01:32:39.359  INFO 17564 --- [ol-1-thread-666] t.banner.redislock.service.TestService   : b => 963
2022-04-04 01:32:39.359  INFO 17564 --- [ol-1-thread-668] t.banner.redislock.service.TestService   : b => 962
2022-04-04 01:32:39.405  INFO 17564 --- [           main] t.b.redislock.RedisLockApplicationTests  : 耗时：298
```

> 有锁情况

```shell
2022-04-04 01:31:30.540  INFO 17537 --- [ol-1-thread-969] t.banner.redislock.service.TestService   : b => 986
2022-04-04 01:31:30.549  INFO 17537 --- [ol-1-thread-972] t.banner.redislock.service.TestService   : b => 987
2022-04-04 01:31:30.643  INFO 17537 --- [ol-1-thread-966] t.banner.redislock.service.TestService   : b => 988
2022-04-04 01:31:30.713  INFO 17537 --- [ol-1-thread-965] t.banner.redislock.service.TestService   : b => 989
2022-04-04 01:31:30.780  INFO 17537 --- [ol-1-thread-967] t.banner.redislock.service.TestService   : b => 990
2022-04-04 01:31:30.846  INFO 17537 --- [ol-1-thread-959] t.banner.redislock.service.TestService   : b => 991
2022-04-04 01:31:30.934  INFO 17537 --- [ol-1-thread-986] t.banner.redislock.service.TestService   : b => 992
2022-04-04 01:31:31.015  INFO 17537 --- [ol-1-thread-961] t.banner.redislock.service.TestService   : b => 993
2022-04-04 01:31:31.087  INFO 17537 --- [ol-1-thread-978] t.banner.redislock.service.TestService   : b => 994
2022-04-04 01:31:31.178  INFO 17537 --- [ol-1-thread-977] t.banner.redislock.service.TestService   : b => 995
2022-04-04 01:31:31.282  INFO 17537 --- [ol-1-thread-956] t.banner.redislock.service.TestService   : b => 996
2022-04-04 01:31:31.368  INFO 17537 --- [ol-1-thread-971] t.banner.redislock.service.TestService   : b => 997
2022-04-04 01:31:31.437  INFO 17537 --- [ol-1-thread-988] t.banner.redislock.service.TestService   : b => 998
2022-04-04 01:31:31.491  INFO 17537 --- [ool-1-thread-97] t.banner.redislock.service.TestService   : b => 999
2022-04-04 01:31:31.493  INFO 17537 --- [           main] t.b.redislock.RedisLockApplicationTests  : 耗时：57903
```

- ## [幂等锁 Idempotent](src/main/java/top/banner/lib/idempotent/Idempotent.java)

🤔 注意事项：尽量将`key`设置为能够表示用户身份的参数上。否则可能变成全局限制

### 使用方式

```java

@RestController
public class DemoController {
    @GetMapping("/get")
    @Idempotent(key = "#p0", expireTime = 3, info = "请勿重复查询")
    public String get5(String key) throws Exception {
        Thread.sleep(2000L);
        return "success";
    }
}
```

### 测试结果

使用JMeter进行测试
![https://image.xuguoliang.top/2022/04/04/V5V208_ORj5Va.png](https://image.xuguoliang.top/2022/04/04/V5V208_ORj5Va.png)
![https://image.xuguoliang.top/2022/04/04/GKLPsK_nQ8RyH.png](https://image.xuguoliang.top/2022/04/04/GKLPsK_nQ8RyH.png)
