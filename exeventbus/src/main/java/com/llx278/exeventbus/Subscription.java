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
    final Type mType;

    Subscription(@NonNull Object param, @NonNull Method method, @NonNull ThreadModel threadModel,@NonNull Type type) {
        mSubscribeRef = new WeakReference<Object>(param);
        mMethod = method;
        mThreadModel = threadModel;
        mType = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Subscription that = (Subscription) o;

        if (!mSubscribeRef.equals(that.mSubscribeRef)) return false;
        if (!mMethod.equals(that.mMethod)) return false;
        if (mThreadModel != that.mThreadModel) return false;
        return mType == that.mType;
    }

    @Override
    public int hashCode() {
        int result = mSubscribeRef.hashCode();
        result = 31 * result + mMethod.hashCode();
        result = 31 * result + mThreadModel.hashCode();
        result = 31 * result + mType.hashCode();
        return result;
    }
}
