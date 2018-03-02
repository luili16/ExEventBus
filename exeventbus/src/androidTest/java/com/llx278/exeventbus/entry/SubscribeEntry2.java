package com.llx278.exeventbus.entry;

import android.util.Log;

import com.llx278.exeventbus.EventBusImpl;
import com.llx278.exeventbus.Subscriber;
import com.llx278.exeventbus.ThreadModel;
import com.llx278.exeventbus.event.Event2;
import com.llx278.exeventbus.event.Event3;
import com.llx278.exeventbus.event.Event4;

import java.util.concurrent.CountDownLatch;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

/**
 * 测试执行默认tag
 * Created by llx on 2018/2/6.
 */

public class SubscribeEntry2 {

    private final CountDownLatch mDownSignal;

    public SubscribeEntry2(CountDownLatch downSignal) {
        mDownSignal = downSignal;
    }
    public SubscribeEntry2() {
        this(null);
    }

    public void register() {
        EventBusImpl.getDefault().register(this);
    }

    public void unRegister() {
        EventBusImpl.getDefault().unRegister(this);
    }

    @Subscriber(model = ThreadModel.HANDLER,tag = "event3")
    public void event3Method(Event3 event3) {
        assertNotNull(event3);
        assertEquals(event3.msg,"event3");
        Log.d("main","SubscribeEntry2 event3Method has called!");
        if (mDownSignal != null) {
            mDownSignal.countDown();
        }
    }

    @Subscriber(model = ThreadModel.POOL,tag = "event4")
    public void event4Method(Event4 event4) {
        assertNotNull(event4);
        assertEquals(event4.msg,"event4");
        Log.d("main","event4Method finish");
        if (mDownSignal != null) {
            mDownSignal.countDown();
        }
    }

    @Subscriber(model = ThreadModel.MAIN,tag = "event2")
    public void event5Method(Event2 event2) {
        assertNotNull(event2);
        assertEquals(event2.msg,"event2");
        Log.d("main","event2Method finish");
        if (mDownSignal != null) {
            mDownSignal.countDown();
        }
    }
}
