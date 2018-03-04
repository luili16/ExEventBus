package com.llx278.exeventbus;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;

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
        EventBus eventBus = EventBusImpl.getDefault();
        mRouter = new Router(this,eventBus);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
