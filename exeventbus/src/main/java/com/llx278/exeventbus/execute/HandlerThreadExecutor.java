package com.llx278.exeventbus.execute;

import android.os.Handler;
import android.os.HandlerThread;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 发布的时间执行在HandlerThread
 * Created by llx on 2018/2/5.
 */

public class HandlerThreadExecutor implements Executor {

    private final Handler mHandler;

    public HandlerThreadExecutor() {
        HandlerThread handlerThread = new HandlerThread("EventBusHandlerThread");
        handlerThread.start();
        mHandler = new Handler(handlerThread.getLooper());
    }

    @Override
    public void execute(final Method method, final Object paramObj, final Object object) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    method.invoke(object,paramObj);
                } catch (IllegalAccessException ignore) {
                    // never happen
                } catch (InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }
}
