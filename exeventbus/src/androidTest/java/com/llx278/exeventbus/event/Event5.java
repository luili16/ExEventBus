package com.llx278.exeventbus.event;

/**
 * Created by llx on 2018/2/6.
 */

public class Event5 extends BaseEvent {
    public  String msg;
    public Event5(String msg) {
        this.msg = msg;
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
        return null;
    }
}
