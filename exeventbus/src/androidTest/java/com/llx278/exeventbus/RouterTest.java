package com.llx278.exeventbus;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcelable;
import android.os.SystemClock;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ServiceTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.llx278.exeventbus.entry.SubscribeEntry1;
import com.llx278.exeventbus.entry.SubscribeEntry2;
import com.llx278.exeventbus.entry.SubscribeEntry7;
import com.llx278.exeventbus.event.Event8;
import com.llx278.exeventbus.remote.Address;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;

/**
 * 对Router类进行测试
 * Created by llx on 2018/3/4.
 */

@RunWith(AndroidJUnit4.class)
public class RouterTest {

    private Router mRouter;
    EventBus mEventBus;

    IRouterInteractInterface mTest9;

    @Rule
    public final ServiceTestRule mServiceRule = new ServiceTestRule();

    @Before
    public void before() throws Exception {
        mEventBus = EventBusImpl.getDefault();
        Context context = InstrumentationRegistry.getTargetContext();
        mRouter = new Router(context, mEventBus);
        Intent serviceIntent = new Intent(context, TestService9.class);
        IBinder binder = mServiceRule.bindService(serviceIntent);
        mTest9 = IRouterInteractInterface.Stub.asInterface(binder);
    }

    @After
    public void after() {
    }

    @Test
    public void registerUnregisterToRemoteTest() throws Exception {
        SubscribeEntry1 subscribeEntry1 = new SubscribeEntry1(null);
        ArrayList<Event> register1 = mEventBus.register(subscribeEntry1);
        Assert.assertEquals(2, register1.size());
        mRouter.add(register1);
        long timeout = 1000;
        Event[] addRegisterEventList = null;
        long endTime = SystemClock.uptimeMillis() + timeout;
        while (SystemClock.uptimeMillis() < endTime) {
            Thread.sleep(50);
            addRegisterEventList = mTest9.getAddRegisterEventList(Address.createOwnAddress().toString());
            if (addRegisterEventList != null && addRegisterEventList.length != 0) {
                break;
            }
        }
        Assert.assertNotNull(addRegisterEventList);
        Assert.assertEquals(2, addRegisterEventList.length);
        Assert.assertArrayEquals(register1.toArray(new Event[0]),addRegisterEventList);
        ArrayList<Event> removedEvents = mEventBus.unRegister(subscribeEntry1);
        Assert.assertEquals(2,removedEvents.size());
        mRouter.remove(removedEvents);
        endTime = SystemClock.uptimeMillis() + timeout;
        while (SystemClock.uptimeMillis() < endTime) {
            Thread.sleep(50);
            addRegisterEventList = mTest9.getAddRegisterEventList(Address.createOwnAddress().toString());
            if (addRegisterEventList != null && addRegisterEventList.length == 0) {
                break;
            }
        }
        Assert.assertNotNull(addRegisterEventList);
        Assert.assertEquals(0,addRegisterEventList.length);
    }

    @Test
    public void routerTest() {
        SubscribeEntry7 subscribeEntry7 = new SubscribeEntry7(null);
        ArrayList<Event> register7 = mEventBus.register(subscribeEntry7);
        mRouter.add(register7);
        Event8 event8 = new Event8("event8");
        String tag = "event8";
        String returnClassName = String.class.getName();
        //Parcelable returnValue = mRouter.route(event8, tag, returnClassName, 1000 * 2);
        //Assert.assertNotNull(returnValue);


    }
}
