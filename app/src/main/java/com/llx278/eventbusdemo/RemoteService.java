package com.llx278.eventbusdemo;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Process;
import android.support.annotation.Nullable;
import android.util.Log;

import com.llx278.exeventbus.ExEventBus;
import com.llx278.exeventbus.Subscriber;
import com.llx278.exeventbus.exception.TimeoutException;

/**
 * Created by liu on 18-3-31.
 */

public class RemoteService extends Service {

    private Thread mThread;

    @Override
    public void onCreate() {
        super.onCreate();
        ExEventBus.getDefault().register(this);
        mThread = new MyThread();
        mThread.start();
    }

    @Subscriber(tag = "toggle",remote = true)
    public void toggle(boolean start) {
        Log.d("main","toggle : " + start);
        if (start) {
            mThread = new MyThread();
            mThread.start();
        } else {
            mThread.interrupt();
            mThread = null;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private class MyThread extends Thread {
        @Override
        public void run() {
            long i = 0;
            while (true) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ignore) {
                    break;
                }

                String msg = "count" + (i++);
                Log.d("main","远程发布 : " + msg);
                Event event = new Event(msg, Process.myPid());
                try {
                    ExEventBus.getDefault().remotePublish(event,"remoteReceiveMethod",2000);
                } catch (TimeoutException e) {
                    Log.e("main","",e);
                }
            }
        }
    }
}
