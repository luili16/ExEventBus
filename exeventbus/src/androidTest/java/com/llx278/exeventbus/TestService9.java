package com.llx278.exeventbus;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Process;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;

import com.llx278.exeventbus.entry.SubscribeEntry7;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 *
 * Created by llx on 2018/3/4.
 */

public class TestService9 extends Service {

    private Router mRouter;

    private IRouterInteractInterface.Stub mBinder = new IRouterInteractInterface.Stub() {
        @Override
        public Event[] getAddRegisterEventList(String address) throws RemoteException {
            ConcurrentHashMap<String, CopyOnWriteArrayList<Event>> subScribeEventList = mRouter.getSubScribeEventList();
            CopyOnWriteArrayList<Event> events = subScribeEventList.get(address);
            if (events != null) {
                return  events.toArray(new Event[0]);
            }
            return null;
        }

        @Override
        public String getAddress() throws RemoteException {
            return null;
        }

        @Override
        public void killSelf() throws RemoteException {
            Process.killProcess(Process.myPid());
        }

        @Override
        public String testMethod1Result() throws RemoteException {
            return null;
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
        Log.d("main","testService9 onCreate");
        new Thread(){
            @Override
            public void run() {
                EventBus eventBus = new EventBus();
                mRouter = new Router(TestService9.this,eventBus);
                SubscribeEntry7 subscribeEntry7 = new SubscribeEntry7(null);
                ArrayList<Event> addEventList = eventBus.register(subscribeEntry7);
                mRouter.add(addEventList);
            }
        }.start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        new Thread(){
            @Override
            public void run() {
                mRouter.destroy();
            }
        }.start();
    }
}
