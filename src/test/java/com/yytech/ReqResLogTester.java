package com.yytech;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.yytech.logger.annotation.EnableReqResLog;
import com.yytech.logger.autoconfig.ReqResLogProperties;
import com.yytech.logger.util.ReqResLogUtil;
import com.yytech.test.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = {BeanConfig.class})
@EnableReqResLog
public class ReqResLogTester {

    @Autowired
    ReqResLogProperties reqResLogProperties;

    @Autowired
    Animal cat;

    @Autowired
    Dog dog;

    @Autowired
    StudentManager studentManager;


    @Test
    public void test1() throws JsonProcessingException {
        /*
         * 通过配置文件进行的配置会映射到这个配置对象里
         * 最终配置应用的优先级为
         * 注解上的配置 > 配置文件里进行的全局配置 > 默认配置
         */
        log.info("reqResLogProperties:{}", ReqResLogUtil.toJsonWithoutNull(reqResLogProperties));

        //因为Cat implements Animal，所以默认使用的是JDK动态代理
        log.info("Cat class:{}", cat.getClass());

        //Dog没有继承接口，所以使用的是CGLIB代理
        log.info("dog class:{}", dog.getClass());

        //存在ReqResLog，所以可以打印日志
        cat.speak();

        //不存在ReqResLog，所以不打印日志
        cat.run();

        //日志等级为WARN
        dog.speak();

        //带title的日志
        dog.run();

        //使用UUID串联请求和返回日志
        dog.jump();

        //不存在traceIdMethod方法，则traceId表示为NoSuchMethodException
        dog.eat("milk", "meat");

        //里面存在一个RunTime异常，因为狗不会飞
        try {
            dog.fly();
        } catch (Exception e) {

        }

        //使用第一个入参的traceId方法获取日志的traceId。请求用默认的JSON格式记录
        studentManager.addScore(new Student("testName1", 88), 10);

        //配置了traceIdMethod，但第一个参数为null的情况下traceId会记录为null
        studentManager.addScore(null, 10);

        //请求参数的标记为字段名而不是字段类型，返回值用toString方法记录
        studentManager.subtractionScore(new Student("testName2", 90), 20);

    }

}
