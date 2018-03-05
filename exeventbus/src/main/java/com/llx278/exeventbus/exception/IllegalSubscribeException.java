package com.llx278.exeventbus.exception;

/**
 * Created by llx on 2018/3/5.
 */

public class IllegalSubscribeException extends RuntimeException {
    public IllegalSubscribeException(String message) {
        super(message);
    }

    public IllegalSubscribeException() {
        super();
    }

    public IllegalSubscribeException(String message,Throwable cause) {
        super(message,cause);
    }

    public IllegalSubscribeException(Throwable cause) {
        super(cause);
    }
}
