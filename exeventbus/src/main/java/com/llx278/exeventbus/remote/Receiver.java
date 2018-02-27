package com.llx278.exeventbus.remote;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by llx on 2018/2/27.
 */

public class Receiver implements IReceiver {

    private static final String KEY_WHERE = "com.llx278.exeventbus.remote.Receiver.key_where";
    private static final String KEY_MESSAGE = "com.llx278.exeventbus.remote.Receiver.key_message";

    private Context mContext;
    private FilterReceiver mFilterReceiver;
    private ExecutorService mExecutor;
    private ReceiveListener mListener;

    public Receiver(Context context) {
        mContext = context;
        mFilterReceiver = new FilterReceiver();
        mExecutor = Executors.newCachedThreadPool();
    }

    @Override
    public void init() {

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
    public void receive(final String where, final Bundle message) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                if (mListener != null) {
                    mListener.onReceive(where,message);
                }
            }
        });
    }

    public void setReceiveListener(ReceiveListener listener) {
        mListener = listener;
    }

    @Override
    public void destroy() {
        mContext.unregisterReceiver(mFilterReceiver);
    }

    private class FilterReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Address.Filter.isFilterAction(action)) {
                String where = intent.getStringExtra(KEY_WHERE);
                Bundle message = intent.getParcelableExtra(KEY_MESSAGE);
                Receiver.this.receive(where,message);
            }
        }
    }

    public interface ReceiveListener {
        void onReceive(String where,Bundle message);
    }
}
