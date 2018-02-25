package com.llx278.exeventbus.exception;

/**
 *
 * Created by llx on 2018/2/25.
 */

public class UnSupportedThreadModelException extends RuntimeException {

    public UnSupportedThreadModelException(String message) {
        super(message);
    }

    public UnSupportedThreadModelException() {
        super();
    }

    public UnSupportedThreadModelException(String message,Throwable cause) {
        super(message,cause);
    }

    public UnSupportedThreadModelException(Throwable cause) {
        super(cause);
    }
}
