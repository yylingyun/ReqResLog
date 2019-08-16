package com.yytech.logger.aspect;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.yytech.logger.annotation.ReqResLog;
import com.yytech.logger.autoconfig.ReqResLogProperties;
import com.yytech.logger.util.ReqResLogUtil;
import com.yytech.logger.util.StringUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 日志记录切面方式的实现
 */
@Slf4j
@Aspect
@Component
public class ReqResLogAspect {

    @Autowired
    ReqResLogProperties reqResLogProperties;

    /**
     * 代理被ReqResLog注释的方法，记录它的ReqResLog
     *
     * @param pjp
     * @return
     * @throws Throwable
     */
    @Around("@annotation(com.yytech.logger.annotation.ReqResLog)")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        LogCoreParams logCoreParams = getLogCoreParams(pjp);
        try {
            //处理请求日志
            processReqLog(logCoreParams, pjp);
            //实际方法调用处理
            Object response = pjp.proceed();
            //处理返回日志
            processResLog(logCoreParams, pjp, response);
            return response;
        } catch (Throwable throwable) {
            //处理异常日志
            processThrowableLog(logCoreParams, throwable);
            throw throwable;
        }
    }

    /**
     * 组装可复用的日志记录数据
     *
     * @param pjp
     * @return
     */
    private LogCoreParams getLogCoreParams(ProceedingJoinPoint pjp) {
        try {
            LogCoreParams logCoreParams = new LogCoreParams();
            Class<?> targetClass = pjp.getTarget().getClass();
            String classSimpleName = targetClass.getSimpleName();
            MethodSignature signature = (MethodSignature) pjp.getSignature();
            Method method = signature.getMethod();
            String methodName = method.getName();
            Class<?>[] parameterTypes = method.getParameterTypes();
            /*
             * 获取targetClassMethod上的annotation而不是signature.getMethod()的annotation
             * 因为signature.getMethod()的annotation在jdk动态代理的情况下取的是接口上的annotation
             */
            Method targetClassMethod = targetClass.getMethod(methodName, parameterTypes);
            ReqResLog annotation = targetClassMethod.getAnnotation(ReqResLog.class);
            ReqResLogProperties.Level logLevel = reqResLogProperties.getLevelWithDefault(annotation);
            boolean logLevelEnable = isLogLevelEnable(logLevel);
            ReqResLogProperties.Level throwableLogLevel = reqResLogProperties.getThrowableLogLevelWithDefault(annotation);
            boolean throwableLogLevelEnable = isLogLevelEnable(throwableLogLevel);
            logCoreParams.setLogLevelEnable(logLevelEnable);
            logCoreParams.setThrowableLogLevelEnable(throwableLogLevelEnable);
            logCoreParams.setLogLevel(logLevel);
            logCoreParams.setThrowableLogLevel(throwableLogLevel);
            logCoreParams.setAnnotation(annotation);
            //logLevelEnable或throwableLogLevelEnable为true的时候才有必要计算fullTitle，因为都不为true不会打日志
            String fullTitle = "";
            if (logLevelEnable || throwableLogLevelEnable) {
                fullTitle = classSimpleName + "." + methodName;
                String title = reqResLogProperties.getTitleWithDefault(annotation);
                if (StringUtil.isNotEmpty(title)) {
                    fullTitle = fullTitle + " " + title;
                }
                //获取串联日志的traceId
                String traceIdLog = null;
                ReqResLogProperties.TraceType traceType = reqResLogProperties.getTraceTypeWithDefault(annotation);
                if (traceType == ReqResLogProperties.TraceType.UUID) {
                    //使用uuid记录trace信息
                    traceIdLog = ReqResLogUtil.generateUuidId();
                } else if (traceType == ReqResLogProperties.TraceType.METHOD) {
                    //从请求参数中获取traceId信息
                    String traceIdMethod = reqResLogProperties.getTraceIdMethodWithDefault(annotation);
                    if (StringUtil.isNotEmpty(traceIdMethod)) {
                        if (parameterTypes != null && parameterTypes.length > 0) {
                            Class<?> firstParameterType = parameterTypes[0];
                            try {
                                Method traceMethod = firstParameterType.getMethod(traceIdMethod, null);
                                Object[] pjpArgs = pjp.getArgs();
                                if (pjpArgs != null && pjpArgs.length > 0 && pjpArgs[0] != null) {
                                    Object traceId = traceMethod.invoke(pjpArgs[0]);
                                    traceIdLog = "" + traceId;
                                } else {
                                    traceIdLog = "null";
                                }
                            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                                traceIdLog = "[" + e.getClass().getSimpleName() + "]";
                            }
                        }
                    }
                }
                if (StringUtil.isNotEmpty(traceIdLog)) {
                    fullTitle = fullTitle + " traceId:" + traceIdLog;
                }
            }
            logCoreParams.setFullTitle(fullTitle);
            return logCoreParams;
        } catch (Throwable e) {
            log.error("ReqResLogAspect getLogCoreParams", e);
            return null;
        }
    }

    /**
     * 处理req日志
     *
     * @param logCoreParams
     * @param pjp
     */
    private void processReqLog(LogCoreParams logCoreParams, ProceedingJoinPoint pjp) {
        try {
            if (logCoreParams == null || !logCoreParams.isLogLevelEnable()) {
                return;
            }
            StringBuilder requestParamsLogSb = new StringBuilder("[REQUEST]");
            String fullTitle = logCoreParams.getFullTitle();
            ReqResLogProperties.Level logLevel = logCoreParams.getLogLevel();
            ReqResLog annotation = logCoreParams.getAnnotation();
            ReqResLogProperties.LogType reqLogType = reqResLogProperties.getReqLogTypeWithDefault(annotation);
            if (reqLogType == ReqResLogProperties.LogType.NONE) {
                //只记录请求行为，不记录请求实体
                writeLog(logLevel, fullTitle + " " + requestParamsLogSb.toString());
                return;
            }
            MethodSignature signature = (MethodSignature) pjp.getSignature();
            Class[] parameterTypes = signature.getParameterTypes();
            String[] parameterNames = signature.getParameterNames();
            if (parameterTypes == null || parameterTypes.length == 0) {
                //只记录请求行为，因为请求参数个数为0
                writeLog(logLevel, fullTitle + " " + requestParamsLogSb.toString());
                return;
            }
            ReqResLogProperties.ReqParamMark reqParamMark = reqResLogProperties.getReqParamMarkWithDefault(annotation);
            Object[] pjpArgs = pjp.getArgs();
            for (int i = 0; i < pjpArgs.length; i++) {
                String paramMark = null;
                String paramLog = toJsonOrString(reqLogType, pjpArgs[i]);
                if (reqParamMark == ReqResLogProperties.ReqParamMark.TYPE) {
                    paramMark = parameterTypes[i].getSimpleName();
                } else if (reqParamMark == ReqResLogProperties.ReqParamMark.NAME) {
                    paramMark = parameterNames[i];
                }
                requestParamsLogSb.append(" ");
                if (paramMark != null) {
                    requestParamsLogSb.append(paramMark).append(":");
                }
                requestParamsLogSb.append(paramLog);
            }
            writeLog(logLevel, fullTitle + " " + requestParamsLogSb.toString());
        } catch (Throwable e) {
            log.error("ReqResLogAspect processReqLog", e);
        }
    }

    /**
     * 处理res日志
     *
     * @param logCoreParams
     * @param pjp
     * @param response
     */
    private void processResLog(LogCoreParams logCoreParams, ProceedingJoinPoint pjp, Object response) {
        try {
            if (logCoreParams == null || !logCoreParams.isLogLevelEnable()) {
                return;
            }
            StringBuilder responseParamsLogSb = new StringBuilder("[RESPONSE]");
            String fullTitle = logCoreParams.getFullTitle();
            ReqResLogProperties.Level logLevel = logCoreParams.getLogLevel();
            ReqResLog annotation = logCoreParams.getAnnotation();
            ReqResLogProperties.LogType resLogType = reqResLogProperties.getResLogTypeWithDefault(annotation);
            if (resLogType == ReqResLogProperties.LogType.NONE) {
                //只记录返回行为，不记录返回实体
                writeLog(logLevel, fullTitle + " " + responseParamsLogSb.toString());
                return;
            }
            //获取返回值类型
            Class returnType = ((MethodSignature) pjp.getSignature()).getReturnType();
            if (returnType.equals(Void.TYPE)) {
                //只记录返回行为，因为返回内容是void
                writeLog(logLevel, fullTitle + " " + responseParamsLogSb.toString());
                return;
            }
            ReqResLogProperties.ResParamMark resParamMark = reqResLogProperties.getResParamMarkWithDefault(annotation);
            responseParamsLogSb.append(" ");
            if (resParamMark == ReqResLogProperties.ResParamMark.TYPE) {
                responseParamsLogSb.append(returnType.getSimpleName()).append(":");
            }
            responseParamsLogSb.append(toJsonOrString(resLogType, response));
            writeLog(logLevel, fullTitle + " " + responseParamsLogSb.toString());
        } catch (Throwable e) {
            log.error("ReqResLogAspect processResLog", e);
        }
    }

    /**
     * 处理throwable日志
     *
     * @param logCoreParams
     * @param throwable
     */
    private void processThrowableLog(LogCoreParams logCoreParams, Throwable throwable) {
        try {
            if (logCoreParams == null || !logCoreParams.isThrowableLogLevelEnable() || throwable == null) {
                return;
            }
            String fullTitle = logCoreParams.getFullTitle();
            ReqResLogProperties.Level throwableLogLevel = logCoreParams.getThrowableLogLevel();
            ReqResLog annotation = logCoreParams.getAnnotation();
            ReqResLogProperties.ThrowableLogType throwableLogType = reqResLogProperties.getThrowableLogTypeWithDefault(annotation);
            if (throwableLogType == ReqResLogProperties.ThrowableLogType.STACK) {
                writeThrowableLog(throwableLogLevel, fullTitle, throwable);
            } else if (throwableLogType == ReqResLogProperties.ThrowableLogType.MESSAGE) {
                writeLog(throwableLogLevel, fullTitle + " " + throwable.getClass().getName()
                        + ":" + throwable.getMessage());
            }
        } catch (Throwable e) {
            log.error("ReqResLogAspect processThrowableLog", e);
        }
    }

    /**
     * 记录日志
     *
     * @param level
     * @param logContent
     */
    private void writeLog(ReqResLogProperties.Level level, String logContent) {
        if (level == ReqResLogProperties.Level.TRACE) {
            log.trace(logContent);
        } else if (level == ReqResLogProperties.Level.DEBUG) {
            log.debug(logContent);
        } else if (level == ReqResLogProperties.Level.INFO) {
            log.info(logContent);
        } else if (level == ReqResLogProperties.Level.WARN) {
            log.warn(logContent);
        } else if (level == ReqResLogProperties.Level.ERROR) {
            log.error(logContent);
        }
    }

    /**
     * 记录异常日志
     *
     * @param level
     * @param logContent
     * @param throwable
     */
    private void writeThrowableLog(ReqResLogProperties.Level level, String logContent, Throwable throwable) {
        if (level == ReqResLogProperties.Level.TRACE) {
            log.trace(logContent, throwable);
        } else if (level == ReqResLogProperties.Level.DEBUG) {
            log.debug(logContent, throwable);
        } else if (level == ReqResLogProperties.Level.INFO) {
            log.info(logContent, throwable);
        } else if (level == ReqResLogProperties.Level.WARN) {
            log.warn(logContent, throwable);
        } else if (level == ReqResLogProperties.Level.ERROR) {
            log.error(logContent, throwable);
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

    /**
     * 判断日志等级是否可用
     *
     * @param level
     * @return
     */
    private boolean isLogLevelEnable(ReqResLogProperties.Level level) {
        if (ReqResLogProperties.Level.TRACE == level) {
            return log.isTraceEnabled();
        }
        if (ReqResLogProperties.Level.DEBUG == level) {
            return log.isDebugEnabled();
        }
        if (ReqResLogProperties.Level.INFO == level) {
            return log.isInfoEnabled();
        }
        if (ReqResLogProperties.Level.WARN == level) {
            return log.isWarnEnabled();
        }
        if (ReqResLogProperties.Level.ERROR == level) {
            return log.isErrorEnabled();
        }
        return false;
    }

    @Getter
    @Setter
    private static class LogCoreParams {
        /**
         * req/res日志级别是否可用
         */
        private boolean logLevelEnable;
        /**
         * throwable日志级别是否可用
         */
        private boolean throwableLogLevelEnable;
        /**
         * req/res日志级别
         */
        private ReqResLogProperties.Level logLevel;
        /**
         * throwable日志级别
         */
        private ReqResLogProperties.Level throwableLogLevel;
        /**
         * 被代理的方法注解上d值
         */
        private ReqResLog annotation;
        /**
         * 完整的记录title，拼接在实际日志数据前
         * (logLevelEnable || throwableLogLevelEnable) == true的时候才计算此值
         */
        private String fullTitle;
    }

}
