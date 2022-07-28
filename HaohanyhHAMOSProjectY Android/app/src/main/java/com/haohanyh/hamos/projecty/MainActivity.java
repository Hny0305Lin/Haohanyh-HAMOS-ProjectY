/* 受Haohanyh Computer Software Products Open Source LICENSE保护 https://git.haohanyh.top:3001/Haohanyh/LICENSE */
package com.haohanyh.hamos.projecty;

import static com.haohanyh.hamos.dataII.Data.GetData;
import static com.haohanyh.hamos.huawei.Huawei.GetHuawei;
import static com.haohanyh.hamos.projecty.R.layout;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends Activity {
    //从写下第一行Java语言的代码开始……到ProjectX的开源……再到今天第二次改版代码，你已经历许多。现在，开启最伟大的物联网探索吧：从早期ProjectX到ProjectY。
    //2.0版本，我们在彻底摆脱部分数据填写，这三个项目ID硬件ID什么的，慢慢的我们可以不用手动填写了。
    private String project_id = "";
    private String device_id = "";
    //保存的设备信息和当前地区Wi-Fi信息,需要配合下面ToBearPiJSON写入NFC里面,WPA_address和WPA_pwd请手动填写!!!!!!!!!!
    private String Device_info = "";
    private String Device_secret = "";
    private String Product_name = "";
    private String WPA_address = GetData().getWPA_address();
    private String WPA_pwd = GetData().getWPA_pwd();
    //通过NFC传给小熊派的JSON!
    private static String ToBearPiJSON = "";
    public static String getToBearPiJSON() { return ToBearPiJSON; }
    //Timer、Handler和计数器
    final Timer newtimer = new Timer();
    int count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layout.activity_first);
        Toast.makeText(MainActivity.this,"使用本软件请提前打开NFC",Toast.LENGTH_SHORT).show();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                count++;
                Log.v("浩瀚银河HAMOS启动计时:", String.valueOf(count));
                if(count == 2)                              { Log.v("浩瀚银河:", "护花使者APP已全部开启"); }
                if(count >= 3 && count <= 4)        { Log.v("浩瀚银河:", "网络进度16.5%"); IAMUserBeijing4th();}
                if(count == 5)                              { Log.v("浩瀚银河:", "网络进度55%，即将成功啦~"); Product();}
                if(count == 7)                              { Log.v("浩瀚银河:", "网络进度100%"); Data();}
                if(count == 8)                              { Log.v("浩瀚银河:","即将开始处理NFC"); }
                if(count == 9)                              { count = 10;Intent i = new Intent(MainActivity.this,NFCActivity.class);startActivity(i);newtimer.cancel(); }
            }
        };
        newtimer.schedule(task,300,1000);
    }

    /*
     * IAMUserBeijing4th()为获取IAM用户在北京4区的项目ID，做到自动化处理信息。
     */
    private void IAMUserBeijing4th() {
        String id = "";
        GetHuawei().Knowtokenneedpost();
        id = GetHuawei().Knowprojectneedget("https://iam.cn-north-4.myhuaweicloud.com/v3/projects?domain_id=");
        project_id = id;
    }

    /*
     * Product()为创建产品到数据提供の自动化流程（如果创建过也是不会再次创建的，所以这个函数跑一下倒没多大事），做到自动化解决创建产品难题。
     */
    private void Product() {
        //创建产品
        GetHuawei().CreateProductinIoTDAneedpost();
        //创建IoTDA设备
        GetHuawei().CreateDeviceIoTDAneedpost();
        //查询IoTDA设备是否创建成功（除了网络问题就是100%成功）然后包了重置密钥
        GetHuawei().SearchDeviceIoTDAneedget();
    }

    /*
     * Data()为Product()函数里面的最后一步，把数据从Huawei.java丢过来
     */
    private void Data() {
        //4+4，8行代码，华为函数轻而易举。
        Device_info = GetHuawei().getDevice_info();
        Device_secret = GetHuawei().getDevice_secret();
        Product_name = GetHuawei().getProduct_name();
        Log.v("浩瀚银河:","设备ID="+Device_info);
        Log.v("浩瀚银河:","设备密钥="+Device_secret);
        Log.v("浩瀚银河:","产品名称="+Product_name);
        ToBearPiJSON = "{\"product\": \""+ Product_name +"\",\"device_id\": \""+Device_info+"\",\"secret\": \""+ Device_secret +"\",\"ssid\": \""+ WPA_address +"\",\"pwd\": \""+ WPA_pwd +"\"}";
    }

}