package com.yytech.test;

import com.yytech.logger.annotation.ReqResLog;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Cat implements Animal {

    @ReqResLog
    @Override
    public void speak() {
        log.info("喵喵喵~~~");
    }

    @Override
    public void run() {
        log.info("cat is running...");
    }

}
