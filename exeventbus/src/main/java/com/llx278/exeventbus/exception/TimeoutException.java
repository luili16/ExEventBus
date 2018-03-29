package com.llx278.exeventbus.exception;

/**
 * Created by llx on 2018/3/1.
 */

public class TimeoutException extends Exception {
    public TimeoutException(String message) {
        super(message);
    }

    public TimeoutException() {
        super();
    }

    public TimeoutException(String message,Throwable cause) {
        super(message,cause);
    }

    public TimeoutException(Throwable cause) {
        super(cause);
    }
}
