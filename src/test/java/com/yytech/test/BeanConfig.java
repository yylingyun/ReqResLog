package com.yytech.test;

import com.yytech.logger.DefaultLoggerServiceImpl;
import com.yytech.logger.LoggerService;
import com.yytech.logger.autoconfig.ReqResLogProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class BeanConfig {

    @Bean
    public Cat cat() {
        return new Cat();
    }

    @Bean
    public Dog dog() {
        return new Dog();
    }

    @Bean
    public StudentManager studentManager() {
        return new StudentManager();
    }

    @Bean
    @Primary
    public LoggerService testLoggerService() {
        return new DefaultLoggerServiceImpl() {
            @Override
            protected void writeLog(ReqResLogProperties.Level level, String logContent) {
                logContent = "[DIY-PREFIX]|" + logContent;
                super.writeLog(level, logContent);
            }
        };
    }

}
