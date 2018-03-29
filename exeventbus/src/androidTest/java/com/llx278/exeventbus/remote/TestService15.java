package com.llx278.exeventbus.remote;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;

import com.llx278.exeventbus.IMyTestInterface;
import com.llx278.exeventbus.exception.TimeoutException;

/**
 *
 * Created by llx on 2018/3/27.
 */

public class TestService15 extends Service {


    private IMyTestInterface.Stub mTest = new IMyTestInterface.Stub() {
        @Override
        public String getProcessName() throws RemoteException {
            return null;
        }

        @Override
        public String getBroadcastStr() throws RemoteException {
            return null;
        }

        @Override
        public String getReceiveStr() throws RemoteException {
            return null;
        }

        @Override
        public void mockSendMessage(String address, Bundle message) throws RemoteException {
        }

        @Override
        public boolean mockSendMessage1(String address, Bundle message, long timeout) throws RemoteException {

            return true;
        }

        @Override
        public void mockSendBroadcast(Bundle message) throws RemoteException {
        }

        @Override
        public void clear() throws RemoteException {

        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("main","TestService15 onCreate");

        Intent service = new Intent(this,RouteService.class);
        bindService(service, new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                Log.d("main","TestService15 连接到RouterService!!!");
                IRouter iRoute = IRouter.Stub.asInterface(service);

            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        }, Context.BIND_AUTO_CREATE);
    }



    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mTest;
    }
}
