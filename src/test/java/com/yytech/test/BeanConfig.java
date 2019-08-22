package com.yytech.test;

import com.alibaba.ttl.threadpool.TtlExecutors;
import com.yytech.logger.DefaultLoggerServiceImpl;
import com.yytech.logger.LoggerService;
import com.yytech.logger.autoconfig.ReqResLogProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

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
    public Teacher teacher() {
        return new Teacher();
    }

    @Bean
    public StudentManager studentManager() {
        return new StudentManager();
    }

    /**
     * 配置一个extends DefaultLoggerServiceImpl或者implements LoggerService
     * 可以自定义日志记录行为
     * 如本case中，每条日志都多打印一个"[DIY-PREFIX]|"
     *
     * @return
     */
    @Bean
    @Primary
    public LoggerService testLoggerService() {
        return new DefaultLoggerServiceImpl() {
            @Override
            protected void writeLog(ReqResLogProperties.Level level, String logContent) {
                //给logContent拼接一个前缀
                logContent = "[DIY-PREFIX]|" + logContent;
                super.writeLog(level, logContent);
            }
        };
    }

    @Bean("callerRunsPolicyExecutor")
    public Executor callerRunsPolicyExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        //配置核心线程数
        executor.setCorePoolSize(1);
        //配置最大线程数
        executor.setMaxPoolSize(1);
        //配置队列大小
        executor.setQueueCapacity(10);
        //配置拒绝策略
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        //执行初始化
        executor.initialize();
        /*
         * 返回一个被修饰的executor
         * 详情参考
         * https://github.com/alibaba/transmittable-thread-local
         */
//        return executor;
        return TtlExecutors.getTtlExecutor(executor);
    }

}
