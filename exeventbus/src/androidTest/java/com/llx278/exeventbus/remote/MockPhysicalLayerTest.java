package com.llx278.exeventbus.remote;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ServiceTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

//import com.llx278.exeventbus.IMyTestInterface;
import com.llx278.exeventbus.IMyTestInterface;
import com.llx278.exeventbus.remote.test.*;
import com.llx278.exeventbus.remote.test.Constant;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by llx on 2018/2/28.
 */
@RunWith(AndroidJUnit4.class)
public class MockPhysicalLayerTest implements MockPhysicalLayer.ReceiverListener {


    private MockPhysicalLayer mMockPhysicalLayer;
    private IMyTestInterface mTest1;
    private IMyTestInterface mTest2;
    private IMyTestInterface mTest3;
    private IMyTestInterface mTest4;

    private boolean hasReceive;


    @Before
    public void setUp() throws Exception {
        mMockPhysicalLayer = new MockPhysicalLayer(InstrumentationRegistry.getTargetContext());
        mMockPhysicalLayer.setOnReceiveListener(this);
        Context context = InstrumentationRegistry.getTargetContext();
        Intent intent1 = new Intent(context, TestService1.class);
        IBinder binder1 = mServiceRule.bindService(intent1);
        mTest1 = IMyTestInterface.Stub.asInterface(binder1);
        Intent intent2 = new Intent(context, TestService2.class);
        IBinder binder2 = mServiceRule.bindService(intent2);
        mTest2 = IMyTestInterface.Stub.asInterface(binder2);
        Intent intent3 = new Intent(context, TestService3.class);
        IBinder binder3 = mServiceRule.bindService(intent3);
        mTest3 = IMyTestInterface.Stub.asInterface(binder3);
        Intent intent4 = new Intent(context, TestService4.class);
        IBinder binder4 = mServiceRule.bindService(intent4);
        mTest4 = IMyTestInterface.Stub.asInterface(binder4);
    }

    @After
    public void tearDown() {
        mMockPhysicalLayer.destroy();
    }

    @Rule
    public final ServiceTestRule mServiceRule = new ServiceTestRule();


    @Test
    public void sendMessage() throws Exception {

        clearAll();
        Bundle message = new Bundle();
        String receiveStr = "receive";
        message.putString(Constant.KEY_RECEIVE, receiveStr);
        Address address1 = createTest1Address();
        mMockPhysicalLayer.send(address1.toString(), message);

        Address address2 = createTest2Address();
        mMockPhysicalLayer.send(address2.toString(), message);

        Address address3 = createTest3Address();
        mMockPhysicalLayer.send(address3.toString(), message);

        Address address4 = createTest4Address();
        mMockPhysicalLayer.send(address4.toString(), message);

        String expectedStr1 = receiveStr + ":" + address1.toString();
        String expectedStr2 = receiveStr + ":" + address2.toString();
        String expectedStr3 = receiveStr + ":" + address3.toString();
        String expectedStr4 = receiveStr + ":" + address4.toString();
        long timeout = 2000;
        long endTime = SystemClock.uptimeMillis() + timeout;
        boolean isReceive = false;
        while (SystemClock.uptimeMillis() < endTime) {
            String receiveStr1 = mTest1.getReceiveStr();
            String receiveStr2 = mTest2.getReceiveStr();
            String receiveStr3 = mTest3.getReceiveStr();
            String receiveStr4 = mTest4.getReceiveStr();
            if (expectedStr1.equals(receiveStr1) &&
                    expectedStr2.equals(receiveStr2) &&
                    expectedStr3.equals(receiveStr3) &&
                    expectedStr4.equals(receiveStr4)) {
                isReceive = true;
                break;
            }
            Thread.sleep(100);
        }
        Assert.assertTrue(isReceive);
    }

    private Address createTest1Address() throws Exception {
        String processName1 = mTest1.getProcessName();
        // 模拟向TestService1所在的进程发送消息
        int pid1 = Integer.parseInt(processName1.split("-")[1]);
        return Address.createAddress(pid1);
    }

    private Address createTest2Address() throws Exception {
        String processName2 = mTest2.getProcessName();
        int pid2 = Integer.parseInt(processName2.split("-")[1]);
        return Address.createAddress(pid2);
    }

    private Address createTest3Address() throws Exception {
        String processName3 = mTest3.getProcessName();
        int pid3 = Integer.parseInt(processName3.split("-")[1]);
        return Address.createAddress(pid3);
    }

    private Address createTest4Address() throws Exception {
        String processName4 = mTest4.getProcessName();
        int pid4 = Integer.parseInt(processName4.split("-")[1]);
        return Address.createAddress(pid4);
    }


    @Test
    public void messageBroadcast() throws Exception {

        clearAll();
        // 模拟向其他进程发送广播消息
        Bundle message = new Bundle();
        message.putString(Constant.KEY_BROADCAST, Constant.BROADCAST_MESSAGE);
        mMockPhysicalLayer.send(null, message);
        long timeout = 2000;
        boolean receive = false;
        long endTime = SystemClock.uptimeMillis() + timeout;
        while (SystemClock.uptimeMillis() < endTime) {
            String broadcastStr1 = mTest1.getBroadcastStr();
            String broadcastStr2 = mTest2.getBroadcastStr();
            String broadcastStr3 = mTest3.getBroadcastStr();
            String broadcastStr4 = mTest4.getBroadcastStr();
            if (Constant.BROADCAST_MESSAGE.equals(broadcastStr1) &&
                    Constant.BROADCAST_MESSAGE.equals(broadcastStr2) &&
                    Constant.BROADCAST_MESSAGE.equals(broadcastStr3) &&
                    Constant.BROADCAST_MESSAGE.equals(broadcastStr4)) {
                receive = true;
                break;
            }
            Thread.sleep(100);
        }
        Assert.assertTrue(receive);
    }

    @Test
    public void messageReceive() throws Exception {
        clearAll();
        hasReceive = false;
        CountDownLatch mReceiveSignal = new CountDownLatch(1);
        mTest1.mockSendMessage(Address.createOwnAddress().toString(), null);
        mReceiveSignal.await(2,TimeUnit.SECONDS);
        Assert.assertTrue(hasReceive);
    }

    private void clearAll() throws Exception {
        mTest1.clear();
        mTest2.clear();
        mTest3.clear();
        mTest4.clear();
    }

    @Override
    public void onMessageReceive(String where, Bundle message) {
        // 接收返回的消息

        try {
            Log.d("main","where : " + where);
            Assert.assertEquals(createTest1Address().toString(),where);
            String address = message.getString(Constant.KEY_MY_OWN_ADDRESS);
            Log.d("main","address : " + address);
            Assert.assertEquals(createTest1Address().toString(),address);
            hasReceive = true;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
