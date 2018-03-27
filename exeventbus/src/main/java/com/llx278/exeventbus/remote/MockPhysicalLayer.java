package com.llx278.exeventbus.remote;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;

/**
 * 对ITransferLayer的具体实现
 * 这里用广播来实现消息间的通信，因为只有广播能做到在动态注册.
 * Created by llx on 2018/2/28.
 */

public class MockPhysicalLayer implements IMockPhysicalLayer {

    private static final String KEY_WHERE = "com.llx278.exeventbus.remote.ReceiverImpl.key_where";
    private static final String KEY_MESSAGE = "com.llx278.exeventbus.remote.ReceiverImpl.key_message";
    private Context mContext;
    private FilterReceiver mFilterReceiver;
    private Receiver mListener;
    private HandlerThread mHandlerThread;

    public MockPhysicalLayer(Context context) {
        mContext = context;
        register();
    }

    @Override
    public void init() {
    }

    @Override
    public void destroy() {
        mContext.unregisterReceiver(mFilterReceiver);
        mHandlerThread.quitSafely();
        mContext = null;
    }

    private void register() {
        mHandlerThread = new HandlerThread("ExEventBus-MockPhysicalLayer-BroadcastThread");
        mHandlerThread.start();
        Handler mHandler = new Handler(mHandlerThread.getLooper());

        mFilterReceiver = new FilterReceiver();
        // 对外注册一个广播，这个广播只能由特定的action和category的组合才能够接受到消息，就相当于一台电脑的ip地址
        IntentFilter intentFilter = new IntentFilter();
        Address.Filter filter = Address.Filter.crateIntentFilter();
        intentFilter.addAction(filter.getAction());
        for (String category : filter.getCategories()) {
            intentFilter.addCategory(category);
        }
        mContext.registerReceiver(mFilterReceiver, intentFilter,null, mHandler);
    }

    @Override
    public void send(String address, Bundle message) {
        Intent intent = new Intent();
        Address.Filter filter = Address.Filter.createIntentBy(Address.parse(address));
        intent.setAction(filter.getAction());
        for (String category : filter.getCategories()) {
            intent.addCategory(category);
        }
        intent.putExtra(KEY_WHERE, Address.createOwnAddress().toString());
        intent.putExtra(KEY_MESSAGE, message);
        mContext.sendBroadcast(intent);
    }

    private void receive(final String where, final Bundle message) {
        if (mListener != null) {
            mListener.onMessageReceive(where, message);
        }
    }

    @Override
    public void setOnReceiveListener(Receiver listener) {
        mListener = listener;
    }

    private class FilterReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Address.Filter.isFilterAction(action)) {
                String where = intent.getStringExtra(KEY_WHERE);
                if (Address.createOwnAddress().equals(Address.parse(where))) {
                    return;
                }
                Bundle message = intent.getParcelableExtra(KEY_MESSAGE);
                MockPhysicalLayer.this.receive(where, message);
            }
        }
    }
}
