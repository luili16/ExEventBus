package com.llx278.exeventbus.execute;

import android.support.annotation.NonNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * 发布事件放到线程池中执行
 * Created by llx on 2018/2/5.
 */

class PoolThreadExecutor implements Executor {

    private final ExecutorService mExecutor;

    PoolThreadExecutor() {
        // 先写死一个线程池，这里的线程池应该由EventBus从外部注入的
        mExecutor = Executors.newCachedThreadPool(new PoolThreadFactory());
    }

    @Override
    public Object submit(final Method method, final Object paramObj, final Object object, Type type) {

        switch (type) {
            case DEFAULT:
                mExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (paramObj == null) {
                                method.invoke(object);
                            } else {
                                method.invoke(object, paramObj);
                            }

                        } catch (IllegalAccessException ignore) {
                        } catch (InvocationTargetException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
                return null;
            case BLOCK_RETURN:
                try {
                    return mExecutor.submit(new Callable<Object>() {
                        @Override
                        public Object call() throws Exception {
                            if (paramObj == null) {
                                return method.invoke(object);
                            } else {
                                return method.invoke(object, paramObj);
                            }
                        }
                    }).get();
                } catch (InterruptedException ignore) {
                    return null;
                } catch (ExecutionException e) {
                    throw new RuntimeException(e);
                }

            default:
                return null;
        }
    }

    /**
     * 对线程命名
     * Created by llx on 2018/3/26.
     */

    static class PoolThreadFactory implements ThreadFactory {
        private static final String THREAD_NAME = "ExEventBus-pool_thread";
        private int mNum = 0;

        @Override
        public Thread newThread(@NonNull Runnable r) {
            mNum++;
            if (mNum == Integer.MAX_VALUE) {
                mNum = 0;
            }
            return new Thread(r,THREAD_NAME + "-" + mNum);
        }
    }
}
