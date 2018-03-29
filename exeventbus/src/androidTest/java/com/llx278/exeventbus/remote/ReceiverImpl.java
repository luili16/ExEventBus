package com.llx278.exeventbus.remote;

import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

/**
 * Created by llx on 2018/3/27.
 */

public class ReceiverImpl extends IReceiver.Stub {

    @Override
    public void onMessageReceive(String where, Bundle message) throws RemoteException {
        Log.d("main","接收到 onMessageReceive!!!");
    }
}
