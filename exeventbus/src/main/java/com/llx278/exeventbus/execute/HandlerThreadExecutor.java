package com.llx278.exeventbus.execute;

import android.os.Handler;
import android.os.HandlerThread;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.CountDownLatch;

/**
 * 发布时执行在HandlerThread
 * Created by llx on 2018/2/5.
 */

class HandlerThreadExecutor implements Executor {

    private final Handler mHandler;

    HandlerThreadExecutor() {
        HandlerThread handlerThread = new HandlerThread("EventBusHandlerThread");
        handlerThread.start();
        mHandler = new Handler(handlerThread.getLooper());
    }

    @Override
    public Object submit(final Method method, final Object paramObj, final Object object, Type type) {

        switch (type) {
            case DEFAULT:

                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (paramObj == null) {
                                method.invoke(object);
                            } else {
                                method.invoke(object, paramObj);
                            }
                        } catch (IllegalAccessException ignore) {
                            // never happen
                        } catch (InvocationTargetException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
                return null;
            case BLOCK_RETURN:

                CountDownLatch doneSignal = new CountDownLatch(1);
                MyRunnable myRunnable = new MyRunnable(doneSignal, method, paramObj, object);
                mHandler.post(myRunnable);
                try {
                    doneSignal.await();
                } catch (InterruptedException ignore) {
                }
                return myRunnable.getReturnValue();
            default:
                return null;
        }
    }

}
