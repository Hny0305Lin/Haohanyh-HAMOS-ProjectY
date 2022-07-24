package com.haohanyh.hamos.projecty;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import utils.NFCHelper;

public class BaseNFCActivity extends AppCompatActivity {
    protected NFCHelper nfcHelper;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        nfcHelper = new NFCHelper(this);
    }


    @Override
    protected void onResume() {
        super.onResume();
        //判断设备是否支持NFC功能
        if (nfcHelper.isSupportNFC()) {
            //判断设备是否开启NFC功能
            if (nfcHelper.isEnableNFC()) {
                //注册FNC监听器
                nfcHelper.registerNFC(this);
            } else {
                nfcHelper.showFNCSetting(this);
            }
        } else {
            showToast("当前设备不支持NFC功能");
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        nfcHelper.unRegisterNFC(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        log("Action: " + intent.getAction());

    }

    public void start(Class clazz) {
        startActivity(new Intent(this, clazz));
    }

    public void showToast(String content) {
        if (TextUtils.isEmpty(content))
            return;
        Toast.makeText(this, content, Toast.LENGTH_SHORT).show();

    }

    public void log(String content) {
        Log.e(getClass().getSimpleName(), content);
    }
}
