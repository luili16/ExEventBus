package com.llx278.exeventbus.entry;

import android.util.Log;

import com.llx278.exeventbus.EventBus;
import com.llx278.exeventbus.Subscriber;
import com.llx278.exeventbus.ThreadModel;
import com.llx278.exeventbus.event.Event1;
import com.llx278.exeventbus.event.Event2;
import com.llx278.exeventbus.event.Event3;

import java.util.concurrent.CountDownLatch;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

/**
 * 测试执行默认tag
 * Created by llx on 2018/2/6.
 */

public class SubscribeEntry3 {

    private final CountDownLatch mDownSignal;

    public SubscribeEntry3(CountDownLatch downSignal) {
        mDownSignal = downSignal;
    }

    public void register() {
        EventBus.getDefault().register(this);
    }

    public void unRegister() {
        EventBus.getDefault().unRegister(this);
    }

    @Subscriber(mode = ThreadModel.MAIN,tag = "subscribe_entry3_event1")
    public void event1Method(Event1 event1) {
        assertNotNull(event1);
        assertEquals(event1.msg,"event1");
        Log.d("main","event1Method finish");
        mDownSignal.countDown();
    }

    @Subscriber(mode = ThreadModel.HANDLER,tag = "subscribe_entry3_event1")
    public void event1Method1(Event1 event1) {
        assertNotNull(event1);
        assertEquals(event1.msg,"event1");
        Log.d("main","event2Method finish");
        mDownSignal.countDown();
    }

    @Subscriber(mode = ThreadModel.MAIN,tag = "subscribe_entry3_event2")
    public void event2Method(Event2 event2) {
        assertNotNull(event2);
        assertEquals(event2.msg,"event2");
        Log.d("main","event3Method finish");
        mDownSignal.countDown();
    }
}
