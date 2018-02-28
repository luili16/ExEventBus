package com.llx278.exeventbus.execute;

import android.os.Handler;
import android.os.Looper;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.CountDownLatch;

/**
 * 发布时执行在主线程
 * Created by llx on 2018/2/5.
 */

public class MainThreadExecutor implements Executor {

    private final Handler mHandler;

     MainThreadExecutor() {
        mHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    public void execute(final Method method, final Object paramObj, final Object obj) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    method.invoke(obj,paramObj);
                } catch (IllegalAccessException ignore) {
                    // never happen
                } catch (InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    @Override
    public Object submit(Method method, Object paramObj, Object obj) {
        CountDownLatch doneSignal = new CountDownLatch(1);
        MyRunnable myRunnable = new MyRunnable(doneSignal,method,paramObj,obj);
        mHandler.post(myRunnable);
        try {
            doneSignal.await();
        } catch (InterruptedException ignore) {
        }
        return myRunnable.getReturnValue();
    }
}
