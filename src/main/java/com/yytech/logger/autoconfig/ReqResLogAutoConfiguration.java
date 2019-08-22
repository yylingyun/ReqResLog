package com.yytech.logger.autoconfig;

import com.yytech.logger.DefaultLoggerServiceImpl;
import com.yytech.logger.LoggerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * 全局配置
 */
@Configuration
@EnableConfigurationProperties(ReqResLogProperties.class)
@EnableAspectJAutoProxy
public class ReqResLogAutoConfiguration {

    @Autowired
    ReqResLogProperties reqResLogProperties;

    @Bean
    public LoggerService defaultLoggerService() {
        return new DefaultLoggerServiceImpl();
    }

}
