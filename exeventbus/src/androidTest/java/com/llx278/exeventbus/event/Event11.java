package com.llx278.exeventbus.event;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by llx on 2018/2/6.
 */

public class Event11 extends BaseEvent implements Parcelable {
    private String msg;

    public Event11(){}
    public Event11(String msg) {
        this.msg = msg;
    }

    protected Event11(Parcel in) {
        this.msg = in.readString();
    }

    public static final Creator<Event11> CREATOR = new Creator<Event11>() {
        @Override
        public Event11 createFromParcel(Parcel source) {
            return new Event11(source);
        }

        @Override
        public Event11[] newArray(int size) {
            return new Event11[size];
        }
    };

    @Override
    public String toString() {
        return msg;
    }

    @Override
    public String getMsg() {
        return msg;
    }

    @Override
    public void setMsg(String msg) {
        this.msg = msg;
    }

    @Override
    public BaseEvent deepCopy() {
        return new Event11(this.msg);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(msg);
    }
}
