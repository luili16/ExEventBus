package com.llx278.exeventbus.remote;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;

import com.llx278.exeventbus.Logger;
import com.llx278.exeventbus.remote.test.Util;

import java.util.Set;

/**
 * 对ITransferLayer的具体实现
 * 这里用广播来实现消息间的通信，因为只有广播能做到在动态注册，而其他的ipc机制，contentProvider
 * service的binder都需要静态注册才能够运行.
 * Created by llx on 2018/2/28.
 */

public class TransferLayer implements ITransferLayer {

    private Context mContext;
    private FilterReceiver mFilterReceiver;
    private ReceiverListener mListener;

    public TransferLayer(Context context) {
        mContext = context;
        register();
    }

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
        Log.d("main",Util.getProcessName(mContext) + "准备发送-" + "发送地址:" + Address.createOwnAddress() + "-接收地址:"+address);
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

    @Override
    public void receive(final String where, final Bundle message) {
        Log.d("main",Util.getProcessName(mContext) + "接收到消息-"+"发送地址:" + where);
        if (mListener != null) {
            mListener.onMessageReceive(where,message);
        }
    }

    public void setOnReceiveListener(ReceiverListener listener) {
        mListener = listener;
    }

    /**
     * 对外暴露一个接口，任何对接收到的消息感兴趣的类都可以通过MessageObserver来向TransferLayer注册，处理
     * 接收到的消息
     */
    public interface ReceiverListener {
        void onMessageReceive(String where,Bundle message);
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
                TransferLayer.this.receive(where,message);
            }
        }
    }
}
