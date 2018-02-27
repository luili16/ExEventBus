package com.llx278.exeventbus.remote;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

/**
 * Created by llx on 2018/2/27.
 */

public class Sender implements ISender {

    private Context mContext;

    public Sender(Context context) {
        mContext = context;
    }

    /**
     *
     * @param address 消息地址
     * @param message 消息体
     */
    @Override
    public void send(String address, Bundle message) {
        Intent intent = new Intent();
        Address.Filter filter = Address.Filter.createIntentBy(Address.parse(address));
        intent.setAction(filter.getAction());
        for (String category : filter.getCategories()) {
            intent.addCategory(category);
        }
        mContext.sendBroadcast(intent);
    }
}
