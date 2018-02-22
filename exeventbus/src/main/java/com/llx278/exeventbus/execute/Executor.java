package com.llx278.exeventbus.execute;

import java.lang.reflect.Method;

/**
 * 订阅事件的执行者
 * Created by llx on 2018/2/5.
 */

public interface Executor {

    /**
     * 执行发布的事件
     * @param method 待执行的方法
     * @param paramObj 待执行的参数
     * @param obj 方法的拥有者
     */
    void execute(Method method,Object paramObj,Object obj);

}
