package com.llx278.exeventbus.exception;

/**
 * 当向其他的进程发布一个并没有被订阅的事件时会抛出这个异常
 * Created by llx on 2018/3/2.
 */

public class UnSubscribedException extends RuntimeException {


    public UnSubscribedException(String message) {
        super(message);
    }

    public UnSubscribedException() {
        super();
    }

    public UnSubscribedException(String message,Throwable cause) {
        super(message,cause);
    }

    public UnSubscribedException(Throwable cause) {
        super(cause);
    }
}
