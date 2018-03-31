package com.llx278.exeventbus.remote;

import android.os.Bundle;
import android.os.Process;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;

import com.llx278.exeventbus.ELogger;
import com.llx278.exeventbus.exception.TimeoutException;

import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * 对ITransportLayer的具体实现,这里不考虑太多复杂的情况，仅仅就是确认一下消息是否已经送达就可以了。
 * Created by llx on 2018/2/28.
 */

public class TransportLayer implements ITransportLayer {

    /**
     * 一条消息所拥有的唯一id
     */
    private static final String KEY_ID = "#*kdfakfjya38783fjlajfdka78er7qw_)98rjflajflkdajfdklaj#";
    /**
     * 消息确认标志
     */
    private static final String KEY_BODY = "JDIJF8958#*#&*Jfkdsjafkldajsfkld+-ajf*djfkdjfkdjfkdj#";
    private static final String BODY = "#ack#";
    /**
     * 标志此条消息是广播消息
     */
    private static final String KEY_BROADCAST = "kdfjdkj#$kdfjskhgdhjhgddksjfdsk**(fhdjhfjd";
    private static final String BROADCAST_BODY = "broadcast";

    private final IMockPhysicalLayer mMockPhysicalLayer;
    private final ConcurrentHashMap<String, CountDownLatch> mSignalMap = new ConcurrentHashMap<>();

    private Receiver mListener;

    public TransportLayer(IMockPhysicalLayer transferLayer) {
        mMockPhysicalLayer = transferLayer;
        mMockPhysicalLayer.setOnReceiveListener(new TransferLayerReceiver());
    }

    @Override
    public void init() {
    }

    @Override
    public void destroy() {
        mMockPhysicalLayer.destroy();
    }

    @Override
    public void send(final String address, final Bundle message, final long timeout)
            throws TimeoutException {

        String id = address + "#" + UUID.randomUUID();
        message.putString(KEY_ID, id);
        CountDownLatch signal = new CountDownLatch(1);
        mSignalMap.put(id, signal);
        mMockPhysicalLayer.send(address, message);
        try {
            if (!signal.await(timeout, TimeUnit.MILLISECONDS)) {
                mSignalMap.remove(id);
                throw new TimeoutException("send message to " + address + " failed!");
            }
            // 发送成功，直接返回，
        } catch (InterruptedException ignore) {
        }
    }

    @Override
    public void sendBroadcast(Bundle message) {
        // 直接发送，不需要考虑消息是否发送成功
        String address = Address.createOwnAddress().toString();
        String id = address + "#" + UUID.randomUUID().toString();
        message.putString(KEY_ID,id);
        message.putString(KEY_BROADCAST,BROADCAST_BODY);
        mMockPhysicalLayer.send(null, message);
    }

    @Override
    public ArrayList<String> getAvailableAddress() {
        return mMockPhysicalLayer.getAvailableAddress();
    }

    /**
     * 处理消息
     *
     * @param where   发送消息的地址
     * @param message 消息体
     */
    private void receive(String where, Bundle message) {
        // 如果接收到的消息是一条确认消息的话
        // 那么就直接返回
        if (isAck(where, message)) {
            return;
        }

        // 接收到了一条消息，发送确认消息，并剥离KEY_ID,
        // 防止干扰上层的业务
        String id = message.getString(KEY_ID);
        if (!TextUtils.isEmpty(id)) {

            String broadcast = message.getString(KEY_BROADCAST);
            if (!BROADCAST_BODY.equals(broadcast)) {
                sendAck(where, id);
                message.putString(KEY_ID,null);
                message.putString(KEY_BROADCAST,null);
            }

            if (mListener != null) {
                mListener.onMessageReceive(where, message);
            }
        }
    }

    private void sendAck(String where, String id) {
        Bundle message = new Bundle();
        message.putString(KEY_ID, id);
        message.putString(KEY_BODY, BODY);
        mMockPhysicalLayer.send(where, message);
    }

    private boolean isAck(String where, Bundle message) {

        String body = message.getString(KEY_BODY);
        if (BODY.equals(body)) {
            // 是一条ack的确认消息
            String id = message.getString(KEY_ID);
            if (!TextUtils.isEmpty(id) && id.startsWith(where)) {
                CountDownLatch countDownLatch = mSignalMap.get(id);
                if (countDownLatch != null) {
                    countDownLatch.countDown();
                    mSignalMap.remove(id);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void setOnReceiveListener(Receiver listener) {
        mListener = listener;
    }

    /**
     * 实现了ITransferLayer的消息通知接口
     */
    private class TransferLayerReceiver implements Receiver {

        @Override
        public void onMessageReceive(String where, Bundle message) {
            TransportLayer.this.receive(where, message);
        }
    }
}
