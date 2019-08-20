package com.yytech.logger.aspect;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.yytech.logger.LogAttributes;
import com.yytech.logger.LoggerService;
import com.yytech.logger.annotation.ReqResLog;
import com.yytech.logger.autoconfig.ReqResLogProperties;
import com.yytech.logger.util.ReqResLogUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;

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
     * @param pjp
     * @return
     * @throws Throwable
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
        }
    }

    /**
     * 处理Req日志
     *
     * @param logAttributes
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
     * @param logAttributes
     * @param response
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
     * @param logAttributes
     * @param throwable
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
     * @param pjp
     * @return
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
            //TODO 设置traceId
            String traceId = null;
            logAttributes.setTraceId(traceId);
            //设置类名和方法名
            logAttributes.setTargetClassSimpleName(targetClass.getSimpleName());
            logAttributes.setTargetClassTypeName(targetClass.getTypeName());
            logAttributes.setMethodName(methodName);
            //设置请求参数标记与请求参数日志
            ReqResLogProperties.ReqParamMark reqParamMark = reqResLogProperties.getReqParamMarkWithDefault(annotation);
            ReqResLogProperties.LogType reqLogType = reqResLogProperties.getReqLogTypeWithDefault(annotation);
            if (reqLogType != ReqResLogProperties.LogType.NONE
                    && parameterTypes != null && parameterTypes.length > 0) {
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
     * 根据LogType把对象转换成Json格式或者toString格式的字符串
     *
     * @param logType
     * @param object
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