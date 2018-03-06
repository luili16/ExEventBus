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

import com.llx278.exeventbus.entry.SubscribeEntry12;
import com.llx278.exeventbus.event.Event8;
import com.llx278.exeventbus.event.Event9;
import com.llx278.exeventbus.remote.Address;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.Field;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import static junit.framework.Assert.*;

/**
 * Created by llx on 2018/3/5.
 */

@RunWith(AndroidJUnit4.class)
public class ExEventBusTest {

    private static IRouterInteractInterface mTest10;
    private static IRouterInteractInterface mTest11;
    private static IRouterInteractInterface mTest12;
    private static IRouterInteractInterface mTest13;
    private static ExEventBus mExEventBus;

    private static String mAddress10;
    private static String mAddress11;
    private static String mAddress12;
    private static String mAddress13;

    private static SubscribeEntry12 mSubscribeEntry12;

    private static long mDefaultTimeout = 1000 * 2;
    @ClassRule
    public static final ServiceTestRule mServiceRule = new ServiceTestRule();

    @BeforeClass
    public static void before() throws Exception {
        Context context = InstrumentationRegistry.getTargetContext();
        Intent intent10 = new Intent(context, TestService10.class);
        IBinder binder10 = mServiceRule.bindService(intent10);
        mTest10 = IRouterInteractInterface.Stub.asInterface(binder10);

        Intent intent11 = new Intent(context,TestService11.class);
        IBinder binder11 = mServiceRule.bindService(intent11);
        mTest11 = IRouterInteractInterface.Stub.asInterface(binder11);

        Intent intent12 = new Intent(context,TestService12.class);
        IBinder binder12 = mServiceRule.bindService(intent12);
        mTest12 = IRouterInteractInterface.Stub.asInterface(binder12);

        Intent intent13 = new Intent(context,TestService13.class);
        IBinder binder13 = mServiceRule.bindService(intent13);
        mTest13 = IRouterInteractInterface.Stub.asInterface(binder13);

        ExEventBus.create(context);
        mExEventBus = ExEventBus.getDefault();

        // 等待收到其他进程发送订阅事件
         mAddress10= mTest10.getAddress();
         mAddress11= mTest11.getAddress();
         mAddress12= mTest12.getAddress();
         mAddress13= mTest13.getAddress();
        long endTime = SystemClock.uptimeMillis() + mDefaultTimeout;
        // 如果2s内没有收到其他进程的
        Router router = getRouterObj();
        boolean received = false;
        while (SystemClock.uptimeMillis() < endTime) {
            Thread.sleep(50);
            ConcurrentHashMap<String, CopyOnWriteArrayList<Event>> subScribeEventList =
                    router.getSubScribeEventList();
            CopyOnWriteArrayList<Event> address10Events = subScribeEventList.get(mAddress10);
            CopyOnWriteArrayList<Event> address11Events = subScribeEventList.get(mAddress11);
            CopyOnWriteArrayList<Event> address12Events = subScribeEventList.get(mAddress12);
            CopyOnWriteArrayList<Event> address13Events = subScribeEventList.get(mAddress13);
            if (address10Events != null && !address10Events.isEmpty()&&
                    address11Events != null && !address11Events.isEmpty()&&
                    address12Events != null && !address12Events.isEmpty()&&
                    address13Events != null && !address13Events.isEmpty()
                    ) {
                received = true;
                break;
            }
        }
        Log.d("main","received : " + received);
        mSubscribeEntry12 = new SubscribeEntry12();
        mExEventBus.register(mSubscribeEntry12);
        assertTrue(received);
    }

    @AfterClass
    public static void after() throws Exception {
        // 通知其他的测试进程退出
        mTest10.killSelf();
        mTest11.killSelf();
        mTest12.killSelf();
        mTest13.killSelf();

        // 确定已经接收到了所有其他进程的退出消息
        Router router = getRouterObj();
        boolean destroy = false;
        long endTime = SystemClock.uptimeMillis() + 1000 * 5;
        while (SystemClock.uptimeMillis() < endTime) {
            Thread.sleep(50);
            ConcurrentHashMap<String, CopyOnWriteArrayList<Event>> subScribeEventList =
                    router.getSubScribeEventList();
            CopyOnWriteArrayList<Event> address10Events = subScribeEventList.get(mAddress10);
            CopyOnWriteArrayList<Event> address11Events = subScribeEventList.get(mAddress11);
            CopyOnWriteArrayList<Event> address12Events = subScribeEventList.get(mAddress12);
            CopyOnWriteArrayList<Event> address13Events = subScribeEventList.get(mAddress13);
            if (address10Events == null || address10Events.isEmpty()&&
                    address11Events == null || address11Events.isEmpty()&&
                    address12Events == null || address12Events.isEmpty()&&
                    address13Events == null || address13Events.isEmpty()
                    ) {
                destroy = true;
                break;
            }
        }

        Log.d("main","destroy : "+ destroy);
        assertTrue(destroy);
        ExEventBus.destroy();
    }

    private static Router getRouterObj() throws Exception {
        Field mRouterField = ExEventBus.class.
                getDeclaredField("mRouter");
        mRouterField.setAccessible(true);
        return (Router) mRouterField.get(mExEventBus);
    }

    /**
     * 测试发布一个不需要返回值的消息到其他进程
     */
    @Test
    public void publishToOtherProcess() throws Exception {
        Event8 event8 = new Event8("event8");
        String tag = "event8";
        mExEventBus.remotePublish(event8,tag,void.class.getName(),1000 * 2);

        // 确认其他进程都收到了发送的消息
        String result10 = null;
        String result11 = null;
        String result12 = null;
        String result13 = null;
        long endTime = SystemClock.uptimeMillis() + mDefaultTimeout;

        while (SystemClock.uptimeMillis() < endTime) {
            Thread.sleep(50);
            result10 = mTest10.testMethod1Result();
            result11 = mTest11.testMethod1Result();
            result12 = mTest12.testMethod1Result();
            result13 = mTest13.testMethod1Result();
            if (!TextUtils.isEmpty(result10) &&
                    !TextUtils.isEmpty(result11) &&
                    !TextUtils.isEmpty(result12) &&
                    !TextUtils.isEmpty(result13)) {
                break;
            }
        }
        assertEquals("event8",result10);
        assertEquals("event8",result11);
        assertEquals("event8",result12);
        assertEquals("event8",result13);
    }

    /**
     * 测试发布一个需要返回值的消息到其他进程
     */
    @Test
    public void publishToOtherProcessAndWaitReturn() {
        Event9 event9 = new Event9("event9_SubscribeEntry8");
        String tag = "event9_SubscribeEntry8";
        // 分别发送消息给其他4个进程
        String returnClassName = String.class.getName();
        Object returnValue = mExEventBus.remotePublish(event9, tag, returnClassName, mDefaultTimeout);
        assertNotNull(returnValue);
        assertEquals(returnValue.getClass(),String.class);
        assertEquals("return_event9_SubscribeEntry8",returnValue.toString());

        event9 = new Event9("event9_SubscribeEntry9");
        tag = "event9_SubscribeEntry9";
        returnClassName = String.class.getName();
        returnValue = mExEventBus.remotePublish(event9,tag,returnClassName,mDefaultTimeout);
        assertNotNull(returnValue);
        assertEquals(returnValue.getClass(),String.class);
        assertEquals("return_event9_SubscribeEntry9",returnValue.toString());

        event9 = new Event9("event9_SubscribeEntry10");
        tag = "event9_SubscribeEntry10";
        returnClassName = String.class.getName();
        returnValue = mExEventBus.remotePublish(event9,tag,returnClassName,mDefaultTimeout);
        assertNotNull(returnValue);
        assertEquals(returnValue.getClass(),String.class);
        assertEquals("return_event9_SubscribeEntry10",returnValue.toString());

        event9 = new Event9("event9_SubscribeEntry11");
        tag = "event9_SubscribeEntry11";
        returnClassName = String.class.getName();
        returnValue = mExEventBus.remotePublish(event9,tag,returnClassName,mDefaultTimeout);
        assertNotNull(returnValue);
        assertEquals(returnValue.getClass(),String.class);
        assertEquals("return_event9_SubscribeEntry11",returnValue.toString());
    }

    /**
     * 测试其他的进程发布一个消息到此进程
     */
    @Test
    public void otherProcessPublishToHere() throws Exception {

        mSubscribeEntry12.mTestMethod1Tag = null;
        mTest10.sendTo(Address.createOwnAddress().toString());
        waitNotEmpty();
        assertEquals("event8_fromTestService10",mSubscribeEntry12.mTestMethod1Tag);


        mSubscribeEntry12.mTestMethod1Tag = null;
        mTest11.sendTo(Address.createOwnAddress().toString());
        waitNotEmpty();
        assertEquals("event8_fromTestService11",mSubscribeEntry12.mTestMethod1Tag);

        mSubscribeEntry12.mTestMethod1Tag = null;
        mTest12.sendTo(Address.createOwnAddress().toString());
        waitNotEmpty();
        assertEquals("event8_fromTestService12",mSubscribeEntry12.mTestMethod1Tag);

        mSubscribeEntry12.mTestMethod1Tag = null;
        mTest13.sendTo(Address.createOwnAddress().toString());
        waitNotEmpty();
        assertEquals("event8_fromTestService13",mSubscribeEntry12.mTestMethod1Tag);
    }

    private void waitNotEmpty() throws Exception {
        long endTIme = SystemClock.uptimeMillis() + mDefaultTimeout;
        while (SystemClock.uptimeMillis() < endTIme) {
            Thread.sleep(50);
            if (!TextUtils.isEmpty(mSubscribeEntry12.mTestMethod1Tag)) {
                break;
            }
        }
    }

    /**
     * 测试进程彼此之间互相发送消息
     */
    public void publishToEachOther() {



    }
}
