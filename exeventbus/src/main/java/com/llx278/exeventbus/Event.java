package com.llx278.exeventbus;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

/**
 * 代表一个订阅事件
 * 这里面用传入方法的参数的class和tag来作为这个唯一标志
 * Created by llx on 2018/2/4.
 */

public final class Event implements Parcelable {

    /**
     * 事件对象的参数类型
     */
    private final String mParamClassName;

    /**
     * 事件的tag
     */
    private final String mTag;

    /**
     * 事件对象返回值的参数类型
     */
    private final String mReturnClassName;


    public Event(@NonNull String tag, @NonNull String paramClassName, @NonNull String returnClassName) {
        mTag = tag;
        mParamClassName = paramClassName;
        mReturnClassName = returnClassName;
    }

    protected Event(Parcel in) {
        mParamClassName = in.readString();
        mTag = in.readString();
        mReturnClassName = in.readString();
    }

    public static final Creator<Event> CREATOR = new Creator<Event>() {
        @Override
        public Event createFromParcel(Parcel in) {
            return new Event(in);
        }

        @Override
        public Event[] newArray(int size) {
            return new Event[size];
        }
    };

    public String getParamClassName() {
        return mParamClassName;
    }

    public String getTag() {
        return mTag;
    }

    public String getReturnClassName() {
        return mReturnClassName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Event event = (Event) o;

        if (!mParamClassName.equals(event.mParamClassName)) return false;
        if (!mTag.equals(event.mTag)) return false;
        return mReturnClassName.equals(event.mReturnClassName);
    }

    @Override
    public int hashCode() {
        int result = mParamClassName.hashCode();
        result = 31 * result + mTag.hashCode();
        result = 31 * result + mReturnClassName.hashCode();
        return result;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mParamClassName);
        dest.writeString(mTag);
        dest.writeString(mReturnClassName);
    }

    @Override
    public String toString() {
        return "Event{" +
                "mParamClassName='" + mParamClassName + '\'' +
                ", mTag='" + mTag + '\'' +
                ", mReturnClassName='" + mReturnClassName + '\'' +
                '}';
    }
}
