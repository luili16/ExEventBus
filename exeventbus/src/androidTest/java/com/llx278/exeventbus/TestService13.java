package com.llx278.exeventbus;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Process;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;

import com.llx278.exeventbus.entry.SubscribeEntry11;
import com.llx278.exeventbus.entry.SubscribeEntry7;
import com.llx278.exeventbus.event.BaseEvent;
import com.llx278.exeventbus.event.Event10;
import com.llx278.exeventbus.event.Event11;
import com.llx278.exeventbus.event.Event8;
import com.llx278.exeventbus.event.Event9;
import com.llx278.exeventbus.remote.Address;

import java.util.ArrayList;
import java.util.UUID;

/**
 *
 * Created by llx on 2018/3/4.
 */

public class TestService13 extends Service {

    private ExEventBus mExEventBus;

    private ArrayList<Holder> mEventTemp = new ArrayList<>();

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
            Log.d("main","TestService13 接收killself");
            ExEventBus.destroy();
        }

        @Override
        public String testMethod1Result() throws RemoteException {
            return mSubscribeEntry11.mTestMethod1Tag;
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
        public void start() throws RemoteException {

        }

        @Override
        public void stop() throws RemoteException {

        }

        @Override
        public void sendTo(String addrss) throws RemoteException {
            Event8 event8 = new Event8("event8_fromTestService13");
            String tag = "event8_sendTo";
            String returnClassName = void.class.getName();
            mExEventBus.remotePublish(event8,tag,returnClassName,1000 * 2);
        }
    };
    private SubscribeEntry11 mSubscribeEntry11;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("main","testService13 onCreate");
        addEventList();
        new Thread(){
            @Override
            public void run() {
                ExEventBus.create(TestService13.this);
                mExEventBus = ExEventBus.getDefault();
                mSubscribeEntry11 = new SubscribeEntry11(null);
                mExEventBus.register(mSubscribeEntry11);
            }
        }.start();

    }

    private void addEventList() {

        mEventTemp.add(new Holder(new Event8(UUID.randomUUID().toString()),"event8",void.class.getName()));

        mEventTemp.add(new Holder(new Event9(UUID.randomUUID().toString()),"event9_SubscribeEntry8",String.class.getName()));
        mEventTemp.add(new Holder(new Event10(UUID.randomUUID().toString()),"event10_SubscribeEntry8",String.class.getName()));
        mEventTemp.add(new Holder(new Event11(UUID.randomUUID().toString()),"event11_SubscribeEntry8",String.class.getName()));

        mEventTemp.add(new Holder(new Event9(),"event9_SubscribeEntry9",String.class.getName()));
        mEventTemp.add(new Holder(new Event10(),"event10_SubscribeEntry9",String.class.getName()));
        mEventTemp.add(new Holder(new Event11(),"event11_SubscribeEntry9",String.class.getName()));

        mEventTemp.add(new Holder(new Event9(),"event9_SubscribeEntry10",String.class.getName()));
        mEventTemp.add(new Holder(new Event10(),"event10_SubscribeEntry10",String.class.getName()));
        mEventTemp.add(new Holder(new Event11(),"event11_SubscribeEntry10",String.class.getName()));

        /*mEventTemp.add(new Holder(new Event9(),"event9_SubscribeEntry11",String.class.getName()));
        mEventTemp.add(new Holder(new Event10(),"event10_SubscribeEntry11",String.class.getName()));
        mEventTemp.add(new Holder(new Event11(),"event11_SubscribeEntry11",String.class.getName()));*/

    }

    private class Holder {
        final BaseEvent event;
        final String tag;
        final String returnClassName;
        Holder(BaseEvent event,String tag,String returnClassName) {
            this.event =event;
            this.tag = tag;
            this.returnClassName = returnClassName;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("main","TestService13 : onDestroy");
    }
}
