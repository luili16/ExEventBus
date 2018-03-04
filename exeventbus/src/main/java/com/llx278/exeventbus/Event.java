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

    /**
     * 标志这个事件是否可以被其他进程执行
     */
    private final boolean mIsRemote;


    public Event(@NonNull String tag, @NonNull String paramClassName, @NonNull String returnClassName,boolean isRemote) {
        mTag = tag;
        mParamClassName = paramClassName;
        mReturnClassName = returnClassName;
        mIsRemote = isRemote;
    }

    protected Event(Parcel in) {
        mParamClassName = in.readString();
        mTag = in.readString();
        mReturnClassName = in.readString();
        mIsRemote = in.readByte() != 0;
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



    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mParamClassName);
        dest.writeString(mTag);
        dest.writeString(mReturnClassName);
        dest.writeByte((byte) (mIsRemote ? 1 : 0));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Event event = (Event) o;

        if (mIsRemote != event.mIsRemote) return false;
        if (!mParamClassName.equals(event.mParamClassName)) return false;
        if (!mTag.equals(event.mTag)) return false;
        return mReturnClassName.equals(event.mReturnClassName);
    }

    @Override
    public int hashCode() {
        int result = mParamClassName.hashCode();
        result = 31 * result + mTag.hashCode();
        result = 31 * result + mReturnClassName.hashCode();
        result = 31 * result + (mIsRemote ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Event{" +
                "mParamClassName='" + mParamClassName + '\'' +
                ", mTag='" + mTag + '\'' +
                ", mReturnClassName='" + mReturnClassName + '\'' +
                ", mIsRemote=" + mIsRemote +
                '}';
    }

    public String getParamClassName() {
        return mParamClassName;
    }

    public String getTag() {
        return mTag;
    }

    public String getReturnClassName() {
        return mReturnClassName;
    }

    public boolean isIsRemote() {
        return mIsRemote;
    }
}
