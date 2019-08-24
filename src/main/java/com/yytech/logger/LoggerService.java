package com.yytech.logger;

import com.yytech.logger.autoconfig.ReqResLogProperties;

/**
 * 日志实际记录的行为接口
 */
public interface LoggerService {

    /**
     * 处理并记录Req日志
     *
     * @param logAttributes 日志参数
     */
    void processReqLog(LogAttributes logAttributes);

    /**
     * 处理并记录Res日志
     *
     * @param logAttributes 日志参数
     */
    void processResLog(LogAttributes logAttributes);

    /**
     * 处理并记录Throwable日志
     *
     * @param logAttributes 日志参数
     */
    void processThrowableLog(LogAttributes logAttributes);

    /**
     * 判断日志等级是否可用
     *
     * @param level 日志参数
     * @return 该日志级别是否需要打印
     */
    boolean isLogLevelEnable(ReqResLogProperties.Level level);

}
