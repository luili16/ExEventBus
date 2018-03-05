package com.llx278.exeventbus.event;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by llx on 2018/2/6.
 */

public class Event9 implements Parcelable {
    private String msg;

    public Event9(String msg) {
        this.msg = msg;
    }

    protected Event9(Parcel in) {
        this.msg = in.readString();
    }

    public static final Creator<Event9> CREATOR = new Creator<Event9>() {
        @Override
        public Event9 createFromParcel(Parcel source) {
            return new Event9(source);
        }

        @Override
        public Event9[] newArray(int size) {
            return new Event9[size];
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
