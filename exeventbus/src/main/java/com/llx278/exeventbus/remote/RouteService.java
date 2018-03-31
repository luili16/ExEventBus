package com.llx278.exeventbus.remote;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcelable;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.llx278.exeventbus.ELogger;
import com.llx278.exeventbus.Event;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 此服务用来中转进程与进程间的数据的交互
 * Created by llx on 2018/3/27.
 */

public class RouteService extends Service {

    private final IRouter.Stub mRoute = new Router();

    private final ConcurrentHashMap<String,IReceiver> mReceiverMap = new ConcurrentHashMap<>();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mRoute;
    }

    private class Router extends IRouter.Stub {

        @Override
        public void connect(String where) throws RemoteException {
        }

        @Override
        public void disConnect(String where) throws RemoteException {
        }

        @Override
        public void send(String where,String address, Bundle message) throws RemoteException {
            message.setClassLoader(getClassLoader());
            if (TextUtils.isEmpty(address)) {
                Set<String> addresses = mReceiverMap.keySet();
                for (String tempAddress : addresses) {
                    IReceiver iReceiver = mReceiverMap.get(tempAddress);
                    if (iReceiver != null) {
                        try {
                            iReceiver.onMessageReceive(where,message);
                        } catch (RemoteException ignore){
                            ELogger.e("main","",ignore);
                        }
                    }
                }
                return;
            }

            IReceiver iReceiver = mReceiverMap.get(address);
            if (iReceiver != null) {
                iReceiver.onMessageReceive(where,message);
            }
        }

        @Override
        public void addReceiveListener(String where, IReceiver listener) throws RemoteException {
            if (TextUtils.isEmpty(where) || listener == null) {
                return;
            }
            mReceiverMap.put(where,listener);
        }

        @Override
        public void removeReceiveListener(String where) throws RemoteException {
            if (TextUtils.isEmpty(where)) {
                return;
            }
            mReceiverMap.remove(where);
        }

        @Override
        public List<String> getConnectedClient(String where) throws RemoteException {

            if (TextUtils.isEmpty(where)) {
                return null;
            }

            Set<String> keyAddress = mReceiverMap.keySet();
            for (String address : keyAddress) {
                IReceiver iReceiver = mReceiverMap.get(address);
                if (iReceiver == null) {
                    mReceiverMap.remove(address);
                    continue;
                }
                try {
                    // 仅仅就是用来测试这个进程是不是存在
                    iReceiver.onMessageReceive(null,null);
                } catch (RemoteException ignore) {
                    mReceiverMap.remove(address);
                }
            }

            // 剔除请求的地址
            ArrayList<String> addressList = new ArrayList<>(mReceiverMap.keySet());
            addressList.remove(where);
            return addressList;
        }
    }
}
