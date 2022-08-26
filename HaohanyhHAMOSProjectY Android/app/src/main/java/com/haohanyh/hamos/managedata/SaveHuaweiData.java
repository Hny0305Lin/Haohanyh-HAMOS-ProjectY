/* 受Haohanyh Computer Software Products Open Source LICENSE保护 https://git.haohanyh.top:3001/Haohanyh/LICENSE */
package com.haohanyh.hamos.managedata;

import static com.haohanyh.hamos.dataI.Data.GetData;

import android.annotation.SuppressLint;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;

public class SaveHuaweiData {
    //浩瀚银河此类保存用户如下信息:
    /* 1. 使用者的华为云IAM所属名
     * 2. 使用者的华为云IAM用户名
     * 3. 使用者的华为云IAM密码
     */
    //并且，会进行文件保存目录输出，让使用者更方便的让数据掌握在自己手上。
    //最后，会进行文件内加密混淆，混淆代码也会在该SaveXXXXData类里加工处理。
    private static final String TAG = "浩瀚银河保存HUAWEI文件:";
    private String Dir = null;
    public void setDir(String dir) { Dir = dir; }
    private String wergjiker =  null;//第一个
    private String wergofjer =  null;//第二个
    private String werobfker =  null;//第三个

    public SaveHuaweiData() {};
    public static SaveHuaweiData GetSaveData() { return SaveHuaweiData.savedata.thing; }
    protected static class savedata { private static final SaveHuaweiData thing = new SaveHuaweiData(); }

    public String ProcessDataToWriteFile() {
        wergjiker = GetData().getJsonDomainName();
        wergofjer = GetData().getJsonName();
        werobfker = GetData().getJsonPassword();
        //取得值后，接下来得组成一个JSON框架，当然这个框架很简单。
        JSONObject First = new JSONObject();
        try {
            First.put("Domain",wergjiker);
            First.put("Name",wergofjer);
            First.put("Pwd",werobfker);
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
            File file = new File(Dir + "/HAMOSData");
            if(!file.exists()) { file.mkdir(); }//判断目录是否存在，不存在就得创建
            File newFile = new File(file + "/User.json");
            if(!newFile.exists()) { newFile.createNewFile(); }//判断文件是否存在，不存在就得创建
            //经过多次版本测试，我们终于得到了确切目录树: /data/user/0/com.haohanyh.hamos.projecty/files/HAMOSData
            String Path = "/data/user/0/com.haohanyh.hamos.projecty/files/HAMOSData/User.json";
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
