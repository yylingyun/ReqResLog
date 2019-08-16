package com.yytech.logger.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.UUID;

/**
 * 工具类
 */
public class ReqResLogUtil {

    /**
     * 生成一个uuid
     *
     * @return
     */
    public static String generateUuidId() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    /**
     * 把对应转换成json打印出来
     *
     * @param object
     * @return
     * @throws JsonProcessingException
     */
    public static String toJsonWithoutNull(Object object) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        return objectMapper.writeValueAsString(object);
    }

    /**
     * first非空优先取first，然后second非空取second，否则取deft值
     *
     * @param first
     * @param second
     * @param deft
     * @param <T>
     * @return
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
     * @param first
     * @param second
     * @param deft
     * @return
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

}
