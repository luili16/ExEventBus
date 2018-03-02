package com.llx278.exeventbus.exception;

/**
 * Created by llx on 2018/3/2.
 */

public class RemoteException extends RuntimeException {

    public RemoteException(String message) {
        super(message);
    }

    public RemoteException() {
        super();
    }

    public RemoteException(String message,Throwable cause) {
        super(message,cause);
    }

    public RemoteException(Throwable cause) {
        super(cause);
    }

}
