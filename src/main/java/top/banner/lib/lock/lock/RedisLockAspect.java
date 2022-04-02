package top.banner.lib.lock.lock;


import com.oracle.tools.packager.Log;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import javax.annotation.Resource;
import java.lang.reflect.Method;

/**
 * @author lgh
 */
@Aspect
public class RedisLockAspect {
    private static final Logger log = LoggerFactory.getLogger(RedisLockAspect.class);


    private static final String REDISSON_LOCK_PREFIX = "redisson_lock:";
    private static final String COLON = ":";
    @Resource
    private RedissonClient redissonClient;

    /**
     * 支持 #p0 参数索引的表达式解析
     *
     * @param rootObject 根对象,method 所在的对象
     * @param spEL       表达式
     * @param method     ，目标方法
     * @param args       方法入参
     * @return 解析后的字符串
     */
    public static String parse(Object rootObject, String spEL, Method method, Object[] args) {
        if (StringUtils.isBlank(spEL)) {
            return "";
        }
        //获取被拦截方法参数名列表(使用Spring支持类库)
        LocalVariableTableParameterNameDiscoverer u =
                new LocalVariableTableParameterNameDiscoverer();
        String[] paraNameArr = u.getParameterNames(method);
        if (paraNameArr == null || paraNameArr.length == 0) {
            return spEL;
        }
        //使用SPEL进行key的解析
        ExpressionParser parser = new SpelExpressionParser();
        //SPEL上下文
        StandardEvaluationContext context = new MethodBasedEvaluationContext(rootObject, method, args, u);
        //把方法参数放入SPEL上下文中
        for (int i = 0; i < paraNameArr.length; i++) {
            context.setVariable(paraNameArr[i], args[i]);
        }
        return parser.parseExpression(spEL).getValue(context, String.class);
    }

    @Around("@annotation(redisLock)")
    public Object around(ProceedingJoinPoint joinPoint, RedisLock redisLock) throws Throwable {
        log.info("...");
        String spEL = redisLock.key();
        String lockName = redisLock.name();

        RLock rLock = redissonClient.getLock(getRedisKey(joinPoint, lockName, spEL));

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
    private String getRedisKey(ProceedingJoinPoint joinPoint, String lockName, String spel) {
        Signature signature = joinPoint.getSignature();
        MethodSignature methodSignature = (MethodSignature) signature;
        Method targetMethod = methodSignature.getMethod();
        Object target = joinPoint.getTarget();
        Object[] arguments = joinPoint.getArgs();
        return REDISSON_LOCK_PREFIX + (lockName.equals("") ? targetMethod.getName() : lockName) + COLON + parse(target, spel, targetMethod, arguments);
    }

}