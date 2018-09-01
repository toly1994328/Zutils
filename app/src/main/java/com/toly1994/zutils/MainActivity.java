package com.toly1994.zutils;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import top.toly.zutils.core.shortUtils.ToastUtil;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ToastUtil.show(this, "toly");
    }
}
