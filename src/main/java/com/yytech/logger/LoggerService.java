package com.yytech.logger;

import com.yytech.logger.autoconfig.ReqResLogProperties;

/**
 * 日志实际记录的行为接口
 */
public interface LoggerService {

    /**
     * 处理并记录Req日志
     *
     * @param logAttributes
     */
    void processReqLog(LogAttributes logAttributes);

    /**
     * 处理并记录Res日志
     *
     * @param logAttributes
     */
    void processResLog(LogAttributes logAttributes);

    /**
     * 处理并记录Throwable日志
     *
     * @param logAttributes
     */
    void processThrowableLog(LogAttributes logAttributes);

    /**
     * 判断日志等级是否可用
     *
     * @param level
     * @return
     */
    boolean isLogLevelEnable(ReqResLogProperties.Level level);

}
