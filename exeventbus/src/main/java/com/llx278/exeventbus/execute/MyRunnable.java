package com.llx278.exeventbus.execute;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.CountDownLatch;

/**
 * 执行阻塞线程
 * Created by llx on 2018/2/26.
 */
class MyRunnable implements Runnable {

    private final CountDownLatch mDoneSignal;
    private final Method mMethod;
    private final Object mParamObj;
    private final Object mObject;
    private Object mReturnValue;

    MyRunnable(CountDownLatch doneSignal, Method method, Object paramObj, Object object) {
        mDoneSignal = doneSignal;
        mMethod = method;
        mParamObj = paramObj;
        mObject = object;
    }

    @Override
    public void run() {
        try {
            if (mParamObj == null) {
                mReturnValue = mMethod.invoke(mObject);
            } else {
                mReturnValue = mMethod.invoke(mObject, mParamObj);
            }
        } catch (IllegalAccessException ignore) {
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
        mDoneSignal.countDown();
    }

    Object getReturnValue() {
        return mReturnValue;
    }
}
