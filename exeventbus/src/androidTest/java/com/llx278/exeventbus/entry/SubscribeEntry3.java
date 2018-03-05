package com.llx278.exeventbus.entry;

import android.util.Log;

import com.llx278.exeventbus.Subscriber;
import com.llx278.exeventbus.ThreadModel;
import com.llx278.exeventbus.event.Event2;
import com.llx278.exeventbus.event.Event5;

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

    public SubscribeEntry3() {
        this(null);
    }

    @Subscriber(model = ThreadModel.MAIN,tag = "event5")
    public void event1Method(Event5 event5) {
        assertNotNull(event5);
        assertEquals(event5.msg,"event5");
        Log.d("main","event5 finish");
        if (mDownSignal != null) {
            mDownSignal.countDown();
        }

    }

    @Subscriber(model = ThreadModel.HANDLER,tag = "event5")
    public void event1Method1(Event5 event5) {
        assertNotNull(event5);
        assertEquals(event5.msg,"event5");
        Log.d("main","event5 finish");
        if (mDownSignal != null) {
            mDownSignal.countDown();
        }
    }

    @Subscriber(model = ThreadModel.MAIN,tag = "event2")
    public void event2Method(Event2 event2) {
        assertNotNull(event2);
        assertEquals(event2.msg,"event2");
        Log.d("main","event2Method finish");
        if (mDownSignal != null) {
            mDownSignal.countDown();
        }
    }
}
