package com.llx278.eventbusdemo;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.llx278.exeventbus.ExEventBus;
import com.llx278.exeventbus.Subscriber;
import com.llx278.exeventbus.execute.ThreadModel;

/**
 *
 * Created by liu on 18-3-31.
 */

public class Fragment2 extends Fragment {

    TextView mTabView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ExEventBus.getDefault().register(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_2,container);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        mTabView = view.findViewById(R.id.textview1);
    }

    @Subscriber(tag = "subscribeTestMethod")
    public void subscribeTestMethod1(String tabName) {
        if (mTabView != null) {
            mTabView.setText(tabName);
        }
    }
}
