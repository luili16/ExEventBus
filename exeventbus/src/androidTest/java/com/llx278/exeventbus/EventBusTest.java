package com.llx278.exeventbus;

import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.llx278.exeventbus.event.Event1;
import com.llx278.exeventbus.event.Event2;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class EventBusTest {

    private ExecutorService mExecutor;

    public EventBusTest() {
    }


    @Before
    public void setUp() {
        mExecutor = Executors.newFixedThreadPool(10);
    }

    @After
    public void tearDown() {
        mExecutor.shutdown();
    }

    /**
     * 模拟其他的线程同时向主线程发布事件
     * @throws Exception
     */
    @Test
    public void postToMainThread() throws Exception {
        final int N = 12;
        final CountDownLatch doneSignal = new CountDownLatch(N);
        SubscribeEntry subscribeEntry = new SubscribeEntry(doneSignal);
        subscribeEntry.register();
        for (int i = 0; i < N;i++) {
            if (i < 9) {
                mExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        Event1 event1 = new Event1("event1");
                        EventBus.getDefault().post(event1,"event1");
                    }
                });
            } else {
                mExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        Event2 event2 = new Event2("event2");
                        EventBus.getDefault().post(event2);
                    }
                });
            }
        }
        doneSignal.await();
        subscribeEntry.unRegister();
        Log.d("main","done");
    }
}
