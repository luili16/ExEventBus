package com.llx278.exeventbus.remote;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.TextUtils;

import com.llx278.exeventbus.ELogger;

import java.util.ArrayList;

/**
 * 对IMockPhysicalLayer的具体实现
 * Created by llx on 2018/2/28.
 */

public class MockPhysicalLayer implements IMockPhysicalLayer {

    private Context mContext;
    private Receiver mListener;
    private IRouter mRoute;
    private IReceiver mReceiver;
    private RouteServiceConnection mConnection;

    public MockPhysicalLayer(Context context) {
        mContext = context;
        mReceiver = new RouteReceiver();
        mConnection = new RouteServiceConnection();
        register();
    }

    @Override
    public void init() {
    }

    @Override
    public void destroy() {
        mContext.unbindService(mConnection);
        mContext = null;
    }

    private void register() {
        Intent routeIntent = new Intent("com.llx278.exeventbus.sync");
        String pkg = mContext.getPackageName();
        String cls = "com.llx278.exeventbus.remote.RouteService";
        ComponentName componentName = new ComponentName(pkg,cls);
        routeIntent.setComponent(componentName);
        boolean success = mContext.bindService(routeIntent, mConnection, Context.BIND_AUTO_CREATE);
        if (!success) {
            ELogger.d("bind route service failed");
        }
    }

    @Override
    public void send(String address, Bundle message) {

        if (mRoute != null) {
            try {
                String where = Address.createOwnAddress().toString();
                mRoute.send(where, address, message);
            } catch (RemoteException e) {
                ELogger.e("", e);
            }
        }
    }

    @Override
    public void setOnReceiveListener(Receiver listener) {
        mListener = listener;
    }

    @Override
    public ArrayList<String> getAvailableAddress(String where) {
        if (mRoute != null) {
            try {
                return new ArrayList<>(mRoute.getConnectedClient(where));
            } catch (RemoteException ignore) {
                ELogger.e("", ignore);
            }
        }
        return null;
    }

    private class RouteServiceConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mRoute = IRouter.Stub.asInterface(service);
            String where = Address.createOwnAddress().toString();
            try {
                mRoute.addReceiveListener(where, mReceiver);
            } catch (RemoteException e) {
                ELogger.e("", e);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    }

    private class RouteReceiver extends IReceiver.Stub {

        @Override
        public void onMessageReceive(String where, Bundle message) throws RemoteException {

            if (TextUtils.isEmpty(where) || message == null) {
                return;
            }
            message.setClassLoader(getClass().getClassLoader());
            String ownAddress = Address.createOwnAddress().toString();
            if (ownAddress.equals(where)) {
                return;
            }
            if (mListener != null) {
                mListener.onMessageReceive(where, message);
            }
        }
    }
}
