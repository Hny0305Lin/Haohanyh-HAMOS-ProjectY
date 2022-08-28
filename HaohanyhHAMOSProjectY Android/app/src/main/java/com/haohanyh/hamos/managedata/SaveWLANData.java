/* 受Haohanyh Computer Software Products Open Source LICENSE保护 https://git.haohanyh.top:3001/Haohanyh/LICENSE */
package com.haohanyh.hamos.managedata;

import static com.haohanyh.hamos.dataII.Data.GetData;

import android.annotation.SuppressLint;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;

public class SaveWLANData {
    //浩瀚银河此类保存用户如下信息:
    /* 4. 使用者对护花使者接入网络的WLAN信息
     * (内容为WLAN SSID和WLAN Secret两份)
     */
    //并且，会进行文件保存目录输出，让使用者更方便的让数据掌握在自己手上。
    //最后，会进行文件内加密混淆，混淆代码也会在该SaveXXXXData类里加工处理。
    private static final String TAG = "浩瀚银河保存WLAN文件:";
    private String Dir = null;
    public void setDir(String dir) { Dir = dir; }
    private String weruygker =  null;private String werrgfjer =  null;//第四个

    public SaveWLANData() {};
    public static SaveWLANData NeedSaveData() { return SaveWLANData.savedata.thing; }
    protected static class savedata { private static final SaveWLANData thing = new SaveWLANData(); }

    public String ProcessDataToWriteFile() {
        weruygker = GetData().getWPA_pwd();
        werrgfjer = GetData().getWPA_address();
        //取得值后，接下来得组成一个JSON框架，当然这个框架很简单。
        JSONObject First = new JSONObject();
        try {
            First.put("Secret",weruygker);
            First.put("SSID",werrgfjer);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return String.valueOf(First);
    }

    /**
     * WriteFile()，保存文件到我们的一亩三分地里~不乱存
     */
    @SuppressLint("SdCardPath")
    public boolean WriteFile() {
        Log.v(TAG,"开始");
        try {
            File fileII = new File(Dir + "/HAMOSData");
            if(!fileII.exists()) { fileII.mkdir(); }//判断目录是否存在，不存在就得创建
            File newFileII = new File(fileII + "/Wlan.json");
            if(!newFileII.exists()) { newFileII.createNewFile(); }//判断文件是否存在，不存在就得创建
            //经过多次版本测试，我们终于得到了确切目录树: /data/user/0/com.haohanyh.hamos.projecty/files/HAMOSData
            String Path = "/data/user/0/com.haohanyh.hamos.projecty/files/HAMOSData/Wlan.json";
            File targetFile = new File(Path);
            RandomAccessFile randomAccessFile = new RandomAccessFile(targetFile,"rw");
            randomAccessFile.seek(0);//写一次而不是在上一次基础上继续写，而是覆写!!!
            randomAccessFile.write(ProcessDataToWriteFile().getBytes(StandardCharsets.UTF_8));//写入我们保存的用户信息
            randomAccessFile.close();
            Log.v(TAG,"结束");
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}
