package com.yytech.test;

import com.yytech.logger.annotation.ReqResLog;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Dog {

    @ReqResLog(level = "WARN")
    public void speak() {
        log.info("汪汪汪~~~");
    }

    @ReqResLog(title = "这是一个测试的title")
    public void run() {
        log.info("dog is running...");
    }

    @ReqResLog(traceType = "UUID")
    public void jump() {
        log.info("dog is jumping...");
    }

    @ReqResLog(reqParamMark = "NAME", traceType = "METHOD", traceIdMethod = "abc")
    public void eat(String... food) {
        if (food == null) {
            log.info("nothing to eat");
            return;
        }
        for (String s : food) {
            log.info("dog eat {}", s);
        }
    }

    @ReqResLog(throwableLogLevel = "WARN")
    public void fly() {
        log.info("dog cat't fly, there is an Exception");
        throw new RuntimeException("DOG CAT'T FLY");
    }

}
