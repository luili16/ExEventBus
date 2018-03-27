package com.llx278.exeventbus;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;

import com.llx278.exeventbus.entry.SubscribeEntry13;

/**
 * 用来测试粘滞事件
 * Created by llx on 2018/3/21.
 */

public class TestService14 extends Service {

    private SubscribeEntry13 mEntry13;

    private IRouterInteractInterface.Stub mBinder = new IRouterInteractInterface.Stub() {
        @Override
        public Event[] getAddRegisterEventList(String address) throws RemoteException {
            return new Event[0];
        }

        @Override
        public String getAddress() throws RemoteException {
            return null;
        }

        @Override
        public void killSelf() throws RemoteException {

        }

        @Override
        public String testMethod1Result() throws RemoteException {
            return mEntry13.mTestMethod1Tag;
        }

        @Override
        public String testMethod2Result() throws RemoteException {
            return null;
        }

        @Override
        public String testMethod3Result() throws RemoteException {
            return null;
        }

        @Override
        public String testMethod4Result() throws RemoteException {
            return null;
        }

        @Override
        public void start(int count) throws RemoteException {

        }

        @Override
        public boolean stop() throws RemoteException {
            ExEventBus.getDefault().unRegister(mEntry13);
            return false;
        }

        @Override
        public void sendTo(String addrss) throws RemoteException {

        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        mEntry13 = new SubscribeEntry13();
        new Thread(){
            @Override
            public void run() {
                super.run();
                ExEventBus.create(TestService14.this);
                ExEventBus.getDefault().register(mEntry13);
            }
        }.start();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
}
