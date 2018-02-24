package com.llx278.exeventbus;

import android.support.annotation.NonNull;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;

/**
 * 订阅者对象，是对{@link Subscriber}所注解的方法的一个封装
 * Created by llx on 2018/2/4.
 */

class Subscription {
    final WeakReference<Object> mSubscribeRef;
    final Method mMethod;
    final ThreadModel mThreadModel;

    Subscription(@NonNull Object param, @NonNull Method method, @NonNull ThreadModel threadModel) {
        mSubscribeRef = new WeakReference<Object>(param);
        mMethod = method;
        mThreadModel = threadModel;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Subscription that = (Subscription) o;

        if (!mSubscribeRef.equals(that.mSubscribeRef)) return false;
        //noinspection SimplifiableIfStatement
        if (!mMethod.equals(that.mMethod)) return false;
        return mThreadModel == that.mThreadModel;
    }

    @Override
    public int hashCode() {
        int result = mSubscribeRef.hashCode();
        result = 31 * result + mMethod.hashCode();
        result = 31 * result + mThreadModel.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Subscription{" +
                "mSubscribeRef=" + mSubscribeRef +
                ", mMethod=" + mMethod +
                ", mThreadModel=" + mThreadModel +
                '}';
    }
}
