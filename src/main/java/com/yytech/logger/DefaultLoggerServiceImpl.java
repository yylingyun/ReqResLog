package com.yytech.logger;

import com.yytech.logger.autoconfig.ReqResLogProperties;
import com.yytech.logger.util.StringUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 日志记录默认实现类
 */
@Slf4j
public class DefaultLoggerServiceImpl implements LoggerService {

    @Override
    public void processReqLog(LogAttributes logAttributes) {
        ReqResLogProperties.Level logLevel = logAttributes.getLogLevel();
        String reqLog = getReqLog(logAttributes);
        writeLog(logLevel, reqLog);
    }

    @Override
    public void processResLog(LogAttributes logAttributes) {
        ReqResLogProperties.Level logLevel = logAttributes.getLogLevel();
        String resLog = getResLog(logAttributes);
        writeLog(logLevel, resLog);
    }

    @Override
    public void processThrowableLog(LogAttributes logAttributes) {
        ReqResLogProperties.Level throwableLogLevel = logAttributes.getThrowableLogLevel();
        String throwableLog = getThrowableLog(logAttributes);
        writeLog(throwableLogLevel, throwableLog);
    }

    @Override
    public boolean isLogLevelEnable(ReqResLogProperties.Level level) {
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

    /**
     * 获取请求日志
     *
     * @param logAttributes 日志参数
     * @return req日志内容
     */
    protected String getReqLog(LogAttributes logAttributes) {
        StringBuilder sb = getLogPrefixStringBuilder(logAttributes);
        sb.append(" [REQUEST]");
        String[] reqParamMarks = logAttributes.getReqParamMarks();
        String[] reqParamLogs = logAttributes.getReqParamLogs();
        if (reqParamLogs != null && reqParamLogs.length > 0) {
            for (int i = 0; i < reqParamLogs.length; i++) {
                sb.append(" ");
                if (StringUtil.isNotEmpty(reqParamMarks[i])) {
                    sb.append(reqParamMarks[i]).append(":");
                }
                sb.append(reqParamLogs[i]);
            }
        }
        return sb.toString();
    }

    /**
     * 获取返回日志
     *
     * @param logAttributes 日志参数
     * @return res日志内容
     */
    protected String getResLog(LogAttributes logAttributes) {
        StringBuilder sb = getLogPrefixStringBuilder(logAttributes);
        sb.append(" [RESPONSE]");
        String resParamMark = logAttributes.getResParamMark();
        String resParamLog = logAttributes.getResParamLog();
        if (StringUtil.isNotEmpty(resParamLog)) {
            sb.append(" ");
            if (StringUtil.isNotEmpty(resParamMark)) {
                sb.append(resParamMark).append(":");
            }
            sb.append(resParamLog);
        }
        return sb.toString();
    }

    /**
     * 获取Throwable日志
     *
     * @param logAttributes 日志参数
     * @return throwable日志内容
     */
    protected String getThrowableLog(LogAttributes logAttributes) {
        StringBuilder sb = getLogPrefixStringBuilder(logAttributes);
        sb.append(" [THROWABLE] ");
        String throwableLog = logAttributes.getThrowableLog();
        sb.append(throwableLog);
        return sb.toString();
    }

    /**
     * 获取日志前缀
     *
     * @param logAttributes 日志前缀
     * @return 包含日志前缀的StringBuilder
     */
    protected StringBuilder getLogPrefixStringBuilder(LogAttributes logAttributes) {
        StringBuilder sb = new StringBuilder();
        sb.append(logAttributes.getTargetClassSimpleName())
                .append(".")
                .append(logAttributes.getMethodName());
        if (StringUtil.isNotEmpty(logAttributes.getTitle())) {
            sb.append(" ").append(logAttributes.getTitle());
        }
        if (StringUtil.isNotEmpty(logAttributes.getTraceId())) {
            sb.append(" traceId:").append(logAttributes.getTraceId());
        }
        return sb;
    }

    /**
     * 记录日志
     *
     * @param level      日志记录级别
     * @param logContent 实际日志内容
     */
    protected void writeLog(ReqResLogProperties.Level level, String logContent) {
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

}
