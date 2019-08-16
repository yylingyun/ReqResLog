package com.yytech.logger.autoconfig;

import com.yytech.logger.annotation.ReqResLog;
import com.yytech.logger.util.ReqResLogUtil;
import com.yytech.logger.util.StringUtil;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * ReqResLog的全局配置
 * 属性值与ReqResLog属性值一致，配置属性请参考ReqResLog
 *
 * @see ReqResLog
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "req-res-log")
public class ReqResLogProperties {

    /**
     * 记录日志的级别
     *
     * @see ReqResLog#level()
     */
    private String level;

    /**
     * 记录日志的标题
     *
     * @see ReqResLog#title()
     */
    private String title;

    /**
     * 打印请求日志时如何标记各个字段
     *
     * @see ReqResLog#reqParamMark()
     */
    private String reqParamMark;

    /**
     * 打印返回日志时如何标记
     *
     * @see ReqResLog#resParamMark()
     */
    private String resParamMark;

    /**
     * 请求数据记录类别
     *
     * @see ReqResLog#reqLogType()
     */
    private String reqLogType;

    /**
     * 返回数据记录类别
     *
     * @see ReqResLog#resLogType()
     */
    private String resLogType;

    /**
     * 异常日志的记录方式
     *
     * @see ReqResLog#throwableLogType()
     */
    private String throwableLogType;

    /**
     * 记录throwable日志的级别
     *
     * @see ReqResLog#throwableLogLevel()
     */
    private String throwableLogLevel;

    /**
     * 日志串联模式
     *
     * @see ReqResLog#traceType()
     */
    private String traceType;

    /**
     * 获取traceId使用的方法
     *
     * @see ReqResLog#traceIdMethod()
     */
    private String traceIdMethod;

    public Level getLevelWithDefault(ReqResLog annotation) {
        Level first = Level.fromStr(annotation.level());
        Level second = Level.fromStr(this.level);
        return ReqResLogUtil.getWithDefault(first, second, Level.INFO);
    }

    public String getTitleWithDefault(ReqResLog annotation) {
        return ReqResLogUtil.getStringWithDefault(annotation.title(),
                this.title, "");
    }

    public ReqParamMark getReqParamMarkWithDefault(ReqResLog annotation) {
        ReqParamMark first = ReqParamMark.fromStr(annotation.reqParamMark());
        ReqParamMark second = ReqParamMark.fromStr(this.reqParamMark);
        return ReqResLogUtil.getWithDefault(first, second, ReqParamMark.TYPE);
    }

    public ResParamMark getResParamMarkWithDefault(ReqResLog annotation) {
        ResParamMark first = ResParamMark.fromStr(annotation.resParamMark());
        ResParamMark second = ResParamMark.fromStr(this.resParamMark);
        return ReqResLogUtil.getWithDefault(first, second, ResParamMark.TYPE);
    }

    public LogType getReqLogTypeWithDefault(ReqResLog annotation) {
        LogType first = LogType.fromStr(annotation.reqLogType());
        LogType second = LogType.fromStr(this.reqLogType);
        return ReqResLogUtil.getWithDefault(first, second, LogType.JSON);
    }

    public LogType getResLogTypeWithDefault(ReqResLog annotation) {
        LogType first = LogType.fromStr(annotation.resLogType());
        LogType second = LogType.fromStr(this.resLogType);
        return ReqResLogUtil.getWithDefault(first, second, LogType.JSON);
    }

    public ThrowableLogType getThrowableLogTypeWithDefault(ReqResLog annotation) {
        ThrowableLogType first = ThrowableLogType.fromStr(annotation.throwableLogType());
        ThrowableLogType second = ThrowableLogType.fromStr(this.throwableLogType);
        return ReqResLogUtil.getWithDefault(first, second, ThrowableLogType.STACK);
    }

    public Level getThrowableLogLevelWithDefault(ReqResLog annotation) {
        Level first = Level.fromStr(annotation.throwableLogLevel());
        Level second = Level.fromStr(this.throwableLogLevel);
        return ReqResLogUtil.getWithDefault(first, second, Level.ERROR);
    }

    public TraceType getTraceTypeWithDefault(ReqResLog annotation) {
        TraceType first = TraceType.fromStr(annotation.traceType());
        TraceType second = TraceType.fromStr(this.traceType);
        return ReqResLogUtil.getWithDefault(first, second, TraceType.NONE);
    }

    public String getTraceIdMethodWithDefault(ReqResLog annotation) {
        return ReqResLogUtil.getStringWithDefault(annotation.traceIdMethod(),
                traceIdMethod, "");
    }

    public enum Level {
        TRACE, DEBUG, INFO, WARN, ERROR;

        public static Level fromStr(String str) {
            if (StringUtil.isNotEmpty(str)) {
                for (Level value : Level.values()) {
                    if (value.toString().equalsIgnoreCase(str)) {
                        return value;
                    }
                }
            }
            return null;
        }
    }

    public enum ReqParamMark {
        TYPE, NAME, NONE;

        public static ReqParamMark fromStr(String str) {
            if (StringUtil.isNotEmpty(str)) {
                for (ReqParamMark value : ReqParamMark.values()) {
                    if (value.toString().equalsIgnoreCase(str)) {
                        return value;
                    }
                }
            }
            return null;
        }
    }

    public enum ResParamMark {
        TYPE, NONE;

        public static ResParamMark fromStr(String str) {
            if (StringUtil.isNotEmpty(str)) {
                for (ResParamMark value : ResParamMark.values()) {
                    if (value.toString().equalsIgnoreCase(str)) {
                        return value;
                    }
                }
            }
            return null;
        }
    }

    public enum LogType {
        JSON, TO_STRING, NONE;

        public static LogType fromStr(String str) {
            if (StringUtil.isNotEmpty(str)) {
                for (LogType value : LogType.values()) {
                    if (value.toString().equalsIgnoreCase(str)) {
                        return value;
                    }
                }
            }
            return null;
        }
    }

    public enum ThrowableLogType {
        STACK, MESSAGE, NONE;

        public static ThrowableLogType fromStr(String str) {
            if (StringUtil.isNotEmpty(str)) {
                for (ThrowableLogType value : ThrowableLogType.values()) {
                    if (value.toString().equalsIgnoreCase(str)) {
                        return value;
                    }
                }
            }
            return null;
        }
    }

    public enum TraceType {
        UUID, METHOD, NONE;

        public static TraceType fromStr(String str) {
            if (StringUtil.isNotEmpty(str)) {
                for (TraceType value : TraceType.values()) {
                    if (value.toString().equalsIgnoreCase(str)) {
                        return value;
                    }
                }
            }
            return null;
        }
    }

}
