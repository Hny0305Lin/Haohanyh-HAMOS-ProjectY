/* 受Haohanyh Computer Software Products Open Source LICENSE保护 https://git.haohanyh.top:3001/Haohanyh/LICENSE */
package com.haohanyh.hamos.managedata;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class ReadHuaweiAndWlanData {
    private static final String TAG = "浩瀚银河读取Huawei和WLAN，共两份文件:";
    private String data1, data2;
    private String Dir = null;
    public void setDir(String dir) { Dir = dir; }

    public ReadHuaweiAndWlanData() {};
    public static ReadHuaweiAndWlanData GetReadData() { return ReadHuaweiAndWlanData.readdata.thing; }
    protected static class readdata { private static final ReadHuaweiAndWlanData thing = new ReadHuaweiAndWlanData(); }

    /**
     * ReadFile()，保存的文件我们需要取出来做事情啦~
     */
    public boolean ReadFile() {
        Log.v(TAG,"开始");
        try {
            File file = new File(Dir + "/HAMOSData/User.json");
            FileInputStream inStream = new FileInputStream(file);
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int length = -1;
            while((length = inStream.read(buffer)) != -1 ){
                outStream.write(buffer, 0, length);
            }
            outStream.close();
            data1 = outStream.toString();
            //Log.v(TAG,"第一份:" + outStream.toString());
            inStream.close();
            Log.v(TAG,"第一份结束");

            File file2 = new File(Dir + "/HAMOSData/Wlan.json");
            FileInputStream inStream2 = new FileInputStream(file2);
            ByteArrayOutputStream outStream2 = new ByteArrayOutputStream();
            byte[] buffer2 = new byte[1024];
            int length2 = -1;
            while((length2 = inStream2.read(buffer2)) != -1 ){
                outStream2.write(buffer2, 0, length2);
            }
            outStream2.close();
            data2 = outStream2.toString();
            //Log.v(TAG,"第二份:" + outStream2.toString());
            inStream2.close();
            Log.v(TAG,"第二份结束");
            return true;
        } catch (IOException e) {
            Log.v(TAG,"失败,说明您没有保存,您需要正常启动成功后,手动保存好信息!");
        }
        return false;
    }

    /**
     * getData1()，华为值先提前处理再使用
     */
    public String[] getData1() {
        String[] data = new String[3];
        try {
            JSONObject jsonObjI = new JSONObject(data1);
            data[0] = jsonObjI.getString("Domain");
            data[1] = jsonObjI.getString("Name");
            data[2] = jsonObjI.getString("Pwd");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return data;
    }

    /**
     * getData2()，WLAN值先提前处理再使用
     */
    public String[] getData2() {
        String[] data = new String[2];
        try {
            JSONObject jsonObjI = new JSONObject(data2);
            data[0] = jsonObjI.getString("SSID");
            data[1] = jsonObjI.getString("Secret");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return data;
    }
}
