package com.llx278.exeventbus.remote;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

/**
 * 对ITransferLayer的具体实现
 * 这里用广播来实现消息间的通信，因为只有广播能做到在动态注册，而其他的ipc机制，contentProvider
 * service的binder都需要静态注册才能够运行.
 * Created by llx on 2018/2/28.
 */

public class MockPhysicalLayer implements IMockPhysicalLayer {

    private Context mContext;
    private FilterReceiver mFilterReceiver;
    private ReceiverListener mListener;

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
        mContext = null;
    }

    private void register() {
        mFilterReceiver = new FilterReceiver();
        // 对外注册一个广播，这个广播只能由特定的action和category的组合才能够接受到消息，就相当于一台电脑的ip地址
        IntentFilter intentFilter = new IntentFilter();
        Address.Filter filter = Address.Filter.crateIntentFilter();
        intentFilter.addAction(filter.getAction());
        for (String category : filter.getCategories()) {
            intentFilter.addCategory(category);
        }
        mContext.registerReceiver(mFilterReceiver,intentFilter);
    }

    @Override
    public void send(String address, Bundle message) {
        Intent intent = new Intent();
        Address.Filter filter = Address.Filter.createIntentBy(Address.parse(address));
        intent.setAction(filter.getAction());
        for (String category : filter.getCategories()) {
            intent.addCategory(category);
        }
        intent.putExtra(Constant.KEY_WHERE, Address.createOwnAddress().toString());
        intent.putExtra(Constant.KEY_MESSAGE,message);
        mContext.sendBroadcast(intent);
    }

    public void receive(final String where, final Bundle message) {
        if (mListener != null) {
            mListener.onMessageReceive(where,message);
        }
    }

    @Override
    public void setOnReceiveListener(ReceiverListener listener) {
        mListener = listener;
    }



    private class FilterReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Address.Filter.isFilterAction(action)) {
                String where = intent.getStringExtra(Constant.KEY_WHERE);
                if (Address.createOwnAddress().equals(Address.parse(where))) {
                    return;
                }
                Bundle message = intent.getParcelableExtra(Constant.KEY_MESSAGE);
                MockPhysicalLayer.this.receive(where,message);
            }
        }
    }
}
