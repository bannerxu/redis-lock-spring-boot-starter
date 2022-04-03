package top.banner.lib.expression;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.lang.reflect.Method;

/**
 * 默认key 抽取， 优先根据 spel 处理
 */
public class ExpressionResolver implements KeyResolver {

    private static final SpelExpressionParser PARSER = new SpelExpressionParser();

    private static final LocalVariableTableParameterNameDiscoverer DISCOVERER = new LocalVariableTableParameterNameDiscoverer();

    @Override
    public String resolver(Method method, String spEL, JoinPoint point) {
        final String[] params = DISCOVERER.getParameterNames(method);
        if (params == null || params.length == 0) {
            return spEL;
        }
        final Object[] args = point.getArgs();
        StandardEvaluationContext context = new MethodBasedEvaluationContext(point.getTarget(), method, args, DISCOVERER);
        for (int i = 0; i < params.length; i++) {
            context.setVariable(params[i], args[i]);
        }
        return PARSER.parseExpression(spEL).getValue(context, String.class);

    }

    /**
     * 根据切点解析方法信息
     *
     * @param joinPoint 切点信息
     * @return Method 原信息
     */
    public Method getMethod(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        if (method.getDeclaringClass().isInterface()) {
            try {
                method = joinPoint.getTarget().getClass().getDeclaredMethod(joinPoint.getSignature().getName(),
                        method.getParameterTypes());
            } catch (SecurityException | NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }
        return method;
    }

}