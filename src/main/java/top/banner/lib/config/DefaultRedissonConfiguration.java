package top.banner.lib.config;

import io.netty.channel.nio.NioEventLoopGroup;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.Codec;
import org.redisson.config.Config;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ClassUtils;
import top.banner.lib.expression.ExpressionResolver;
import top.banner.lib.expression.KeyResolver;
import top.banner.lib.idempotent.IdempotentAspect;
import top.banner.lib.lock.RedisLockAspect;

import javax.annotation.Resource;

@Configuration
@EnableConfigurationProperties(DefaultRedissonProperties.class)
@AutoConfigureAfter(RedisAutoConfiguration.class)
public class DefaultRedissonConfiguration {

    @Resource
    private DefaultRedissonProperties properties;

    /**
     * redissonClient
     */
    @Bean(name = "defaultRedisson", destroyMethod = "shutdown")
    public RedissonClient defaultRedisson() throws Exception {
        Config config = new Config();

        config.useSingleServer().setAddress("redis://" + properties.getHost() + ":" + properties.getPort())
                .setConnectionMinimumIdleSize(24)
                .setConnectionPoolSize(64)
                .setDatabase(properties.getDatabase())
                .setDnsMonitoringInterval(500)
                .setSubscriptionConnectionMinimumIdleSize(1)
                .setSubscriptionConnectionPoolSize(50)
                .setSubscriptionsPerConnection(5)
                .setClientName("RedisLock")
                .setRetryAttempts(3)
                .setRetryInterval(1500)
                .setTimeout(3000)
                .setConnectTimeout(10000)
                .setIdleConnectionTimeout(10000)
                .setPassword(properties.getPassword());
        Codec codec = (Codec) ClassUtils.forName("org.redisson.codec.JsonJacksonCodec", ClassUtils.getDefaultClassLoader()).newInstance();
        config.setCodec(codec);
        config.setThreads(2);
        config.setEventLoopGroup(new NioEventLoopGroup());
        return Redisson.create(config);
    }

    @Bean
    public RedisLockAspect redisLockAspect() {
        return new RedisLockAspect();
    }

    /**
     * 切面 拦截处理所有 @Idempotent
     *
     * @return Aspect
     */
    @Bean
    public IdempotentAspect idempotentAspect() {
        return new IdempotentAspect();
    }

    /**
     * key 解析器
     *
     * @return KeyResolver
     */
    @Bean(name = "idempotentKeyResolver")
    @ConditionalOnMissingBean(KeyResolver.class)
    public KeyResolver keyResolver() {
        return new ExpressionResolver();
    }
}
