package com.llx278.exeventbus.remote;

import android.os.Bundle;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;

import com.llx278.exeventbus.exception.TimeoutException;

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
    private static final String KEY_BODY = "JDIJF8958#*#&*Jfkdsjafkldajsfkld+-ajf*djfkdjfkdjfkdj#";
    private static final String BODY = "#ack#";

    private final IMockPhysicalLayer mTransferLayer;
    private final ConcurrentHashMap<String,CountDownLatch> mSignalMap = new ConcurrentHashMap<>();


    private ReceiverListener mListener;

    public TransportLayer(IMockPhysicalLayer transferLayer) {
        mTransferLayer = transferLayer;
        mTransferLayer.setOnReceiveListener(new TransferLayerReceiver());
    }

    @Override
    public void init() {
    }

    @Override
    public void destroy() {
        mTransferLayer.destroy();
    }

    @Override
    public void send(String address, Bundle message, long timeout) {
        String id = address + "#" + UUID.randomUUID();
        Log.d("main","send id : " + id);
        // 对每一个待发送的消息添加一个id，这个id唯一的标识了一条消息
        // 最好的形式是将这个id作为message的消息头，但是这里用的bundle，无法做字符串的拼接
        // 因此，这里存在的隐患就是如果message中存在着某个字段与KEY_ID相同的话会导致覆盖掉消息体的内容
        // 目前只能将KEY_ID这个字符串尽量设计的复杂一些，但实际上key都应该是有意义的字符串，如果真的一致了
        // 那...该买彩票了!
        message.putString(KEY_ID, id);
        mTransferLayer.send(address,message);
        CountDownLatch signal = new CountDownLatch(1);
        mSignalMap.put(id,signal);
        try {
            if (!signal.await(timeout, TimeUnit.MILLISECONDS)) {
                throw new TimeoutException("send message to " + address + " failed!");
            }
        } catch (InterruptedException ignore) {
        }
    }

    @Override
    public void sendBroadcast(Bundle message) {
        // 直接发送，不需要考虑消息是否发送成功
        mTransferLayer.send(null,message);
    }

    /**
     *
     *处理消息
     * @param where 发送消息的地址
     * @param message 消息体
     */
    private void receive(String where, Bundle message) {

        // 如果接收到的消息是一条确认消息的话
        // 那么就直接返回
        if (isAck(where,message)) {
            return;
        }

        // 接收到了一条消息，发送确认消息，并剥离KEY_ID,
        // 防止干扰上层的业务
        String id = message.getString(KEY_ID);
        if (!TextUtils.isEmpty(id)) {
            sendAck(where,id);
            message.putString(KEY_ID,null);
        }

        if (mListener != null) {
            mListener.onMessageReceive(where,message);
        }
    }

    private void sendAck(String where,String id) {
        Bundle message = new Bundle();
        message.putString(KEY_ID,id);
        message.putString(KEY_BODY,BODY);
        mTransferLayer.send(where,message);
    }

    private boolean isAck(String where,Bundle message) {

        String body = message.getString(KEY_BODY);
        if (BODY.equals(body)) {
            // 是一条ack的确认消息
            String id = message.getString(KEY_ID);
            Log.d("main","receive id : " + id);
            if (!TextUtils.isEmpty(id) && id.startsWith(where)) {
                CountDownLatch countDownLatch = mSignalMap.get(id);
                if (countDownLatch != null) {
                    countDownLatch.countDown();
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void setOnReceiveListener(ITransportLayer.ReceiverListener listener) {
        mListener = listener;
    }

    /**
     * 实现了ITransferLayer的消息通知接口
     */
    private class TransferLayerReceiver implements IMockPhysicalLayer.ReceiverListener {

        @Override
        public void onMessageReceive(String where, Bundle message) {
            TransportLayer.this.receive(where,message);
        }
    }
}
