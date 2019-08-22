package com.yytech.test;

import com.yytech.logger.annotation.ReqResLog;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * Created by yangyue on 2019-08-22.
 */
public class Teacher {

    @Autowired
    StudentManager studentManager;

    @Autowired
    Executor callerRunsPolicyExecutor;

    @ReqResLog(traceType = "UUID", traceIdEntry = true)
    public void addScore(Student student, int addScore) {
        studentManager.addScore(student, addScore);
    }

    @ReqResLog(traceType = "UUID", traceIdEntry = true)
    public void addScoreAsync(Student student, int addScore) {
        new Thread(() -> studentManager.addScore(student, addScore)).start();
    }

    @ReqResLog(traceType = "UUID", traceIdEntry = true)
    public void addScoreAsyncInPool(Student student, int addScore) {
        CompletableFuture.runAsync(() -> studentManager.addScore(student, addScore), callerRunsPolicyExecutor);
    }

}
