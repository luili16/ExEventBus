package com.llx278.exeventbus;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.llx278.exeventbus.entry.SubscribeEntry11;
import com.llx278.exeventbus.event.BaseEvent;
import com.llx278.exeventbus.event.Event10;
import com.llx278.exeventbus.event.Event11;
import com.llx278.exeventbus.event.Event8;
import com.llx278.exeventbus.event.Event9;
import com.llx278.exeventbus.exception.TimeoutException;
import com.llx278.exeventbus.execute.ThreadModel;
import com.llx278.exeventbus.execute.Type;
import com.llx278.exeventbus.remote.Address;

import junit.framework.Assert;

import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 *
 * Created by llx on 2018/3/4.
 */

public class TestService13 extends Service {

    private ExEventBus mExEventBus;
    private SubscribeEntry11 mSubscribeEntry11;
    private ArrayList<Holder> mEventTemp = new ArrayList<>();
    private ExecutorService mExecutor;
    private boolean mStart;

    private IRouterInteractInterface mService10;
    private IRouterInteractInterface mService11;
    private IRouterInteractInterface mService12;
    private int mCount;
    private final Object mWaitLock = new Object();

    private ConcurrentHashMap<String,String> mValueTemp = new ConcurrentHashMap<>();
    private static final String mTag = "TestService13_void_call_result";

    private ServiceConnection mService10Connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService10 = IRouterInteractInterface.Stub.asInterface(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d("main","mService10 onServiceDisconnected");
        }
    };
    private ServiceConnection mService11Connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService11 = IRouterInteractInterface.Stub.asInterface(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d("main","mService11 onServiceDisconnected");
        }
    };
    private ServiceConnection mService12Connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService12 = IRouterInteractInterface.Stub.asInterface(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d("main","mService12 onServiceDisconnected");
        }
    };


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
        public void start(int count) throws RemoteException {
            Log.d("main1","tsetService13Start");
            mCount = count;
            new Thread(){
                @Override
                public void run() {
                    execute();
                }
            }.start();
        }

        @Override
        public boolean stop() throws RemoteException {
            synchronized (mWaitLock) {
                mWaitLock.notify();
            }
            return false;
        }

        @Override
        public void sendTo(String addrss) throws RemoteException {
            Event8 event8 = new Event8("event8_fromTestService13");
            String tag = "event8_sendTo";
            String returnClassName = void.class.getName();
            try {
                mExEventBus.remotePublish(event8,tag,returnClassName,1000 * 2);
            } catch (TimeoutException e) {
                throw new RuntimeException(e);
            }
        }
    };

    private void execute() {
        final Random random = new Random(SystemClock.uptimeMillis());

        for (int i = 0;i < mCount;i++) {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            mExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    int i = random.nextInt(10);
                    if (i == 0) {
                        Holder holder = mEventTemp.get(i);
                        Holder newHolder = holder.deepCopy();
                        String body = UUID.randomUUID().toString();
                        String uuid = UUID.randomUUID().toString();
                        String msg = body + "#" + mTag + "#" + uuid;
                        newHolder.event.setMsg(msg);
                        Log.d("main","TestService13 0 event : " + newHolder.event.toString());
                        try {
                            mExEventBus.remotePublish(newHolder.event,newHolder.tag,newHolder.returnClassName,1000 * 2);
                        } catch (TimeoutException e) {
                            throw new RuntimeException(e);
                        }
                        // 等待执行结果
                        boolean received = false;
                        long endTime = SystemClock.uptimeMillis() + 1000 * 2;
                        String value10 = null;
                        String value11 = null;
                        String value12 = null;
                        try {
                            while (SystemClock.uptimeMillis() < endTime) {

                                value10 = mValueTemp.get(mService10.getAddress() + uuid);
                                value11 = mValueTemp.get(mService11.getAddress() + uuid);
                                value12 = mValueTemp.get(mService12.getAddress() + uuid);

                                if (!TextUtils.isEmpty(value10) &&
                                        !TextUtils.isEmpty(value11) &&
                                        !TextUtils.isEmpty(value12)) {
                                    received = true;
                                    break;
                                }
                            }

                            Assert.assertTrue(received);
                            Assert.assertEquals(body,value10);
                            Assert.assertEquals(body,value11);
                            Assert.assertEquals(body,value12);
                            mValueTemp.remove(mService10.getAddress());
                            mValueTemp.remove(mService11.getAddress());
                            mValueTemp.remove(mService12.getAddress());
                        }catch (RemoteException e) {
                            Log.e("main","",e);
                        }
                    } else {
                        Holder holder = mEventTemp.get(i);
                        Holder newHolder = holder.deepCopy();
                        String msg = UUID.randomUUID().toString();
                        newHolder.event.setMsg(msg);
                        Object o = null;
                        Log.d("main","TestService13 - event : " + newHolder.event.toString());
                        try {
                            o = mExEventBus.remotePublish(newHolder.event, newHolder.tag, newHolder.returnClassName, 1000 * 2);
                        } catch (TimeoutException e) {
                            throw new RuntimeException(e);
                        }
                        Assert.assertNotNull(o);
                        Assert.assertEquals(o.getClass(),String.class);
                        Assert.assertEquals("return_" + msg,o.toString());
                    }
                }
            });
        }
        Log.d("main","TestService13 关闭线程池！");
        mExecutor.shutdown();
        try {
            mExecutor.awaitTermination(1, TimeUnit.HOURS);
        } catch (InterruptedException ignore) {
        }
        synchronized (mWaitLock) {
            try {
                mWaitLock.wait();
            } catch (InterruptedException ignore) {
            }
        }
        Log.d("main1","TestService13 测试线程退出");
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("main","testService13 onCreate");
        Intent service10Intent = new Intent(this,TestService10.class);
        bindService(service10Intent,mService10Connection, Context.BIND_AUTO_CREATE);
        Intent service11Intent = new Intent(this,TestService11.class);
        bindService(service11Intent,mService11Connection,Context.BIND_AUTO_CREATE);
        Intent service12Intent = new Intent(this,TestService12.class);
        bindService(service12Intent,mService12Connection,Context.BIND_AUTO_CREATE);

        addEventList();
        mExecutor = Executors.newCachedThreadPool();
        ExEventBus.create(TestService13.this);
        mExEventBus = ExEventBus.getDefault();
        mSubscribeEntry11 = new SubscribeEntry11(null);
        mExEventBus.register(mSubscribeEntry11);
        mExEventBus.register(TestService13.this);
    }

    @Subscriber(tag = mTag,type = Type.DEFAULT,model = ThreadModel.POOL,remote = true)
    public void waitCallResult(Event8 event8) {
        String msg = event8.getMsg();
        String split[] = msg.split("#");
        String uuid = split[0];
        String address = split[1];
        mValueTemp.put(address,uuid);
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
        public Holder deepCopy() {
            return new Holder(event.deepCopy(),tag,returnClassName);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("main","TestService13 : onDestroy");
    }
}
