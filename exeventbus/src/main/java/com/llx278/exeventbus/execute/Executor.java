package com.llx278.exeventbus.execute;

import java.lang.reflect.Method;

/**
 * 订阅事件的执行者
 * Created by llx on 2018/2/5.
 */

public interface Executor {
    /**
     * 执行发布的事件，并阻塞当前线程，直到事件结束，并返回执行的结果
     * @param method 待执行的方法
     * @param paramObj 待执行的参数
     * @param obj 方法拥有者
     * @param type 执行的类型
     * @return 此方法执行的结果,如果执行的方法返回值为void，则返回null
     */
    Object submit(Method method, Object paramObj, Object obj,Type type);
}
