package com.llx278.eventbusdemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.llx278.exeventbus.EventBusImpl;
import com.llx278.exeventbus.Subscriber;
import com.llx278.exeventbus.ThreadModel;

public class MainActivity extends AppCompatActivity {

    public static final String MAIN_TAG = "updateTextView";

    TextView textView;
    TextView event1Text;
    TextView event2Text;
    TextView event3Text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button bt = findViewById(R.id.bt);
        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        textView = findViewById(R.id.text);
        event1Text = findViewById(R.id.event1);
        event2Text = findViewById(R.id.event2);
        event3Text = findViewById(R.id.event3);

    }

    @Override
    protected void onResume() {
        super.onResume();
        EventBusImpl.getDefault().register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        EventBusImpl.getDefault().unRegister(this);

    }

    @Subscriber(tag = MAIN_TAG, model = ThreadModel.POST)
    public void updateTextView(String msg) {
        try {
            textView.setText(msg);
        } catch (Exception e) {
            Log.e("main",Log.getStackTraceString(e));
        }
    }
}
