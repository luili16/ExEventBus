package com.llx278.exeventbus;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Process;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;

import com.llx278.exeventbus.entry.SubscribeEntry10;
import com.llx278.exeventbus.event.Event8;
import com.llx278.exeventbus.remote.Address;

/**
 *
 * Created by llx on 2018/3/4.
 */

public class TestService12 extends Service {

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
            Log.d("main","TestService12 接收killself");
            ExEventBus.destroy();
        }

        @Override
        public String testMethod1Result() throws RemoteException {
            return mSubscribeEntry10.mTestMethod1Tag;
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
        public void sendTo(String addrss) throws RemoteException {
            Event8 event8 = new Event8("event8_fromTestService12");
            String tag = "event8_sendTo";
            String returnClassName = void.class.getName();
            mExEventBus.remotePublish(event8,tag,returnClassName,1000 * 2);
        }
    };
    private SubscribeEntry10 mSubscribeEntry10;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("main","testService12 onCreate");
        new Thread(){
            @Override
            public void run() {
                ExEventBus.create(TestService12.this);
                mExEventBus = ExEventBus.getDefault();
                mSubscribeEntry10 = new SubscribeEntry10(null);
                mExEventBus.register(mSubscribeEntry10);
            }
        }.start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("main","TestService12 : destroy();");
    }
}
