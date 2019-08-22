package com.yytech.logger.annotation;

import com.yytech.logger.aspect.ReqResLoggerAspect;
import com.yytech.logger.autoconfig.ReqResLogAutoConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 若要启用ReqResLog，则将此注解应用于启动类
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import({ReqResLogAutoConfiguration.class, ReqResLoggerAspect.class})
public @interface EnableReqResLog {

}
