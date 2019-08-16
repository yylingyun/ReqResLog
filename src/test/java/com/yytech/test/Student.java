package com.yytech.test;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Student {

    private String name;

    private int score;

    public String traceId() {
        return "<入参方法返回的traceId>";
    }

    @Override
    public String toString() {
        return "<name:" + name + ",score:" + score + ">";
    }

}
