package com.llx278.exeventbus.execute;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * 发布事件放到线程池中执行
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

    @Override
    public Object submit(final Method method, final Object paramObj, final Object obj) {

        try {
            return mExecutor.submit(new Callable<Object>() {
                @Override
                public Object call() throws Exception {
                    return method.invoke(obj,paramObj);
                }
            }).get();
        } catch (InterruptedException ignore) {
            return null;
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
}
