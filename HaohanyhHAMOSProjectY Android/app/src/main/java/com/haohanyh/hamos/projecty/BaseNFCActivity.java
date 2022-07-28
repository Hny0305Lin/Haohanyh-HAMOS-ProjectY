/* 受Haohanyh Computer Software Products Open Source LICENSE保护 https://git.haohanyh.top:3001/Haohanyh/LICENSE */
package com.haohanyh.hamos.projecty;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.haohanyh.hamos.nfc.NFCHelper;

public class BaseNFCActivity extends Activity {
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
        if (nfcHelper.SupportNFC()) {
            //判断设备是否开启NFC功能
            if (nfcHelper.EnableNFC()) {
                //注册NFC监听器
                nfcHelper.RegisterNFC(this);
            } else {
                nfcHelper.GoToNFCSetting(this);
            }
        } else {
            Toast.makeText(BaseNFCActivity.this,"当前设备不支持NFC功能",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        nfcHelper.UnRegisterNFC(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

}
