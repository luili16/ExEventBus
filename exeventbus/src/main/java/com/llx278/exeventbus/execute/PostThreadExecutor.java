package com.llx278.exeventbus.execute;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 发布事件执行在当前的线程
 * Created by llx on 2018/2/5.
 */

class PostThreadExecutor implements Executor {

    PostThreadExecutor() {
    }

    @Override
    public Object submit(Method method, Object paramObj, Object obj, Type type) {

        switch (type) {
            case DEFAULT:
                try {
                    method.invoke(obj, paramObj);
                } catch (IllegalAccessException ignore) {
                    // never happen
                } catch (InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
                return null;
            case BLOCK_RETURN:
                try {
                    if (paramObj == null) {
                        return method.invoke(obj);
                    } else {
                        return method.invoke(obj, paramObj);
                    }
                } catch (IllegalAccessException ignore) {
                    // never happen
                } catch (InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
            default:
                return null;
        }
    }
}
