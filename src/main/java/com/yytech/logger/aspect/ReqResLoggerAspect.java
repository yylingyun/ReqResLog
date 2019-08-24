package com.yytech.logger.aspect;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.yytech.logger.LogAttributes;
import com.yytech.logger.LoggerService;
import com.yytech.logger.annotation.ReqResLog;
import com.yytech.logger.autoconfig.ReqResLogProperties;
import com.yytech.logger.util.ReqResLogUtil;
import com.yytech.logger.util.StringUtil;
import com.yytech.logger.util.TraceIdThreadLocal;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 日志记录切面方式的实现
 */
@Slf4j
@Aspect
public class ReqResLoggerAspect {

    @Autowired
    ReqResLogProperties reqResLogProperties;

    @Autowired
    LoggerService loggerService;

    /**
     * 代理被ReqResLog注释的方法，记录它的ReqResLog
     *
     * @param pjp ProceedingJoinPoint
     * @return 被代理的原方法返回值
     * @throws Throwable 被代理的原方法抛的Throwable
     */
    @Around("@annotation(com.yytech.logger.annotation.ReqResLog)")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        LogAttributes logAttributes = parseLogAttributes(pjp);
        try {
            //处理请求日志
            processReqLog(logAttributes);
            //实际方法调用处理
            Object response = pjp.proceed();
            //处理返回日志
            processResLog(logAttributes, response);
            return response;
        } catch (Throwable throwable) {
            //处理异常日志
            processThrowableLog(logAttributes, throwable);
            throw throwable;
        } finally {
            //释放ttl中的traceId信息
            if (logAttributes != null && logAttributes.isTraceIdEntry()) {
                TraceIdThreadLocal.release();
            }
        }
    }

    /**
     * 处理Req日志
     *
     * @param logAttributes 日志参数
     */
    private void processReqLog(LogAttributes logAttributes) {
        try {
            if (logAttributes == null || !logAttributes.isLogEnable()) {
                return;
            }
            loggerService.processReqLog(logAttributes);
        } catch (Throwable e) {
            log.error("ReqResLoggerAspect processReqLog", e);
        }
    }

    /**
     * 处理Res日志
     *
     * @param logAttributes 日志参数
     * @param response      被代理的原方法返回值
     */
    private void processResLog(LogAttributes logAttributes, Object response) {
        try {
            if (logAttributes == null || !logAttributes.isLogEnable()) {
                return;
            }
            ReqResLog annotation = logAttributes.getReqResLogAnnotation();
            ReqResLogProperties.LogType resLogType = reqResLogProperties.getResLogTypeWithDefault(annotation);
            String resParamLog = toJsonOrString(resLogType, response);
            logAttributes.setResParamLog(resParamLog);
            loggerService.processResLog(logAttributes);
        } catch (Throwable e) {
            log.error("ReqResLoggerAspect processResLog", e);
        }
    }

    /**
     * 处理异常日志
     *
     * @param logAttributes 日志参数
     * @param throwable     被代理的原方法的throwable
     */
    private void processThrowableLog(LogAttributes logAttributes, Throwable throwable) {
        try {
            if (logAttributes == null || !logAttributes.isThrowableLogEnable()) {
                return;
            }
            ReqResLog annotation = logAttributes.getReqResLogAnnotation();
            ReqResLogProperties.ThrowableLogType throwableLogType = reqResLogProperties.getThrowableLogTypeWithDefault(annotation);
            if (throwableLogType == ReqResLogProperties.ThrowableLogType.MESSAGE) {
                logAttributes.setThrowableLog("[" + throwable.getClass().getTypeName() + "]:" + throwable.getMessage());
            } else if (throwableLogType == ReqResLogProperties.ThrowableLogType.STACK) {
                logAttributes.setThrowableLog(ReqResLogUtil.getStackTrace(throwable));
            }
            loggerService.processThrowableLog(logAttributes);
        } catch (Throwable e) {
            log.error("ReqResLoggerAspect processResLog", e);
        }
    }

    /**
     * 解析日志记录可能用到的各个属性
     *
     * @param pjp ProceedingJoinPoint
     * @return 当前可以解析到的日志参数
     */
    private LogAttributes parseLogAttributes(ProceedingJoinPoint pjp) {
        try {
            Class<?> targetClass = pjp.getTarget().getClass();
            Object[] pjpArgs = pjp.getArgs();
            MethodSignature signature = (MethodSignature) pjp.getSignature();
            String methodName = signature.getName();
            Class[] parameterTypes = signature.getParameterTypes();
            String[] parameterNames = signature.getParameterNames();
            Class returnType = signature.getReturnType();
            /*
             * 获取targetClassMethod上的annotation而不是signature.getMethod()的annotation
             * 因为signature.getMethod()的annotation在jdk动态代理的情况下取的是接口上的annotation
             */
            Method targetClassMethod = targetClass.getMethod(methodName, parameterTypes);
            ReqResLog annotation = targetClassMethod.getAnnotation(ReqResLog.class);
            ReqResLogProperties.Level logLevel = reqResLogProperties.getLevelWithDefault(annotation);
            boolean logEnable = loggerService.isLogLevelEnable(logLevel);
            ReqResLogProperties.Level throwableLogLevel = reqResLogProperties.getThrowableLogLevelWithDefault(annotation);
            boolean throwableLogLevelEnable = loggerService.isLogLevelEnable(throwableLogLevel);
            ReqResLogProperties.ThrowableLogType throwableLogType = reqResLogProperties.getThrowableLogTypeWithDefault(annotation);
            boolean throwableLogEnable = throwableLogLevelEnable && (throwableLogType != ReqResLogProperties.ThrowableLogType.NONE);

            //设置日志级别以及可用性
            LogAttributes logAttributes = new LogAttributes();
            logAttributes.setLogLevel(logLevel);
            logAttributes.setThrowableLogLevel(throwableLogLevel);
            logAttributes.setLogEnable(logEnable);
            logAttributes.setThrowableLogEnable(throwableLogEnable);
            logAttributes.setReqResLogAnnotation(annotation);
            if (!logEnable && !throwableLogEnable) {
                return logAttributes;
            }
            //设置当前context的title
            String title = reqResLogProperties.getTitleWithDefault(annotation);
            logAttributes.setTitle(title);
            //设置traceId
            parseAndSetTraceId(logAttributes, annotation, parameterTypes, pjpArgs);
            //设置类名和方法名
            logAttributes.setTargetClassSimpleName(targetClass.getSimpleName());
            logAttributes.setTargetClassTypeName(targetClass.getTypeName());
            logAttributes.setMethodName(methodName);
            //设置请求参数标记与请求参数日志
            ReqResLogProperties.ReqParamMark reqParamMark = reqResLogProperties.getReqParamMarkWithDefault(annotation);
            ReqResLogProperties.LogType reqLogType = reqResLogProperties.getReqLogTypeWithDefault(annotation);
            if (reqLogType != ReqResLogProperties.LogType.NONE && parameterTypes != null && parameterTypes.length > 0) {
                String[] reqParamMarks = new String[parameterTypes.length];
                if (reqParamMark == ReqResLogProperties.ReqParamMark.TYPE) {
                    for (int i = 0; i < parameterTypes.length; i++) {
                        reqParamMarks[i] = parameterTypes[i].getSimpleName();
                    }
                } else if (reqParamMark == ReqResLogProperties.ReqParamMark.NAME) {
                    System.arraycopy(parameterNames, 0, reqParamMarks, 0, parameterNames.length);
                } else {
                    for (int i = 0; i < parameterTypes.length; i++) {
                        reqParamMarks[i] = "";
                    }
                }
                logAttributes.setReqParamMarks(reqParamMarks);
                String[] reqParamLogs = new String[parameterTypes.length];
                for (int i = 0; i < pjpArgs.length; i++) {
                    reqParamLogs[i] = toJsonOrString(reqLogType, pjpArgs[i]);
                }
                logAttributes.setReqParamLogs(reqParamLogs);
            }
            //设置返回数据标记与返回数据日志
            ReqResLogProperties.ResParamMark resParamMark = reqResLogProperties.getResParamMarkWithDefault(annotation);
            if (resParamMark == ReqResLogProperties.ResParamMark.TYPE) {
                logAttributes.setResParamMark(returnType.getSimpleName());
            }
            return logAttributes;
        } catch (Throwable e) {
            log.error("ReqResLoggerAspect parseLogAttributes", e);
            return null;
        }
    }

    /**
     * 解析traceId
     *
     * @param logAttributes  日志解析属性
     * @param annotation     方法上注解的内容
     * @param parameterTypes 方法请求参数类型
     * @param pjpArgs        方法实际的请求参数
     */
    private void parseAndSetTraceId(LogAttributes logAttributes, ReqResLog annotation, Class[] parameterTypes, Object[] pjpArgs) {
        //从ttl获取traceId如果不为空的话，则直接使用
        String traceIdFromTtl = TraceIdThreadLocal.getTraceId();
        if (StringUtil.isNotEmpty(traceIdFromTtl)) {
            logAttributes.setTraceId(traceIdFromTtl);
            return;
        }
        //尝试获取新的traceId
        String traceId = null;
        ReqResLogProperties.TraceType traceType = reqResLogProperties.getTraceTypeWithDefault(annotation);
        if (traceType == ReqResLogProperties.TraceType.UUID) {
            //使用uuid记录trace信息
            traceId = ReqResLogUtil.generateUuidId();
        } else if (traceType == ReqResLogProperties.TraceType.METHOD) {
            //从请求参数中获取traceId信息
            String traceIdMethod = reqResLogProperties.getTraceIdMethodWithDefault(annotation);
            if (StringUtil.isNotEmpty(traceIdMethod)) {
                if (pjpArgs != null && pjpArgs.length > 0 && pjpArgs[0] != null) {
                    if (parameterTypes != null && parameterTypes.length > 0) {
                        try {
                            Class<?> firstParameterType = parameterTypes[0];
                            Method traceMethod = firstParameterType.getMethod(traceIdMethod, null);
                            Object traceIdObj = traceMethod.invoke(pjpArgs[0]);
                            if (traceIdObj != null) {
                                traceId = traceIdObj.toString();
                            }
                        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                            log.warn("ReqResLoggerAspect parseTraceId Exception", e);
                        }
                    }
                }
            }
        }
        if (StringUtil.isNotEmpty(traceId)) {
            logAttributes.setTraceId(traceId);
            boolean traceIdEntry = annotation.traceIdEntry();
            if (traceIdEntry && TraceIdThreadLocal.setTraceId(traceId)) {
                logAttributes.setTraceIdEntry(true);
            }
        }
    }

    /**
     * 根据LogType把对象转换成Json格式或者toString格式的字符串
     *
     * @param logType 日志记录类别
     * @param object  要被记录的对象
     * @return
     */
    private String toJsonOrString(ReqResLogProperties.LogType logType, Object object) throws JsonProcessingException {
        if (object == null) {
            return null;
        }
        if (logType == ReqResLogProperties.LogType.JSON) {
            return ReqResLogUtil.toJsonWithoutNull(object);
        } else if (logType == ReqResLogProperties.LogType.TO_STRING) {
            return object.toString();
        }
        return null;
    }

}
