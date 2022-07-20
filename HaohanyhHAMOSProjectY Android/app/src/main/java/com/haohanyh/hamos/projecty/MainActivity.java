/* 受Haohanyh Computer Software Products Open Source LICENSE保护 https://git.haohanyh.top:3001/Haohanyh/LICENSE */
package com.haohanyh.hamos.projecty;

import static com.haohanyh.hamos.huawei.Huawei.GetHuawei;
import static com.haohanyh.hamos.projecty.R.drawable;
import static com.haohanyh.hamos.projecty.R.id;
import static com.haohanyh.hamos.projecty.R.layout;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends Activity {
    //从写下第一行Java语言的代码开始……到ProjectX的开源……再到今天第二次改版代码，你已经历许多。现在，开启最伟大的物联网探索吧：从早期ProjectX到ProjectY。
    public ImageView ImageTemp;
    public ImageView ImageHumi;
    public ImageView ImageSoil;
    public TextView TxtTemp;
    public TextView TxtHumi;
    public TextView TxtSoil;
    public Button Btn;
    //进程类，继承父类Threads
    public HuhuaThread huhua;
    public WaterControlThread water;
    //bool
    private volatile boolean Ctrl = false;
    //自行设置项目ID和硬件ID，这些都是有办法查得到的，不知道的看我教程！！！！！！
    private final String project_id = "";
    private final String device_id = "";
    //服务ID、命令名字、命令服务名、命令属性（也就是上报值），后面的链接是在线调试，可以帮助你得到这些变量。
    //（配置好你的板子的过程，也配置了这些，如果忘记了但不想重新创建设备，就这么找吧）
    //https://console.huaweicloud.com/iotdm/?region=cn-north-4#/dm-portal/monitor/online-debugger
    //⚠警告，command_value是可以改变为ON的，因为我没设置成final，这里注意一下也不能写错哦。（不过也不用写On，硬件有代码浇花一定时间后会硬件级暂停）
    private final String service_id = "AutoWater";
    private final String command_name = "AutoWater_Control_Pump";
    private final String command_param = "Motor";
    private String command_value = "OFF";
    //Timer、Handler和计数器
    final Timer newtimer = new Timer();
    final Handler handler = new Handler();
    final Handler handlerII = new Handler();
    int count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layout.activity_main);
        Init();
        //建议5s延缓启用，对Android的onCreate生命周期减少负担，也为自己的软件保证稳定性。
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                count++;
                Log.w("浩瀚银河HAMOS启动计时:", String.valueOf(count));
                if(count == 5) { Log.w("浩瀚银河:", "护花进程开启！"); }
                if(count >= 5) { HuHuaStart(); }
                if(count == 10) { Log.w("浩瀚银河:", "浇花进程开启！"); }
                if(count >= 10) { WaterStart(); }
            }
        };
        newtimer.schedule(task,100,1000);
    }

    /*
     * Init()为初始化各类元素
     */
    private void Init() {
        ImageTemp = findViewById(id.IItemp);
        ImageHumi = findViewById(id.IIHumi);
        ImageSoil = findViewById(id.IISoil);
        TxtTemp = findViewById(id.txtwendu);
        TxtHumi = findViewById(id.txtshidu);
        TxtSoil = findViewById(id.txtsoil);
        Btn = findViewById(id.kaiqi);
    }

    /*
     * HuHuaStart()为护花进程开启
     */
    private void HuHuaStart(){
        Log.i( "Action!" , "护花进程，开启！" );
        huhua = new HuhuaThread();
        huhua.start();
    }

    /*
     * HuHua进程类
     */
    public class HuhuaThread extends Thread {
        @Override
        /*
         * 自动跑，不用理它，我基本上帮你们写好了这些了。
         */
        public void run() {
            GetHuawei().Post();
            String result = GetHuawei().Get("https://iotda.cn-north-4.myhuaweicloud.com/v5/iot/" + project_id + "/devices/" + device_id + "/shadow");
            //我们把从华为云，通过get函数查询到的设备数据，做数据显示和浇花控制。
            DataChangeShow(result);
        }
        /*
         * 定制函数，显示数据到页面的。
         */
        private void DataChangeShow(String result) {
            try {
                JSONObject jsonObj = new JSONObject(result);
                JSONArray jsonArray = jsonObj.getJSONArray("shadow");
                System.out.println("浩瀚银河函数:shadow=====" + jsonArray);
                for(int i = 0;i < jsonArray.length(); i++){
                    //第一层，reported，我们要的是后面的json，没事，我们慢慢转
                    JSONObject obj = jsonArray.getJSONObject(i);
                    Object reported = obj.get("reported");
                    //第二层，properties，里面就有我们要的数据了
                    JSONObject Two = new JSONObject(String.valueOf(reported));
                    String properties = Two.getString("properties");
                    //最后一层，我们取里面，传感器上报到华为云的数据，这一些代码才是精华
                    JSONObject Last = new JSONObject(properties);
                    String TemperatureResult = Last.getString("Temperature");
                    String HumidityResult = Last.getString("Humidity");
                    String SoilResult = Last.getString("Soil_Moisture");
                    //我们使用handler循环即可，已经写了那么多次了，该排查的问题都排查得清清楚楚了，所以没有问题~
                    handler.post(new Runnable() {
                        @SuppressLint({"SetTextI18n"})
                        @Override
                        public void run() {
                            //显示
                            TxtTemp.setText("附近温度："+"\t"+ TemperatureResult);
                            TxtHumi.setText("空气湿度："+"\t"+ HumidityResult);
                            TxtSoil.setText("土壤湿度："+"\t"+ SoilResult);
                            //String转int，然后判断，达到临界值后，修改页面图片做提示
                            int Temp = Integer.parseInt(TemperatureResult);
                            int Humi = Integer.parseInt(HumidityResult);
                            int Soil = Integer.parseInt(SoilResult);
                            //临界值：温度大于30小于20；湿度大于70小于40；土壤湿度大于44小于17
                            if(Temp < 20 || Temp > 30) {
                                ImageTemp.setImageResource(drawable.temp_warn);
                            } else {
                                ImageTemp.setImageResource(drawable.temp);
                            }
                            if(Humi < 40 || Humi > 70) {
                                ImageHumi.setImageResource(drawable.humi_warn);
                            } else {
                                ImageHumi.setImageResource(drawable.humi);
                            }
                            if(Soil < 17 || Soil > 44) {
                                ImageSoil.setImageResource(drawable.soil_warn);
                            } else {
                                ImageSoil.setImageResource(drawable.soil);
                            }
                        }
                    });
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    /*
     * WaterStart()为护花进程开启后，启动的智能判断临界值情况，里面的算法其实很简单，就是触发到时设备浇花。
     */
    private void WaterStart(){
        Log.i( "Action!" , "浇花进程，开启！" );
        water = new WaterControlThread();
        water.start();
    }

    /*
     * Water进程类
     * 触发时，浇花3s后，暂停浇花。
     */
    private class WaterControlThread extends Thread {
        @Override
        public void run() {
            Btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    command_value = "ON";
                    Toast.makeText(MainActivity.this, "浇花中", Toast.LENGTH_SHORT).show();
                    WaterSomeFlower();
                    Toast.makeText(MainActivity.this, "浇花完成", Toast.LENGTH_SHORT).show();
                }
            });
        }

        /*
         * 定制函数，浇花函数。
         */
        private void WaterSomeFlower() {
            //请保证6个东西你都填写了再执行该函数~
            GetHuawei().CreateJsonToControlSenderneedPost(project_id,device_id,service_id,command_name,command_param,command_value);
        }
    }
}