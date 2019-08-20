package com.yytech.logger;

import com.yytech.logger.annotation.ReqResLog;
import com.yytech.logger.autoconfig.ReqResLogProperties;
import lombok.Getter;
import lombok.Setter;

/**
 * 记录日志需要用到的各个属性
 */
@Getter
@Setter
public class LogAttributes {

    /**
     * req/res日志级别
     */
    private ReqResLogProperties.Level logLevel;

    /**
     * throwable日志级别
     */
    private ReqResLogProperties.Level throwableLogLevel;

    /**
     * req/res日志是否需要记录isLogLevelEnable(logLevel)
     */
    private boolean logEnable;

    /**
     * throwable日志是否需要记录(isLogLevelEnable(throwableLogLevel) && throwableLogType!=ThrowableLogType.NONE)
     */
    private boolean throwableLogEnable;

    /**
     * 配置的日志title
     */
    private String title;

    /**
     * 当前context日志记录使用的traceId
     */
    private String traceId;

    /**
     * 当前执行方法所属的类的simpleName
     */
    private String targetClassSimpleName;

    /**
     * 当前执行方法所属的类的typeName
     */
    private String targetClassTypeName;

    /**
     * 当前执行方法的名字
     */
    private String methodName;

    /**
     * req的标记
     * reqParamMarks和reqParamLogs均不为null时，他们的length是一样的
     */
    private String[] reqParamMarks;

    /**
     * req的信息
     * reqParamMarks和reqParamLogs均不为null时，他们的length是一样的
     */
    private String[] reqParamLogs;

    /**
     * res的标记
     */
    private String resParamMark;

    /**
     * res的信息
     * 方法正常返回后此值才有内容
     */
    private String resParamLog;

    /**
     * 异常message或者stack
     * 方法抛出异常后且throwableLogEnable为true时，此值才有内容
     */
    private String throwableLog;

    /**
     * 正在执行的方法上的注解内容
     * 如需访问生效配置，可以注入ReqResLogProperties，并用里面的方法获取生效配置
     * {@link com.yytech.logger.autoconfig.ReqResLogProperties#getLevelWithDefault(ReqResLog)}
     */
    private ReqResLog reqResLogAnnotation;

}
