package com.yytech.logger.util;

/**
 * 字符串工具类
 */
public class StringUtil {

    /**
     * 判断字符串是否为空
     *
     * @param str 要被判断的字符串
     * @return 是否为空
     */
    public static boolean isEmpty(String str) {
        return (str == null || str.trim().isEmpty());
    }

    /**
     * 判断字符串是否非空
     *
     * @param str 要被判断的字符串
     * @return 是否非空
     */
    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

}
