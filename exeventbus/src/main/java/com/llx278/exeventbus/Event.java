package com.llx278.exeventbus;

import android.support.annotation.NonNull;

/**
 * 代表一个订阅事件
 * 这里面用传入方法的参数的class和tag来作为这个唯一标志
 * Created by llx on 2018/2/4.
 */

public final class Event {

    /**
     * 事件对象的参数类型
     */
    private final Class<?> mParamClass;
    /**
     * 事件的tag
     */
    private final String mTag;

    /**
     * 事件对象返回值的参数类型
     */
    private final Class<?> mReturnClass;

    public Event(@NonNull String tag, @NonNull Class<?> paramClass,@NonNull Class<?> returnClass) {
        mTag = tag;
        mParamClass = paramClass;
        mReturnClass = returnClass;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Event event = (Event) o;

        if (!mParamClass.equals(event.mParamClass)) return false;
        if (!mTag.equals(event.mTag)) return false;
        return mReturnClass.equals(event.mReturnClass);
    }

    @Override
    public int hashCode() {
        int result = mParamClass.hashCode();
        result = 31 * result + mTag.hashCode();
        result = 31 * result + mReturnClass.hashCode();
        return result;
    }
}
