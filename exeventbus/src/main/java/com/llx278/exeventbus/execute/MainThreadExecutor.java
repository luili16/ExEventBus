package com.llx278.exeventbus.execute;

import android.os.Handler;
import android.os.Looper;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 发布的时间执行在主线程
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
}
