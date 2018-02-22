package com.llx278.exeventbus;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 对外发布的注解类
 * Created by llx on 2018/2/4.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Subscriber {
    /**
     * 订阅事件的tag，同订阅的参数一起标识一个订阅事件
     */
    String tag() default EventType.DEFAULT_TAG;

    /**
     * 订阅事件执行的线程，默认是在主线程
     */
    ThreadModel mode() default ThreadModel.MAIN;

}
