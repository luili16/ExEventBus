package com.llx278.exeventbus.entry;

import android.util.Log;

import com.llx278.exeventbus.EventBusImpl;
import com.llx278.exeventbus.Subscriber;
import com.llx278.exeventbus.ThreadModel;
import com.llx278.exeventbus.event.Event1;
import com.llx278.exeventbus.event.Event3;

import java.util.concurrent.CountDownLatch;

import static junit.framework.Assert.*;

/**
 * 测试执行默认tag
 * Created by llx on 2018/2/6.
 */

public class SubscribeEntry1 {

    private final CountDownLatch mDownSignal;

    public SubscribeEntry1(CountDownLatch downSignal) {
        mDownSignal = downSignal;
    }
    public SubscribeEntry1() {
        this(null);
    }

    public void register() {
        EventBusImpl.getDefault().register(this);
    }

    public void unRegister() {
        EventBusImpl.getDefault().unRegister(this);
    }

    @Subscriber(model = ThreadModel.MAIN,tag = "event1")
    public void event1Method(Event1 event1) {
        assertNotNull(event1);
        assertEquals(event1.msg,"event1");
        Log.d("main","event1Method finish");
        if (mDownSignal != null) {
            mDownSignal.countDown();
        }
    }

    @Subscriber(model = ThreadModel.HANDLER,tag = "event3")
    public void event3Method1(Event3 event3) {
        assertNotNull(event3);
        assertEquals(event3.msg,"event3");
        Log.d("main","event3Method1 has called!");
        if (mDownSignal != null) {
            mDownSignal.countDown();
        }
    }

    @Subscriber(model = ThreadModel.POOL,tag = "event3")
    public void event3Method(Event3 event3) {
        assertNotNull(event3);
        assertEquals(event3.msg,"event3");
        Log.d("main","SubscribeEntry1 event3Method has called!");
        if (mDownSignal != null) {
            mDownSignal.countDown();
        }
    }
}
