package com.llx278.exeventbus.remote;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Parcelable;

import com.llx278.exeventbus.AbsEventBus;

/**
 *
 * Created by llx on 2018/2/27.
 */

public class RemoteEventBus extends AbsEventBus {

    private Context mContext;
    private Executor mExecutor;
    private Callbacker mCallbacker;


    public RemoteEventBus(Context context) {
        super();
        mContext = context;
        registerReceiver();
    }

    private void registerReceiver() {
        mExecutor = new Executor();
        IntentFilter excutorFilter = new IntentFilter(Constant.ACTION_EVENT_EXECUTE);
        mContext.registerReceiver(mExecutor,excutorFilter);
        mCallbacker = new Callbacker();
        IntentFilter callbackerFilter = new IntentFilter(Constant.ACTION_EVENT_CALLBACK);
        mContext.registerReceiver(mCallbacker,callbackerFilter);
    }

    @Override
    public void cleanUp() {
        super.cleanUp();
        unregisterReceiver();
    }

    private void unregisterReceiver() {
        if (mExecutor != null) {
            mContext.unregisterReceiver(mExecutor);
            mExecutor = null;
        }

        if (mCallbacker != null) {
            mContext.unregisterReceiver(mCallbacker);
            mCallbacker = null;
        }
    }

    @Override
    public Object post(Object eventObj, String tag, String returnClassName) {

        Intent intent = new Intent();
        intent.setAction(Constant.ACTION_EVENT_CALLBACK);
        Bundle bundle = new Bundle();
        try {
            bundle.putParcelable(Constant.KEY_EVENT_OBJ, (Parcelable) eventObj);
        } catch (ClassCastException e) {
            throw new RuntimeException("eventObj must be implemented android.os.Parcelable",e);
        }
        bundle.putString(Constant.KEY_TAG,tag);
        bundle.putString(Constant.KEY_RETURN_CLASS_NAME,returnClassName);
        mContext.sendBroadcast(intent);
        // 等待返回的结果

        return null;
    }

    private class Executor extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

        }
    }

    private class Callbacker extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

        }
    }
}
