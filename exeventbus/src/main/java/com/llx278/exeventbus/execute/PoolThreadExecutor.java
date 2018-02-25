package com.llx278.exeventbus.execute;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by llx on 2018/2/5.
 */

public class PoolThreadExecutor implements Executor {

    private final ExecutorService mExecutor;

    PoolThreadExecutor() {
        // 先写死一个线程池，这里的线程池应该由EventBus从外部注入的
        mExecutor = Executors.newCachedThreadPool();
    }
    @Override
    public void execute(final Method method, final Object paramObj, final Object object) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    method.invoke(object,paramObj);
                } catch (IllegalAccessException ignore) {
                } catch (InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }
}
