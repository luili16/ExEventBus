package com.llx278.exeventbus;

import android.util.Log;

import com.llx278.exeventbus.EventBus;
import com.llx278.exeventbus.Subscriber;
import com.llx278.exeventbus.ThreadModel;
import com.llx278.exeventbus.event.Event1;
import com.llx278.exeventbus.event.Event2;
import com.llx278.exeventbus.event.Event3;

import junit.framework.Assert;

import java.util.concurrent.CountDownLatch;

import static junit.framework.Assert.*;

/**
 * 测试执行默认tag
 * Created by llx on 2018/2/6.
 */

public class SubscribeEntry {

    private final CountDownLatch mDownSignal;

    public SubscribeEntry(CountDownLatch downSignal) {
        mDownSignal = downSignal;
    }

    public void register() {
        EventBus.getDefault().register(this);
    }

    public void unRegister() {
        EventBus.getDefault().unRegister(this);
    }

    @Subscriber(mode = ThreadModel.MAIN,tag = "event1")
    public void event1Method(Event1 event1) {
        assertNotNull(event1);
        assertEquals(event1.msg,"event1");
        Log.d("main","event1Method finish");
        mDownSignal.countDown();
    }

    @Subscriber(mode = ThreadModel.HANDLER)
    public void event2Method(Event2 event2) {
        assertNotNull(event2);
        assertEquals(event2.msg,"event2");
        Log.d("main","event2Method finish");
        mDownSignal.countDown();
    }

    @Subscriber(mode = ThreadModel.MAIN)
    public void event3Method(Event3 event3) {
        assertNotNull(event3);
        assertEquals(event3.msg,"event3");
        Log.d("main","event3Method finish");
        mDownSignal.countDown();
    }

}
