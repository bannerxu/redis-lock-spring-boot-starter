package top.banner.lib.expression;

import org.aspectj.lang.JoinPoint;

import java.lang.reflect.Method;

/**
 * 唯一标志处理器
 */
public interface KeyResolver {

    /**
     * 解析处理 key
     *
     * @param spEL  spEL表达式
     * @param point 接口切点信息
     * @return 处理结果
     */
    String resolver(Method method, String spEL, JoinPoint point);

    Method getMethod(JoinPoint joinPoint);
}