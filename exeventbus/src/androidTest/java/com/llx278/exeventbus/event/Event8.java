package com.llx278.exeventbus.event;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by llx on 2018/2/6.
 */

public class Event8 implements Parcelable {
    private String msg;

    public Event8(String msg) {
        this.msg = msg;
    }

    protected Event8(Parcel in) {
        this.msg = in.readString();
    }

    public static final Creator<Event8> CREATOR = new Creator<Event8>() {
        @Override
        public Event8 createFromParcel(Parcel source) {
            return new Event8(source);
        }

        @Override
        public Event8[] newArray(int size) {
            return new Event8[size];
        }
    };

    public String getMsg() {
        return msg;
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
