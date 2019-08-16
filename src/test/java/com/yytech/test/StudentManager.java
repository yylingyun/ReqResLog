package com.yytech.test;

import com.yytech.logger.annotation.ReqResLog;

public class StudentManager {

    @ReqResLog(traceType = "METHOD", traceIdMethod = "traceId")
    public Student addScore(Student student, int addScore) {
        if (student != null) {
            student.setScore(student.getScore() + addScore);
        }
        return student;
    }

    @ReqResLog(reqParamMark = "NAME", resLogType = "TO_STRING")
    public Student subtractionScore(Student student, int subtractionScore) {
        if (student != null) {
            student.setScore(student.getScore() - subtractionScore);
        }
        return student;
    }

}
