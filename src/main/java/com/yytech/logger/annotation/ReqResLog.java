package com.yytech.logger.annotation;

import java.lang.annotation.*;

/**
 * 将此注释添加到类的方法上，可以记录方法的Req与Res日志。
 * 使用这种代理和反射的方法记录日志多少会影响性能，超高qps那种基础服务方法不建议在生产环境使用
 * 日志记录基于Spring的AOP实现，所以只有被Spring管理的bean才生效，下面简述一下常见问题
 * 更详细的内容请参考Spring AOP相关知识
 * --------
 * 当某个bean实现了接口的时候，Spring默认使用java动态代理，此时只有接口中的方法才能被代理
 * 所以只有接口中的方法才能记录下日志
 * --------
 * 当某个bean没有实现接口的时候，Spring会使用CGLIB动态代理，由于CGLIB创建子类overwrite父类方法的机制
 * 所以final的方法无法被代理，所以final的方法日志无法被记录
 * --------
 * 当使用动态代理的时候，一个被代理的类的某个方法method1中又调用了自己这个对象的方法method2
 * 此时method2由于并没有被代理拦截到，所以method2的日志无法打印
 * --------
 * 当然最终生效的代理方式可以用户进行强制指定，这个以用户指定为主
 * --------
 * 配置优先级：first:注解配置 second:全局配置  third:默认配置
 *
 * @see com.yytech.logger.autoconfig.ReqResLogProperties
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface ReqResLog {

    /**
     * 记录日志的级别 TRACE/DEBUG/INFO/WARN/ERROR
     * -------------
     * 默认生效配置: INFO
     *
     * @return level
     */
    String level() default "";

    /**
     * 记录日志的标题，在每行日志实际数据前会包含该内容
     *
     * @return title
     */
    String title() default "";

    /**
     * 打印请求日志时如何标记各个字段
     * TYPE：使用请求参数类型标记
     * NAME：使用请求参数名字标记
     * NONE：仅打印请求参数，不记录类型或名字
     * -------------
     * 默认生效配置：TYPE
     *
     * @return reqParamMark
     */
    String reqParamMark() default "";

    /**
     * 打印返回日志时如何标记
     * TYPE：使用返回数据的类型标记
     * NONE：不进行标记
     * -------------
     * 默认生效配置：TYPE
     *
     * @return resParamMark
     */
    String resParamMark() default "";

    /**
     * 请求数据记录类别
     * JSON：对象转换成json记录
     * TO_STRING：用对象的toString方法记录
     * NONE: 仅记录req行为，不记录req实际数据
     * -------------
     * 默认生效配置：JSON
     *
     * @return reqLogType
     */
    String reqLogType() default "";

    /**
     * 返回数据记录类别
     * JSON：对象转换成json记录
     * TO_STRING：用对象的toString方法记录
     * NONE: 仅记录res行为，不记录res实际数据
     * -------------
     * 默认生效配置：JSON
     *
     * @return resLogType
     */
    String resLogType() default "";

    /**
     * 异常日志的记录方式
     * STACK：记录异常名字和异常堆栈
     * MESSAGE：记录异常名字和异常信息
     * NONE：不进行异常记录
     * -------------
     * 默认生效配置：STACK
     *
     * @return throwableLogType
     */
    String throwableLogType() default "";

    /**
     * 记录throwable日志的级别 TRACE/DEBUG/INFO/WARN/ERROR
     * -------------
     * 默认生效配置: ERROR
     *
     * @return throwableLogLevel
     */
    String throwableLogLevel() default "";

    /**
     * 日志串联模式
     * UUID：使用uuid作为traceId串联每一条日志
     * METHOD：详情请见traceIdMethod属性注释
     * NONE：不用额外数据进行日志的串联
     * 备注：如果当前线程可以从TraceIdThreadLocal拿到非空traceId，则忽略配置属性，直接使用
     * -------------
     * 默认生效配置: NONE
     *
     * @return traceType
     */
    String traceType() default "";

    /**
     * 当满足以下全部条件时，会用请求参数中第一个参数的对应方法获取traceId，并用toString方法记录
     * 1.traceType为method
     * 2.请求参数个数大于等于1
     * 3.第一个请求参数包含该无参方法且有返回值
     *
     * @return traceIdMethod
     */
    String traceIdMethod() default "";

    /**
     * 是否可作为traceId的入口
     * 当此属性为true且从当前的TraceIdThreadLocal中拿到的traceId为空时
     * 把此方法拿到的traceId放入TraceIdThreadLocal中
     * 以供后面的方法使用
     * -------------
     * 如果在使用线程池的情况下还想保持traceId跨线程正常工作
     * 则需要改动工程以符合TransmittableThreadLocal的使用规则(个人认为改动不大)
     * 详情请见
     * https://github.com/alibaba/transmittable-thread-local
     *
     * @return traceIdEntry
     */
    boolean traceIdEntry() default false;

}
