package com.llx278.exeventbus;

import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ServiceTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.text.TextUtils;
import android.util.Log;

import com.llx278.exeventbus.entry.SubscribeEntry8;
import com.llx278.exeventbus.event.Event8;
import com.llx278.exeventbus.remote.Address;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

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
        mEventBus = new EventBus();
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
        SubscribeEntry8 subscribeEntry8 = new SubscribeEntry8(null);
        ArrayList<Event> register1 = mEventBus.register(subscribeEntry8);
        assertEquals(4, register1.size());
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
        assertNotNull(addRegisterEventList);
        assertEquals(3, addRegisterEventList.length);
        //Assert.assertArrayEquals(register1.toArray(new Event[0]),addRegisterEventList);
        ArrayList<Event> removedEvents = mEventBus.unRegister(subscribeEntry8);
        assertEquals(4,removedEvents.size());
        mRouter.remove(removedEvents);
        endTime = SystemClock.uptimeMillis() + timeout;
        while (SystemClock.uptimeMillis() < endTime) {
            Thread.sleep(50);
            addRegisterEventList = mTest9.getAddRegisterEventList(Address.createOwnAddress().toString());
            if (addRegisterEventList != null && addRegisterEventList.length == 0) {
                break;
            }
        }
        assertNotNull(addRegisterEventList);
        assertEquals(0,addRegisterEventList.length);
    }

    @Test
    public void routerTest() throws Exception {
        Event8 event8 = new Event8("event8");
        String tag = "event8";
        String returnClassName = String.class.getName();
        Event event = new Event(tag,event8.getClass().getName(),returnClassName,true);
        // 等待事件被注册
        long endTime = SystemClock.uptimeMillis() + 1000 * 2;
        while (SystemClock.uptimeMillis() < endTime) {
            Thread.sleep(50);
            ArrayList<String> addressOf = mRouter.getAddressOf(event);
            if (!addressOf.isEmpty()) {
                Log.d("main","address : " + addressOf);
                break;
            }
        }
        Object returnValue = mRouter.route(event8, tag, returnClassName, 1000 * 10);
        assertNotNull(returnValue);
        assertEquals(returnValue.getClass(),String.class);
        assertEquals("return_event8",returnValue.toString());
    }
}
