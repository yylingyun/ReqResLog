package com.yytech.logger.util;

import com.alibaba.ttl.TransmittableThreadLocal;

/**
 * traceId的threadLocal信息
 */
public class TraceIdThreadLocal {

    private static final TransmittableThreadLocal<String> traceIdTtl = new TransmittableThreadLocal<>();

    /**
     * 把traceId设置到ttl中，只有非空的traceId才能设置成功
     *
     * @param traceId
     * @return
     */
    public static boolean setTraceId(String traceId) {
        if (StringUtil.isEmpty(traceId)) {
            return false;
        }
        traceIdTtl.set(traceId);
        return true;
    }

    /**
     * 从ttl中获取traceId
     *
     * @return
     */
    public static String getTraceId() {
        return traceIdTtl.get();
    }

    /**
     * 释放资源
     */
    public static void release() {
        traceIdTtl.remove();
    }

}
