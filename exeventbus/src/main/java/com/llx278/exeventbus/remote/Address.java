package com.llx278.exeventbus.remote;

import android.os.Process;
import android.text.TextUtils;

import com.llx278.exeventbus.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * 封装了地址管理相关的方法
 * 目前考虑用一个进程的唯一pid来作为这个进程的地址
 * Created by llx on 2018/2/27.
 */

public class Address {

    private static final String PID = "pid";

    private int mPid;

    private Address(int pid) {
        mPid = pid;
    }

    @Override
    public String toString() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(PID,mPid);
        } catch (JSONException e) {
            Logger.e("",e);
        }
        return jsonObject.toString();
    }

    public static Address createOwnAddress() {
        return new Address(Process.myPid());
    }

    public static Address createAddress(int pid) {
        return new Address(pid);
    }

    public static Address parse(String address) {

        if (TextUtils.isEmpty(address)) {
            return null;
        }

        Address addressObj = null;
        try {
            JSONObject jsonObject = new JSONObject(address);
            int pid = jsonObject.getInt(PID);
            addressObj = new Address(pid);
        } catch (JSONException e) {
            Logger.e("",e);
        }
        return addressObj;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Address address = (Address) o;

        return mPid == address.mPid;
    }

    @Override
    public int hashCode() {
        return mPid;
    }

    /**
     * 封装了对外注册相关的field
     */
    public static class Filter {

        private static final String ACTION_RECEIVE = "com.llx278.exeventbus.remote.ReceiverImpl.receive";

        private final String mAction;
        private Set<String> mCategories;

        private Filter(String action,String ownCategory,String broadcastCategory) {
            mAction = action;
            mCategories = new HashSet<>();
            if (!TextUtils.isEmpty(ownCategory)) {
                mCategories.add(ownCategory);
            }

            if (!TextUtils.isEmpty(broadcastCategory)) {
                mCategories.add(broadcastCategory);
            }
        }

        public String getAction() {
            return mAction;
        }

        public Set<String> getCategories() {
            return Collections.unmodifiableSet(mCategories);
        }

        @Override
        public String toString() {
            return "Filter{" +
                    "mAction='" + mAction + '\'' +
                    ", mCategories=" + mCategories +
                    '}';
        }

        public static Filter createIntentBy(Address address) {

            String category = null;
            String broadcastCategory = null;
            if (address != null) {
                category = createCategory(address);
            } else {
                broadcastCategory = createBroadcastCategory();
            }
            return new Filter(ACTION_RECEIVE,category,broadcastCategory);
        }

        public static Filter crateIntentFilter() {
            String ownCategory = createCategory(Address.createOwnAddress());
            String broadcastCategory = createBroadcastCategory();
            return new Filter(ACTION_RECEIVE,ownCategory,broadcastCategory);
        }

        private static String createCategory(Address address) {
            return "com.llx278.exeventbus.remote:" + address.toString();
        }

        private static String createBroadcastCategory() {
            return "com.com.llx278.exeventbus.remote:broadcast";
        }

        public static boolean isFilterAction(String action) {
            return TextUtils.equals(action,ACTION_RECEIVE);
        }
    }

}
