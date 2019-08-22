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

    @Autowired
    Teacher teacher;

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

        //不存在traceIdMethod方法，会有warn级别的异常信息输出，不影响程序正常执行
        dog.eat("milk", "meat");

        //有一个RunTime异常日志打印，日志级别配置成了WARN，因为狗不会飞
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

    @Test
    public void test2() throws InterruptedException {
        //同一个线程间的调用，多个方法保持traceId是一致的
        teacher.addScore(new Student("testName1", 88), 10);

        //new一个线程，父子线程关系，多个方法也能保持traceId一致
        teacher.addScoreAsync(new Student("testName2", 88), 10);

        //使用线程池，多个方法也能保持traceId一致
        teacher.addScoreAsyncInPool(new Student("testName3", 88), 10);

        //线程池的maxsize=1，因为线程池被TtlExecutors修饰过，所以线程池中线程复用的时候，也能取到新的正确的traceId
        //(如果线程池去掉了TtlExecutors修饰，则线程再次使用的时候，拿到的是这个线程第一次使用时的traceId，不是我们想要的效果)
        //本工程采用ttl来保证使用线程池时，调用链路context中traceId一致
        //ttl使用详情请参考：https://github.com/alibaba/transmittable-thread-local
        teacher.addScoreAsyncInPool(new Student("testName4", 88), 10);
        Thread.sleep(3000L);
    }

}
