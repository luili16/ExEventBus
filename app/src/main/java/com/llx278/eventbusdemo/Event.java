package com.llx278.eventbusdemo;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by liu on 18-3-31.
 */

public class Event implements Parcelable {

    private final String msg;
    private final int pid;

    public Event(String msg, int pid) {
        this.msg = msg;
        this.pid = pid;
    }

    protected Event(Parcel in) {
        msg = in.readString();
        pid = in.readInt();
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

    public String getMsg() {
        return msg;
    }

    public int getPid() {
        return pid;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(msg);
        dest.writeInt(pid);
    }
}
