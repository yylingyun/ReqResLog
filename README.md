# ReqResLog

### 简介 ###
ReqResLog是基于Spring AOP与反射机制实现自动记录方法Request, Response, Throwable日志的注解。
它主要负责一个工程内的调用链路记录，并且提供机制使得这个工程内的调用可以与微服务之间的调用保持一致的traceId，从而使得调用链路更清晰，排查问题更容易。

### 简单入门 ###
1. 引入maven依赖（已经发到了Maven中央服务器）
```xml
<dependency>
    <groupId>com.github.yylingyun</groupId>
    <artifactId>x-yytech-logger</artifactId>
    <version>1.0.RELEASE</version>
</dependency>
```
2. SpringBoot启动类加上注解
```
@EnableReqResLog
```
3. 方法上加上注解
```
@ReqResLog
```

### 测试case展示 ###
1. 测试Student类定义
```java
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
```
2. 测试方法定义(给一个student加分，指定日志中traceId的获取方式为从第一个参数的指定方法获取，指定日志记录的traceId获取方法为"traceId")
```
@ReqResLog(traceType = "METHOD", traceIdMethod = "traceId")
public Student addScore(Student student, int addScore) {
    if (student != null) {
        student.setScore(student.getScore() + addScore);
    }
    return student;
}
```
3. 输出日志
```
StudentManager.addScore traceId:<入参方法返回的traceId> [REQUEST] Student:{"name":"testName1","score":88} int:10
StudentManager.addScore traceId:<入参方法返回的traceId> [RESPONSE] Student:{"name":"testName1","score":98}
```
4. 代码包含了很多可以运行的测试case，不妨跑几个试试

### 配置参数 ###
1. 配置优先级：注解配置 > 配置文件配置 > 默认配置
2. 配置文件中属性配置方式
```
req-res-log.level=
req-res-log.title=
req-res-log.req-param-mark=
req-res-log.res-param-mark=
req-res-log.req-log-type=
req-res-log.res-log-type=
req-res-log.throwable-log-type=
req-res-log.throwable-log-level=
req-res-log.trace-type=
req-res-log.trace-id-method=
```
3. 注解中配置属性包括配置文件中的全部属性，而且额外有一个属性
```
traceIdEntry
```

4. 各配置属性意义
```
level
     * 记录日志的级别 TRACE/DEBUG/INFO/WARN/ERROR
     * -------------
     * 默认生效配置: INFO
     
title
     * 记录日志的标题，在每行日志实际数据前会包含该内容
     
reqParamMark
     * 打印请求日志时如何标记各个字段
     * TYPE：使用请求参数类型标记
     * NAME：使用请求参数名字标记
     * NONE：仅打印请求参数，不记录类型或名字
     * -------------
     * 默认生效配置：TYPE
     
resParamMark
     * 打印返回日志时如何标记
     * TYPE：使用返回数据的类型标记
     * NONE：不进行标记
     * -------------
     * 默认生效配置：TYPE
     
reqLogType
     * 请求数据记录类别
     * JSON：对象转换成json记录
     * TO_STRING：用对象的toString方法记录
     * NONE: 仅记录req行为，不记录req实际数据
     * -------------
     * 默认生效配置：JSON
     
resLogType
     * 返回数据记录类别
     * JSON：对象转换成json记录
     * TO_STRING：用对象的toString方法记录
     * NONE: 仅记录res行为，不记录res实际数据
     * -------------
     * 默认生效配置：JSON
     
throwableLogType
     * 异常日志的记录方式
     * STACK：记录异常名字和异常堆栈
     * MESSAGE：记录异常名字和异常信息
     * NONE：不进行异常记录
     * -------------
     * 默认生效配置：STACK
     
throwableLogLevel
     * 记录throwable日志的级别 TRACE/DEBUG/INFO/WARN/ERROR
     * -------------
     * 默认生效配置: ERROR
     
traceType
     * 日志串联模式
     * UUID：使用uuid作为traceId串联每一条日志
     * METHOD：详情请见traceIdMethod属性注释
     * NONE：不用额外数据进行日志的串联
     * -------------
     * 默认生效配置: NONE
     
traceIdMethod
     * 当满足以下全部条件时，会用请求参数中第一个参数的对应方法获取traceId，并用toString方法记录
     * 1.traceType为method
     * 2.请求参数个数大于等于1
     * 3.第一个请求参数包含该无参方法且有返回值
     
traceIdEntry
     * 是否可作为traceId的入口
     * 当此属性为true且从当前的TraceIdThreadLocal中拿到的traceId为空时
     * 把此方法拿到的traceId放入TraceIdThreadLocal中
     * 以供后面的方法使用
     * -------------
     * 如果在使用线程池的情况下还想保持traceId跨线程正常工作
     * 则需要改动工程以符合TransmittableThreadLocal的使用规则(个人认为改动不大)
     * 详情请见
     * https://github.com/alibaba/transmittable-thread-local
```

### traceId详解 ###

#### traceId目的与原理 ####
- 我们使用traceId的时候一般不仅仅是为了把request日志和response日志串联起来，我们更希望traceId可以把整个调用链路串联起来。
- 如果是单线程工作的话使用ThreadLocal把traceId在入口处存起来，后续调用链路中使用，最后再release掉，就可以达到效果。多线程场景可以使用InheritableThreadLocal。
- 但是如果我们使用了线程池的话，InheritableThreadLocal在这个场景下就不能达到我们的预期了。所以我们这个项目使用的是TransmittableThreadLocal来保证traceId在调用链路中保持一致。如果你的业务中用了线程池技术并且希望在线程池中执行的任务也保持traceId一致，需要把工程做一下简单改造以适配TransmittableThreadLocal。详情请参考 [alibaba/transmittable-thread-local](https://github.com/alibaba/transmittable-thread-local)
 
#### traceId获取方式 ####
我们使用
```com.yytech.logger.util.TraceIdThreadLocal```
对```com.alibaba.ttl.TransmittableThreadLocal```进行一层包装来存储当前context下的traceId，当TraceIdThreadLocal中存在当前context的traceId时，则直接使用这个traceId。否则会尝试用配置的方式获取traceId。所以我们有以下2个方法获取traceId。
1. 现在很多微服务框架都能记录各个服务之间的调用链路，这时候其实已经有一个traceId了，我们这个注解负责记录的是工程内部的调用链路。如果服务间的traceId和工程内的traceId是一致的话，那么整个链路就会更加清晰，排查问题更加容易。我们可以用我们的服务框架中的filter做一层拦截，在收到请求的时候把服务间的traceId设置到TraceIdThreadLocal中。
```
TraceIdThreadLocal.setTraceId(traceId);
```
调用完成之后再release掉
```
TraceIdThreadLocal.release();
```
这样后续被```@ReqResLog```注解的方法就会使用这个traceId了。当然这个用法不利用filter也是可行的，只要在合适的地方向TraceIdThreadLocal赋值，用完后release掉都是可以的。使用filter只是一个推荐的用法。

2. 利用@ReqResLog中的属性来进行traceId的获取或者自动生成
```
#具体使用方式参考配置参数那一节
traceType
traceIdMethod
traceIdEntry

```

### 自定义日志记录 ###
默认的日志记录实现类是```com.yytech.logger.DefaultLoggerServiceImpl```,记录方式用的是```@Slf4j```,如
```
log.info(logContent);
```
我们可以新建一个类```extends com.yytech.logger.DefaultLoggerServiceImpl```或者```implements com.yytech.logger.LoggerService```来自定义日志记录方式(如把日志打向阿里云，重新定义各个参数在日志中的顺序等)。新建的类需要实例化并被Spring管理起来，记得添加```@Primary```注解来使其实际生效。例子如下：
```
@Bean
@Primary
public LoggerService testLoggerService() {
    return new DefaultLoggerServiceImpl() {
        @Override
        protected void writeLog(ReqResLogProperties.Level level, String logContent) {
            //给logContent拼接一个前缀
            logContent = "[DIY-PREFIX]|" + logContent;
            super.writeLog(level, logContent);
        }
    };
}
```


### 原理 ###
1. Spring AOP机制实现方法代理
2. Java反射机制通过配置方法名获取第一个入参的traceId
3. TransmittableThreadLocal来保证traceId在调用链路中保持一致