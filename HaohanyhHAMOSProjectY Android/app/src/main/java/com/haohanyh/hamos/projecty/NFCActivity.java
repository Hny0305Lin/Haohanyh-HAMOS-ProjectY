package com.haohanyh.hamos.projecty;

import static com.haohanyh.hamos.projecty.MainActivity.getToBearPiJSON;
import static com.haohanyh.hamos.projecty.R.id;
import static com.haohanyh.hamos.projecty.R.layout;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.Nullable;

import java.nio.charset.Charset;

public class NFCActivity extends BaseNFCActivity {
    public EditText NfcData;
    private boolean isWrite = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layout.activity_nfc);
        //初始化
        NfcData = findViewById(id.et_data);
        NfcData.post(new Runnable() {@Override public void run() { NfcData.setText(getToBearPiJSON());NfcData.setText(SSRJSON()); }});
    }

    private String SSRJSON() {
        Log.v("浩瀚银河:","目前JSON文本情况:"+NfcData.getText().toString()+","+"目前JSON在GB2312情况下字节数:"+NfcData.getText().toString().getBytes(Charset.forName("gb2312")).length);
        //正确（4的倍数），就按兵不动；反之（非4倍数），就添加空格。
        if (NfcData.getText().toString().getBytes(Charset.forName("gb2312")).length % 4 != 0) {
            Log.e("浩瀚银河:","您写入的数据字节数不为4的倍数，请稍等");
            int i = NfcData.getText().toString().getBytes(Charset.forName("gb2312")).length % 4;
            Log.e("浩瀚银河:","即将为您补充"+i+"个空格在json数据里");
            String json = NfcData.getText().toString().substring(0, NfcData.getText().toString().length() - 1);
            for(int j = 0; j == i; j++){
                Log.e("浩瀚银河:","已经添加了"+j+"个空格");
                json = json + " ";
            }
            Log.e("浩瀚银河:","现在您可以点击写入按钮了");
            return json + "}";
        }
        Log.e("浩瀚银河:","现在您可以点击写入按钮了");
        return NfcData.getText().toString();
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.bt_write_text:
                isWrite = true;
                break;
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (isWrite) {
            String text = NfcData.getText().toString();
            boolean ret = nfcHelper.writeNFC_NDEF(intent, text);
            nfcHelper.vibrate(this);
            Log.e("浩瀚银河:",ret ? "数据写入成功" : "数据写入失败");
            if(ret){ Intent i = new Intent(NFCActivity.this,HAMOSActivity.class);startActivity(i); }
            isWrite = false;
        }
    }
}
