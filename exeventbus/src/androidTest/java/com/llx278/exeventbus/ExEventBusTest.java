package com.llx278.exeventbus;

import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.SystemClock;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ServiceTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.text.TextUtils;
import android.util.Log;

import com.llx278.exeventbus.entry.SubscribeEntry12;
import com.llx278.exeventbus.event.BaseEvent;
import com.llx278.exeventbus.event.Event10;
import com.llx278.exeventbus.event.Event11;
import com.llx278.exeventbus.event.Event8;
import com.llx278.exeventbus.event.Event9;
import com.llx278.exeventbus.remote.Address;

import junit.framework.Assert;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import static junit.framework.Assert.*;

/**
 *
 * Created by llx on 2018/3/5.
 */

@RunWith(AndroidJUnit4.class)
public class ExEventBusTest {

    private static IRouterInteractInterface sTest10;
    private static IRouterInteractInterface sTest11;
    private static IRouterInteractInterface sTest12;
    private static IRouterInteractInterface sTest13;
    private static ExEventBus sExEventBus;

    private static String sAddress10;
    private static String sAddress11;
    private static String sAddress12;
    private static String sAddress13;

    private static final String M_TAG = "ExEventBusTest_void_call_result";

    private static SubscribeEntry12 sSubscribeEntry12;

    private static long sDefaultTimeout = 1000 * 2;

    private static ArrayList<Holder> sEventTemp = new ArrayList<>();

    @ClassRule
    public static final ServiceTestRule sServiceRule = new ServiceTestRule();

    private static ConcurrentHashMap<String,String> sValueTemp = new ConcurrentHashMap<>();

    @BeforeClass
    public static void before() throws Exception {
        Context context = InstrumentationRegistry.getTargetContext();
        Intent intent10 = new Intent(context, TestService10.class);
        IBinder binder10 = sServiceRule.bindService(intent10);
        sTest10 = IRouterInteractInterface.Stub.asInterface(binder10);

        Intent intent11 = new Intent(context,TestService11.class);
        IBinder binder11 = sServiceRule.bindService(intent11);
        sTest11 = IRouterInteractInterface.Stub.asInterface(binder11);

        Intent intent12 = new Intent(context,TestService12.class);
        IBinder binder12 = sServiceRule.bindService(intent12);
        sTest12 = IRouterInteractInterface.Stub.asInterface(binder12);

        Intent intent13 = new Intent(context,TestService13.class);
        IBinder binder13 = sServiceRule.bindService(intent13);
        sTest13 = IRouterInteractInterface.Stub.asInterface(binder13);

        ExEventBus.create(context);
        sExEventBus = ExEventBus.getDefault();

        // 等待收到其他进程发送订阅事件
         sAddress10 = sTest10.getAddress();
         sAddress11 = sTest11.getAddress();
         sAddress12 = sTest12.getAddress();
         sAddress13 = sTest13.getAddress();
        long endTime = SystemClock.uptimeMillis() + sDefaultTimeout;
        // 如果2s内没有收到其他进程的
        Router router = getRouterObj();
        boolean received = false;
        while (SystemClock.uptimeMillis() < endTime) {
            Thread.sleep(50);
            ConcurrentHashMap<String, CopyOnWriteArrayList<Event>> subScribeEventList =
                    router.getSubScribeEventList();
            CopyOnWriteArrayList<Event> address10Events = subScribeEventList.get(sAddress10);
            CopyOnWriteArrayList<Event> address11Events = subScribeEventList.get(sAddress11);
            CopyOnWriteArrayList<Event> address12Events = subScribeEventList.get(sAddress12);
            CopyOnWriteArrayList<Event> address13Events = subScribeEventList.get(sAddress13);
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
        sSubscribeEntry12 = new SubscribeEntry12();
        sExEventBus.register(sSubscribeEntry12);
        assertTrue(received);
        addEventList();
        // 确保进程之间已经建立连接
        Thread.sleep(1000 * 5);
    }

    private static void addEventList() {
        sEventTemp.add(new Holder(new Event8(UUID.randomUUID().toString()),"event8",void.class.getName()));

        sEventTemp.add(new Holder(new Event9(UUID.randomUUID().toString()),"event9_SubscribeEntry8",String.class.getName()));
        sEventTemp.add(new Holder(new Event10(UUID.randomUUID().toString()),"event10_SubscribeEntry8",String.class.getName()));
        sEventTemp.add(new Holder(new Event11(UUID.randomUUID().toString()),"event11_SubscribeEntry8",String.class.getName()));

        sEventTemp.add(new Holder(new Event9(),"event9_SubscribeEntry9",String.class.getName()));
        sEventTemp.add(new Holder(new Event10(),"event10_SubscribeEntry9",String.class.getName()));
        sEventTemp.add(new Holder(new Event11(),"event11_SubscribeEntry9",String.class.getName()));

        sEventTemp.add(new Holder(new Event9(),"event9_SubscribeEntry10",String.class.getName()));
        sEventTemp.add(new Holder(new Event10(),"event10_SubscribeEntry10",String.class.getName()));
        sEventTemp.add(new Holder(new Event11(),"event11_SubscribeEntry10",String.class.getName()));

        sEventTemp.add(new Holder(new Event9(),"event9_SubscribeEntry11",String.class.getName()));
        sEventTemp.add(new Holder(new Event10(),"event10_SubscribeEntry11",String.class.getName()));
        sEventTemp.add(new Holder(new Event11(),"event11_SubscribeEntry11",String.class.getName()));
    }

    @AfterClass
    public static void after() throws Exception {
        // 通知其他的测试进程退出
        sTest10.killSelf();
        sTest11.killSelf();
        sTest12.killSelf();
        sTest13.killSelf();

        // 确定已经接收到了所有其他进程的退出消息
        Router router = getRouterObj();
        boolean destroy = false;
        long endTime = SystemClock.uptimeMillis() + 1000 * 5;
        while (SystemClock.uptimeMillis() < endTime) {
            Thread.sleep(10);
            ConcurrentHashMap<String, CopyOnWriteArrayList<Event>> subScribeEventList =
                    router.getSubScribeEventList();
            CopyOnWriteArrayList<Event> address10Events = subScribeEventList.get(sAddress10);
            CopyOnWriteArrayList<Event> address11Events = subScribeEventList.get(sAddress11);
            CopyOnWriteArrayList<Event> address12Events = subScribeEventList.get(sAddress12);
            CopyOnWriteArrayList<Event> address13Events = subScribeEventList.get(sAddress13);
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
        return (Router) mRouterField.get(sExEventBus);
    }

    /**
     * 测试发布一个不需要返回值的消息到其他进程
     */
    @Test
    public void publishToOtherProcess() throws Exception {
        String body = UUID.randomUUID().toString();
        String uuid = UUID.randomUUID().toString();
        String msg = body + "#" + M_TAG + "#" + uuid;
        Event8 event8 = new Event8(msg);
        String tag = "event8";
        sExEventBus.remotePublish(event8,tag,void.class.getName(),1000 * 2);

        // 确认其他进程都收到了发送的消息
        String result10 = null;
        String result11 = null;
        String result12 = null;
        String result13 = null;
        long endTime = SystemClock.uptimeMillis() + sDefaultTimeout;

        while (SystemClock.uptimeMillis() < endTime) {
            Thread.sleep(50);
            result10 = sTest10.testMethod1Result();
            result11 = sTest11.testMethod1Result();
            result12 = sTest12.testMethod1Result();
            result13 = sTest13.testMethod1Result();
            if (!TextUtils.isEmpty(result10) &&
                    !TextUtils.isEmpty(result11) &&
                    !TextUtils.isEmpty(result12) &&
                    !TextUtils.isEmpty(result13)) {
                break;
            }
        }
        assertEquals(msg,result10);
        assertEquals(msg,result11);
        assertEquals(msg,result12);
        assertEquals(msg,result13);
    }

    /**
     * 测试发布一个需要返回值的消息到其他进程
     */
    @Test
    public void publishToOtherProcessAndWaitReturn() throws Exception {
        Event9 event9 = new Event9("event9_SubscribeEntry8");
        String tag = "event9_SubscribeEntry8";
        // 分别发送消息给其他4个进程
        String returnClassName = String.class.getName();
        Object returnValue = sExEventBus.remotePublish(event9, tag, returnClassName, sDefaultTimeout);
        assertNotNull(returnValue);
        assertEquals(returnValue.getClass(),String.class);
        assertEquals("return_event9_SubscribeEntry8",returnValue.toString());

        event9 = new Event9("event9_SubscribeEntry9");
        tag = "event9_SubscribeEntry9";
        returnClassName = String.class.getName();
        returnValue = sExEventBus.remotePublish(event9,tag,returnClassName, sDefaultTimeout);
        assertNotNull(returnValue);
        assertEquals(returnValue.getClass(),String.class);
        assertEquals("return_event9_SubscribeEntry9",returnValue.toString());

        event9 = new Event9("event9_SubscribeEntry10");
        tag = "event9_SubscribeEntry10";
        returnClassName = String.class.getName();
        returnValue = sExEventBus.remotePublish(event9,tag,returnClassName, sDefaultTimeout);
        assertNotNull(returnValue);
        assertEquals(returnValue.getClass(),String.class);
        assertEquals("return_event9_SubscribeEntry10",returnValue.toString());

        event9 = new Event9("event9_SubscribeEntry11");
        tag = "event9_SubscribeEntry11";
        returnClassName = String.class.getName();
        returnValue = sExEventBus.remotePublish(event9,tag,returnClassName, sDefaultTimeout);
        assertNotNull(returnValue);
        assertEquals(returnValue.getClass(),String.class);
        assertEquals("return_event9_SubscribeEntry11",returnValue.toString());
    }

    /**
     * 测试进程之间的粘滞事件
     */
    @Test
    public void stickyPublishEvent() throws Exception {

        String msg = "event8_sticky_return";
        Event8 event8 = new Event8(msg);
        String tag = "event8_sticky";
        // 先发送一个粘滞事件
        sExEventBus.stickyRemotePublish(event8,tag,2000);
        Thread.sleep(3000);
        // 启动TestService14
        Context context = InstrumentationRegistry.getTargetContext();
        Intent service14Intent = new Intent(context,TestService14.class);
        IBinder binder = sServiceRule.bindService(service14Intent);
        IRouterInteractInterface test14 = IRouterInteractInterface.Stub.asInterface(binder);
        // 启动进程以后就应该执行了注册的方法
        long endtime = SystemClock.uptimeMillis() + 1000 * 5;
        boolean found = false;
        while (SystemClock.uptimeMillis() < endtime) {
            String s = test14.testMethod1Result();
            if (!TextUtils.isEmpty(s)) {
                found = true;
                break;
            }
        }
        assertTrue(found);
        assertEquals(msg,test14.testMethod1Result());
        test14.stop();
    }

    /**
     * 测试进程彼此之间互相发送消息
     */
    @Test
    public void publishToEachOther() throws Exception {

        sExEventBus.register(this);

        int count = 1000;

        sTest10.start(count);
        sTest11.start(count);
        sTest12.start(count);
        sTest13.start(count);

        final Random random = new Random(SystemClock.uptimeMillis());

        for (int i = 0; i < count;i++) {
            Thread.sleep(200);
            int index = random.nextInt(13);
            if (index == 0) {
                Holder holder = sEventTemp.get(index);
                Holder newHolder = holder.deepCopy();
                String body = UUID.randomUUID().toString();
                String uuid = UUID.randomUUID().toString();
                String msg = body + "#" + M_TAG + "#" + uuid;
                newHolder.event.setMsg(msg);
                sExEventBus.remotePublish(newHolder.event,newHolder.tag,newHolder.returnClassName,1000 * 2);
                boolean received = false;
                long endTimeReturn = SystemClock.uptimeMillis() + 1000 * 2;
                String value10 = null;
                String value11 = null;
                String value12 = null;
                String value13 = null;

                try {
                    while (SystemClock.uptimeMillis() < endTimeReturn) {

                        value10 = sValueTemp.get(sTest10.getAddress()+uuid);
                        value11 = sValueTemp.get(sTest11.getAddress()+uuid);
                        value12 = sValueTemp.get(sTest12.getAddress()+uuid);
                        value13 = sValueTemp.get(sTest13.getAddress()+uuid);

                        if (!TextUtils.isEmpty(value10) &&
                                !TextUtils.isEmpty(value11) &&
                                !TextUtils.isEmpty(value12) &&
                                !TextUtils.isEmpty(value13)) {
                            received = true;
                            break;
                        }
                    }
                    Assert.assertTrue(received);
                    Assert.assertEquals(body,value10);
                    Assert.assertEquals(body,value11);
                    Assert.assertEquals(body,value12);
                    Assert.assertEquals(body,value13);
                    sValueTemp.remove(sTest10.getAddress());
                    sValueTemp.remove(sTest11.getAddress());
                    sValueTemp.remove(sTest12.getAddress());
                    sValueTemp.remove(sTest13.getAddress());
                }catch (RemoteException e) {
                    Log.e("main","",e);
                }
            } else {
                Holder holder = sEventTemp.get(index);
                String msg = UUID.randomUUID().toString();
                holder.event.setMsg(msg);
                Object o = sExEventBus.remotePublish(holder.event, holder.tag, holder.returnClassName, 1000 * 2);
                Assert.assertNotNull(o);
                Assert.assertEquals(o.getClass(),String.class);
                Assert.assertEquals("return_" + msg,o.toString());
            }
        }
        Log.d("main1","ExEventBusTset 执行结束，准备关闭其他进程!");
        sTest10.stop();
        sTest11.stop();
        sTest12.stop();
        sTest13.stop();
        Log.d("main1","ExEventBusTset 关闭其他进程结束!");
    }

    @Subscriber(tag = M_TAG,type = Type.DEFAULT,model = ThreadModel.POOL,remote = true)
    public void waitCallResult(Event8 event8) {

        String msg = event8.getMsg();
        String split[] = msg.split("#");
        String uuid = split[0];
        String addressAndUuid = split[1];
        sValueTemp.put(addressAndUuid,uuid);
    }

    private static class Holder {
        final BaseEvent event;
        final String tag;
        final String returnClassName;
        Holder(BaseEvent event,String tag,String returnClassName) {
            this.event =event;
            this.tag = tag;
            this.returnClassName = returnClassName;
        }

        Holder deepCopy() {
            return new Holder(event.deepCopy(),tag,returnClassName);
        }
    }
}
