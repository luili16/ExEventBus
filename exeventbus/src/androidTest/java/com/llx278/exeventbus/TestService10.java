package com.llx278.exeventbus;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Process;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;

import com.llx278.exeventbus.entry.SubscribeEntry7;
import com.llx278.exeventbus.remote.Address;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 *
 * Created by llx on 2018/3/4.
 */

public class TestService10 extends Service {

    private ExEventBus mExEventBus;

    private IRouterInteractInterface.Stub mBinder = new IRouterInteractInterface.Stub() {
        @Override
        public Event[] getAddRegisterEventList(String address) throws RemoteException {
            return null;
        }

        @Override
        public String getAddress() throws RemoteException {
            return Address.createOwnAddress().toString();
        }

        @Override
        public void killSelf() throws RemoteException {
            ExEventBus.destroy();
            Process.killProcess(Process.myPid());
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("main","testService10 onCreate");
        ExEventBus.create(this);
        mExEventBus = ExEventBus.getDefault();
        SubscribeEntry7 subscribeEntry7 = new SubscribeEntry7(null);
        mExEventBus.register(subscribeEntry7);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
