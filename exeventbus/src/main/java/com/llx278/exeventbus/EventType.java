package com.llx278.exeventbus;

import android.support.annotation.NonNull;
import android.text.TextUtils;

/**
 * EventType代表一个用{@link Subscriber}所注解的方法的唯一标志
 * 这里面用传入方法的参数的class和tag来作为这个唯一标志
 * Created by llx on 2018/2/4.
 */

final class EventType {

    static final String DEFAULT_TAG = "DefaultTag";

    /**
     * 事件对象的参数类型
     */
    final Class<?> mParamClass;
    /**
     * 事件的tag
     */
    final String mTag;

    EventType(@NonNull String tag, @NonNull Class<?> paramClass) {
        mTag = tag;
        mParamClass = paramClass;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EventType eventType = (EventType) o;

        //noinspection SimplifiableIfStatement
        if (!mParamClass.equals(eventType.mParamClass)) return false;
        return mTag.equals(eventType.mTag);
    }

    @Override
    public int hashCode() {
        int result = mParamClass.hashCode();
        result = 31 * result + mTag.hashCode();
        return result;
    }
}
