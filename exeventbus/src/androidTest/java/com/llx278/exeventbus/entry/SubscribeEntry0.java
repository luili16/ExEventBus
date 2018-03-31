package com.llx278.exeventbus.entry;

import android.util.Log;

import com.llx278.exeventbus.Subscriber;
import com.llx278.exeventbus.execute.ThreadModel;
import com.llx278.exeventbus.execute.Type;

import java.util.concurrent.CountDownLatch;

/**
 * Created by liu on 18-3-31.
 */

public class SubscribeEntry0 {

    private final CountDownLatch mDownSignal;


    public SubscribeEntry0(CountDownLatch downSignal) {
        mDownSignal = downSignal;
    }
    public SubscribeEntry0() {
        this(null);
    }
    @Subscriber(model = ThreadModel.POOL,tag = "event_void")
    public void event4Method() {
        Log.d("main","SubscribeEntry1 event4Method has called!");
        if (mDownSignal != null) {
            mDownSignal.countDown();
        }
    }

    @Subscriber(model = ThreadModel.POOL,tag = "event_void",type = Type.BLOCK_RETURN)
    public String event5Method() {
        Log.d("main","SubscribeEntry1 event5Method has called!");
        return "event5Method";
    }
}
