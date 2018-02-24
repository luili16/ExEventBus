package com.llx278.exeventbus.entry;

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

public class SubscribeEntry1 {

    private final CountDownLatch mDownSignal;

    public SubscribeEntry1(CountDownLatch downSignal) {
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

    @Subscriber(mode = ThreadModel.HANDLER,tag = "my_event3")
    public void event3Method1(Event3 event3) {
        assertNotNull(event3);
        assertEquals(event3.msg,"event3");
        Log.d("main","event3Method1 has called!");
        mDownSignal.countDown();
    }

    @Subscriber(mode = ThreadModel.MAIN,tag = "my_event3")
    public void event3Method(Event3 event3) {
        assertNotNull(event3);
        assertEquals(event3.msg,"event3");
        Log.d("main","SubscribeEntry1 event3Method has called!");
        mDownSignal.countDown();
    }
}
