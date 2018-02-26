package com.llx278.exeventbus.execute;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 发布事件执行在当前的线程
 * Created by llx on 2018/2/5.
 */

public class PostThreadExecutor implements Executor {

    PostThreadExecutor() {
    }

    @Override
    public void execute(Method method, Object paramObj, Object obj) {
        try {
            method.invoke(obj,paramObj);
        } catch (IllegalAccessException ignore) {
            // never happen
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Object submit(Method method, Object paramObj, Object obj) {
        try {
            return method.invoke(obj,paramObj);
        } catch (IllegalAccessException ignore) {
            // never happen
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
        return null;
    }
}
