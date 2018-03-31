package com.llx278.exeventbus;



import com.llx278.exeventbus.execute.ThreadModel;
import com.llx278.exeventbus.execute.Type;

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
     * 订阅事件的类型，描述了EventBus该如何处理此订阅事件
     */
    Type type() default Type.DEFAULT;

    /**
     * 订阅事件的tag，同订阅事件所执行的方法的参数与返回值一起标识一个订阅事件
     * 执行方法的参数和返回值可以是void
     */
    String tag();

    /**
     * 订阅事件执行的线程，默认是在主线程
     */
    ThreadModel model() default ThreadModel.MAIN;

    /**
     *此订阅事件是否可以跨进程执行，默认是false
     */
    boolean remote() default false;
}
