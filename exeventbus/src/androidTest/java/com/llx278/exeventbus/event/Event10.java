package com.llx278.exeventbus.event;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by llx on 2018/2/6.
 */

public class Event10 implements Parcelable {
    private String msg;

    public Event10(String msg) {
        this.msg = msg;
    }

    protected Event10(Parcel in) {
        this.msg = in.readString();
    }

    public static final Creator<Event10> CREATOR = new Creator<Event10>() {
        @Override
        public Event10 createFromParcel(Parcel source) {
            return new Event10(source);
        }

        @Override
        public Event10[] newArray(int size) {
            return new Event10[size];
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
