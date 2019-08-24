package com.yytech.logger.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.util.UUID;

/**
 * 工具类
 */
@Slf4j
public class ReqResLogUtil {

    /**
     * 生成一个uuid
     *
     * @return 一个随机生成的uuid
     */
    public static String generateUuidId() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    /**
     * 把对应转换成json打印出来
     *
     * @param object 要转换成json的对象
     * @return json字符串
     * @throws JsonProcessingException json处理异常
     */
    public static String toJsonWithoutNull(Object object) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        return objectMapper.writeValueAsString(object);
    }

    /**
     * first非空优先取first，然后second非空取second，否则取deft值
     *
     * @param first  首选值
     * @param second 备选值
     * @param deft   默认值
     * @param <T>    类型
     * @return 最终选取值
     */
    public static <T> T getWithDefault(T first, T second, T deft) {
        if (first != null) {
            return first;
        }
        if (second != null) {
            return second;
        }
        return deft;
    }

    /**
     * first非空优先取first，然后second非空取second，否则取deft值
     *
     * @param first  首选值
     * @param second 备选值
     * @param deft   默认值
     * @return 最终选取值
     */
    public static String getStringWithDefault(String first, String second, String deft) {
        if (StringUtil.isNotEmpty(first)) {
            return first;
        }
        if (StringUtil.isNotEmpty(second)) {
            return second;
        }
        return deft;
    }

    /**
     * Throwable转换成堆栈字符串
     *
     * @param throwable Throwable
     * @return stackTrace
     */
    public static String getStackTrace(final Throwable throwable) {
        if (throwable == null) {
            return null;
        }
        try (final StringWriter sw = new StringWriter();
             final PrintWriter pw = new PrintWriter(sw, true)) {
            throwable.printStackTrace(pw);
            return sw.getBuffer().toString();
        } catch (IOException e) {
            return null;
        }
    }

}
