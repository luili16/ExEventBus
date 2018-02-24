package com.llx278.exeventbus;

import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.llx278.exeventbus.entry.SubscribeEntry1;
import com.llx278.exeventbus.entry.SubscribeEntry2;
import com.llx278.exeventbus.entry.SubscribeEntry3;
import com.llx278.exeventbus.entry.SubscribeEntry4;
import com.llx278.exeventbus.event.Event1;
import com.llx278.exeventbus.event.Event2;
import com.llx278.exeventbus.event.Event3;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static junit.framework.Assert.*;

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
     * 模拟其他的线程发布事件
     *
     * @throws Exception
     */
    @Test
    public void postToMainThread() throws Exception {
        final int N = 12;
        final CountDownLatch doneSignal = new CountDownLatch(N);
        SubscribeEntry1 subscribeEntry1 = new SubscribeEntry1(doneSignal);
        subscribeEntry1.register();
        for (int i = 0; i < N; i++) {
            if (i < 9) {
                mExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        Event1 event1 = new Event1("event1");
                        EventBus.getDefault().post(event1, "event1");
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
        subscribeEntry1.unRegister();
        Log.d("main", "done");
    }

    /**
     * 模拟其他线程发送事件，此事件被多个订阅者所订阅，测试所有订阅了此事件的订阅者都应该接收到了事件。
     *
     * @throws Exception
     */
    @Test
    public void publicEventToSubscriber() throws Exception {
        final int n = 3;
        final CountDownLatch doneSignal = new CountDownLatch(n);
        SubscribeEntry1 subscribeEntry1 = new SubscribeEntry1(doneSignal);
        SubscribeEntry2 subscribeEntry2 = new SubscribeEntry2(doneSignal);
        subscribeEntry1.register();
        subscribeEntry2.register();
        Event3 event3 = new Event3("event3");
        EventBus.getDefault().post(event3, "my_event3");
        doneSignal.await();
        subscribeEntry1.unRegister();
        subscribeEntry2.unRegister();
    }

    @Test
    public void subscribeRegister() throws Exception {


        SubscribeEntry3 subscribeEntry3 = new SubscribeEntry3(null);
        SubscribeEntry4 subscribeEntry4 = new SubscribeEntry4(null);
        EventBus.getDefault().register(subscribeEntry3);
        EventBus.getDefault().register(subscribeEntry4);
        Map subscribeMap1 = getSubscribeMap();
        assertEquals(5,subscribeMap1.size());
        EventBus.getDefault().unRegister(subscribeEntry3);
        Map subscribeMap2 = getSubscribeMap();
        assertEquals(3,subscribeMap2.size());
        EventBus.getDefault().unRegister(subscribeEntry4);
        Map subscribeMap3 = getSubscribeMap();
        assertEquals(0,subscribeMap3.size());
    }

    @Test
    public void subscribeMultiple() throws Exception {
        SubscribeEntry3 subscribeEntry3 = new SubscribeEntry3(null);
        EventBus.getDefault().register(subscribeEntry3);
        Map subMap = getSubscribeMap();
        assertEquals(2,subMap.size());
        EventBus.getDefault().register(subscribeEntry3);
        assertEquals(2,subMap.size());
    }

    private Map getSubscribeMap() throws Exception {
        EventBus eb = EventBus.getDefault();
        Class<? extends EventBus> aClass = eb.getClass();
        Field mSubScribeHolderField = aClass.getDeclaredField("mSubScribeHolder");
        mSubScribeHolderField.setAccessible(true);
        Object mSubscribeHolder = mSubScribeHolderField.get(eb);
        Field mSubscribeMapField = mSubscribeHolder.getClass().getDeclaredField("mSubscribeMap");
        return (Map) mSubscribeMapField.get(mSubscribeHolder);
    }
}
