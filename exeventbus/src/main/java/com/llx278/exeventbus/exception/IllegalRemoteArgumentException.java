package com.llx278.exeventbus.exception;

/**
 * 当订阅一个可以跨进程的事件时，参数和返回值必须要实现Parcelable或Serializable,否则抛出此异常
 * Created by llx on 2018/3/4.
 */

public class IllegalRemoteArgumentException extends RuntimeException {
    public IllegalRemoteArgumentException(String message) {
        super(message);
    }

    public IllegalRemoteArgumentException() {
        super();
    }

    public IllegalRemoteArgumentException(String message,Throwable cause) {
        super(message,cause);
    }

    public IllegalRemoteArgumentException(Throwable cause) {
        super(cause);
    }
}
