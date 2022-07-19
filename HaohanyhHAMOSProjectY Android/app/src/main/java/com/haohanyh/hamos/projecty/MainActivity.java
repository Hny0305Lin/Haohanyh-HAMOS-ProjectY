/* 受Haohanyh Computer Software Products Open Source LICENSE保护 https://git.haohanyh.top:3001/Haohanyh/LICENSE */
package com.haohanyh.hamos.projecty;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import static com.haohanyh.hamos.projecty.R.*;

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
    private final String TAG = "MainActivity主活动";
    public HuhuaThread huhua;
    //自行设置项目ID和硬件ID，这些都是有办法查得到的，不知道的看我教程！！！！！！
    private String project_id = "";
    private String device_id = "";
    //Timer、Handler和计数器
    final Timer newtimer = new Timer();
    final Handler handler = new Handler();
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
                if(count == 5) {
                    Log.w("浩瀚银河:", "开启联动！");
                }
                if(count >= 5) {
                    HuHuaStart();
                }
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
        Log.i( TAG , "护花进程，开启！" );
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
            Huawei.getHuawei().post();
            String result = Huawei.getHuawei().get("https://iotda.cn-north-4.myhuaweicloud.com/v5/iot/" + project_id + "/devices/" + device_id + "/shadow");
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


}