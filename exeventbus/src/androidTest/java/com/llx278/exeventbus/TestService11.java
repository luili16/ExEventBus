package com.llx278.exeventbus;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Process;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;

import com.llx278.exeventbus.entry.SubscribeEntry7;
import com.llx278.exeventbus.entry.SubscribeEntry8;
import com.llx278.exeventbus.entry.SubscribeEntry9;
import com.llx278.exeventbus.remote.Address;

/**
 *
 * Created by llx on 2018/3/4.
 */

public class TestService11 extends Service {

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
        Log.d("main","testService11 onCreate");
        ExEventBus.create(this);
        mExEventBus = ExEventBus.getDefault();
        SubscribeEntry9 subscribeEntry7 = new SubscribeEntry9(null);
        mExEventBus.register(subscribeEntry7);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ExEventBus.destroy();
    }
}
