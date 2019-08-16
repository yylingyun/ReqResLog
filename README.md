# ReqResLog

### 简单入门 ###
1. 引入maven依赖（目前并没有deploy到maven中央服务器，要使用的话得临时deploy到自己的私服上，后续我会把这个deploy到maven中央服务器，搞定后会更新这个页面）
```xml
<dependency>
    <groupId>com.yytech.logger</groupId>
    <artifactId>x-yytech-logger</artifactId>
    <version>1.0-SNAPSHOT</version>
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
4. 代码包含了可以运行的测试case，不妨跑几个试试

### 配置参数 ###
1. 配置优先级：注解配置 > 配置文件配置 > 默认配置
2. 配置文件中属性配置方式（注解中支持的配置属性与配置文件相同）
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
3. 各配置属性意义
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
```

### 原理 ###
```
Spring AOP与java反射机制
```