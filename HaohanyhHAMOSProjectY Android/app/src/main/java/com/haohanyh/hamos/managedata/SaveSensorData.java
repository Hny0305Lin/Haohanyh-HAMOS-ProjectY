package com.haohanyh.hamos.managedata;

import android.annotation.SuppressLint;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;

public class SaveSensorData {
    //浩瀚银河此类保存用户如下信息:
    /* 5. 使用者的护花使者的传感器信息、电机开关情况、传感器信息时间、华为云提交次数
     * (内容为附近温度、空气湿度和土壤湿度)
     */
    private static final String TAG = "浩瀚银河保存传感器文件:";
    int count = 0;
    private String Dir = null;
    public void setDir(String dir) { Dir = dir; }
    private String temp =  null;//附近温度
    private String humi =  null;//空气湿度
    private String soil =  null;//土壤湿度
    private String time = null;//信息时间
    private String num = null;//提交次数

    public SaveSensorData() {};
    public static SaveSensorData NeedSaveData() { return SaveSensorData.savedata.thing; }
    protected static class savedata { private static final SaveSensorData thing = new SaveSensorData(); }
    //HAMOSActivity写入值，用于保存
    public void setTemp(String temp) { this.temp = temp; }
    public void setHumi(String humi) { this.humi = humi; }
    public void setSoil(String soil) { this.soil = soil; }
    public void setTime(String time) { this.time = time; }
    public void setNum(String num) { this.num = num; }

    /* 通过华为云API该页面:https://apiexplorer.developer.huaweicloud.com/apiexplorer/doc?product=IoTDA&api=ShowDeviceShadow
     * 我们可以得知:properties为设备影子的属性数据，event_time为事件操作时间。
     * 华为云对event_time有格式规定：yyyyMMdd'T'HHmmss'Z',如20151212T121212Z。
     * 那么我们可以举例：20220828T180036Z，为2022年8月28号18时00分36秒。
     * 那么，String Stime，就能说明这里注释的一切~
     */
    public String ProcessDataToWriteFile() {
        String Snum = "华为云IoTDA数据记录次数:" + num;
        String Stemp = "附近温度:" + temp;
        String Shumi = "空气湿度:" + humi;
        String Ssoil = "土壤湿度:" + soil;
        String Stime = "信息时间:" + time.substring(0,4) + "年" + time.substring(4,6) + "月" + time.substring(6,8) + "日" + time.substring(9,11) + "时" + time.substring(11,13) + "分" + time.substring(13,15) + "秒";
        return Snum + "\t" + Stemp + "\t" + Shumi + "\t" + Ssoil + "\t" + Stime + "\n";
    }

    /**
     * WriteFile()，保存文件到我们的一亩三分地里~不乱存
     */
    @SuppressLint("SdCardPath")
    public void WriteFile() {
        Log.v(TAG,"开始");
        try {
            File file = new File(Dir + "/HAMOSData");
            if(!file.exists()) { file.mkdir(); }//判断目录是否存在，不存在就得创建
            File file2 = new File(Dir + "/HAMOSData/Sensor");
            if(!file2.exists()) { file2.mkdir(); }//判断目录是否存在，不存在就得创建
            File newFile = new File(file2 + "/Sensor.txt");
            if(!newFile.exists()) { newFile.createNewFile(); }//判断文件是否存在，不存在就得创建
            //经过多次版本测试，我们终于得到了确切目录树: /storage/emulated/0/HAMOSData/Sensor/Sensor.txt
            String Path = Dir + "/HAMOSData/Sensor/Sensor.txt";
            File targetFile = new File(Path);
            RandomAccessFile randomAccessFile = new RandomAccessFile(targetFile,"rw");
            long fileLength = randomAccessFile.length();//神来之笔，我要知道这个JSON里面，存了多少字节了，就靠long fileLength！
            randomAccessFile.seek(fileLength);
            randomAccessFile.write(ProcessDataToWriteFile().getBytes(StandardCharsets.UTF_8));//写入我们保存的用户信息
            randomAccessFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        count++;
        Log.v(TAG,"保存"+count+"次，数据于华为云IoTDA版本为" + num +"，感谢您使用浩瀚银河2.2灰度测试功能!");
    }
}
