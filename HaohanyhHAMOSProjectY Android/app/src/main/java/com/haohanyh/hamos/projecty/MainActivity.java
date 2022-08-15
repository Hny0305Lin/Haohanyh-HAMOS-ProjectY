/* 受Haohanyh Computer Software Products Open Source LICENSE保护 https://git.haohanyh.top:3001/Haohanyh/LICENSE */
package com.haohanyh.hamos.projecty;

import static android.widget.Toast.makeText;
import static com.haohanyh.hamos.dataII.Data.GetData;
import static com.haohanyh.hamos.huawei.Huawei.GetHuawei;
import static com.haohanyh.hamos.projecty.R.color;
import static com.haohanyh.hamos.projecty.R.id;
import static com.haohanyh.hamos.projecty.R.layout;
import static com.haohanyh.hamos.projecty.R.string;
import static com.haohanyh.hamos.projecty.R.style;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.haohanyh.hamos.arithmetic.StringUtils;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends Activity {
    //从写下第一行Java语言的代码开始……到ProjectX的开源……再到今天第二次改版代码，你已经历许多。现在，开启最伟大的物联网探索吧：从早期ProjectX到ProjectY。
    //2.0版本，我们在彻底摆脱部分数据填写，这项目ID硬件ID什么的，慢慢的我们可以不用手动填写了。
    private String project_id = "";
    private String device_id = "";
    //保存的设备信息和当前地区Wi-Fi信息,需要配合下面ToBearPiJSON写入NFC里面,WPA_address和WPA_pwd请手动填写!!!!!!!!!!
    private String Device_info = "";
    private String Device_secret = "";
    private String Product_name = "";
    private String WPA_address = GetData().getWPA_address();
    private String WPA_pwd = GetData().getWPA_pwd();
    private String WPA_address_normal = "";
    private String WPA_pwd_normal = "";
    //通过NFC传给小熊派的JSON!
    private static String ToBearPiJSON = "";

    public static String getToBearPiJSON() { return ToBearPiJSON; }
    //Timer、Handler和计数器
    final Timer newtimer = new Timer();
    int count = 0;
    //2.2 正常启动 所需的控件
    private TableLayout tableLayout, tableLayoutII, tableLayoutIII;
    private EditText Edittext_Iam_domain_name, Edittext_Iam_name, Edittext_Iam_password_input;
    private EditText Edittext_WPA_Ssid, Edittext_WPA_Secret;

    @SuppressLint("RtlHardcoded")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layout.activity_first);
        VersionHAMOSProjectY();
        //2.2正常启动初始化一些控件
        Init();
        //以下为分辨快速启动和正常启动的方式，当然，浩瀚银河很希望用户或开发者，选择快速启动而非正常启动。
        TextView textView = new TextView(this);
        textView.setText("请选择快速启动或正常启动");
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        textView.setPadding(16, 16, 0, 8);
        textView.setGravity(Gravity.LEFT);
        textView.setTextColor(ContextCompat.getColor(this, color.Pink_is_fancy));
        AlertDialog.Builder builder = new AlertDialog.Builder(this,style.HaohanyhDialog)
                .setMessage(string.firstwarning)
                .setCustomTitle(textView)
                .setNegativeButton("快速启动", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        QuickStart();
                        dialogInterface.dismiss();
                    }
                })
                .setPositiveButton("正常启动", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        NormalStart();
                        dialogInterface.dismiss();
                    }
                })
                .setNeutralButton("调试API", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ConsoleAPI();
                        dialogInterface.dismiss();
                    }
                });
        builder.show();
    }

    private void VersionHAMOSProjectY() {
        String a = getString(string.app_version);
        String a1 = a.split("\\.")[0];//版本特性：Alpha Beta Release
        String a2 = a.split("\\.")[1];//版本代数
        String a3 = a.split("\\.")[2];//代数中的第几个版本
        String a4 = a.split("\\.")[3];//该版本测试了多少次
        String a5 = a.split("\\.")[4];//该版本在哪一年发布
        String a6 = a.split("\\.")[5];//该版本在哪一月哪一天发布
        String a7 = a.split("\\.")[6];//开发组织代号
        String a8 = a.split("\\.")[7];//版本代号
        Toast toast = makeText(getApplicationContext(),"ProjectY版本:" + a1+"."+a2+"."+a3+"."+a4 + "\n" + "测试次数:" + a4 + "\n" + "发布于:" + a5+"."+a6 + "\n" + "开发组织:" + a7 + "\n" + "版本代号:" + a8,Toast.LENGTH_SHORT);
        toast.show();
        try {
            //小彩蛋，2019年11月17号9点12分，浩瀚银河正式在这一刻，开始了历史进程。（上线Typecho文章站，再到今天哈哈）
            Thread.sleep(1117);
            toast.cancel();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /*
     * Init()为初始化控件，启动接下来的控件可使用。
     */
    private void Init() {
        //初始化，就不单独开一个函数了
        tableLayout = (TableLayout) getLayoutInflater().inflate(layout.alertdialog_login,null);
        tableLayoutII = (TableLayout) getLayoutInflater().inflate(layout.alertdialog_wifissid,null);
        tableLayoutIII = (TableLayout) getLayoutInflater().inflate(layout.alertdialog_wifisecret,null);
        //非常关键，让EditText属于MainActivity上面的TableLayout，否则会报错！！
        Edittext_Iam_domain_name = tableLayout.findViewById(id.Iam_domain_name);
        Edittext_Iam_name = tableLayout.findViewById(id.Iam_name);
        Edittext_Iam_password_input = tableLayout.findViewById(id.Iam_password_input);
        //同样的道理
        Edittext_WPA_Ssid = tableLayoutII.findViewById(id.WPA_SSID);
        Edittext_WPA_Secret = tableLayoutIII.findViewById(id.WPA_Secret);
    }

    /*
     * QuickStart()为快速启动，启动接下来的所有进程。
     */
    private void QuickStart() {
        //该TimerTask，已经迭代多次了，无Bug，但会在未来新版本做优化，目前先这样吧
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                count++;
                Log.v("浩瀚银河HAMOS启动计时:", String.valueOf(count));
                if(count == 2)                              { Log.v("浩瀚银河:", "护花使者APP已全部开启"); }
                if(count >= 3 && count <= 4)       { Log.v("浩瀚银河:", "网络进度16.5%"); IAMUserBeijing4th();}
                if(count == 5)                              { Log.v("浩瀚银河:", "网络进度55%，即将成功啦~"); Product();}
                if(count == 7)                              { Log.v("浩瀚银河:", "网络进度100%"); Data();}
                if(count == 8)                              { Log.v("浩瀚银河:","即将开始处理NFC"); }
                if(count == 9)                              { count = 10;
                                                                    Intent i = new Intent(MainActivity.this,NFCActivity.class);startActivity(i);newtimer.cancel(); }
            }
        };
        newtimer.schedule(task,300,1000);
    }

    /*
     * NormalStart()为正常启动，手动填写信息后启动进程。
     */
    private void NormalStart() {
        //用户直接打开了GPS（因为方便获取手机连接的Wi-Fi）
        NormalStartNeedGPS();
        //接下来就是输入框了
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this,R.style.ConsoleDialog);
        alertDialog.setTitle("输入华为云账号信息");
        alertDialog.setIcon(R.drawable.a1);
        alertDialog.setView(tableLayout);
        alertDialog.setPositiveButton("点我登录", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //华为账号名的一般设置规则：6-30个字母、数字、下划线、减号或它们的组合，只能以小写字母开头
                //目前写的还算简单，后面肯定会有优化
                String IAMDomainName = Edittext_Iam_domain_name.getText().toString();
                boolean INPUTIAMDomainName = IAMDomainName.length() > 6 && IAMDomainName.length() <= 30                                                                     //6-30个字符
                                                              && StringUtils.GetData().getHuaweiSmallLetter().contains(IAMDomainName.substring(IAMDomainName.length() - 1));    //小写字母开头检测
                Log.v("浩瀚银河:您正确填写了华为账号名了吗?", INPUTIAMDomainName ? "True" : "False");
                //华为云账号名目前并无发现设置规则
                String IAMName = Edittext_Iam_name.getText().toString();
                boolean INPUTIAMName = IAMName.length() > 0;
                Log.v("浩瀚银河:您正确填写了华为云账号名了吗?", INPUTIAMName ? "True" : "False");
                //华为云账号密码，目前也没有发现设置规则，当然，我可以通过自动生成IAM密码中得到方向：
                //          1. 15位左右
                //          2. 大小写字母、数字、符号混合使用
                //根据计算这样生成的一串密码，一台电脑需连续破解需要2亿年左右
                String IAMPassword = Edittext_Iam_password_input.getText().toString();
                Log.v("浩瀚银河:您正确填写了华为云账号密码了吗?", IAMPassword.length() > 0 ? "True" : "False");
                //以下为多余的密码判断功能，计算得到的测试数据结果来源于https://www.security.org/how-secure-is-my-password/
                if(IAMPassword.length() > 12 && StringUtils.GetData().getNormalCharacter().contains(IAMPassword)) { //如果一个密码长度12位以上、且含有标准符号
                    Log.v("浩瀚银河:您华为云账号密码很难破解吗?","非常难，需要正常速度200年以上不间断的破解(不可能)");
                } else if(IAMPassword.length() > 9 && StringUtils.GetData().getNormalCharacter().contains(IAMPassword)) {//如果一个密码长度9位以上、且含有标准符号
                    Log.v("浩瀚银河:您华为云账号密码很难破解吗?","难，需要正常速度3周以上不间断的破解(不可能)");
                } else if(IAMPassword.length() > 6 && StringUtils.GetData().getNormalCharacter().contains(IAMPassword)) {//如果一个密码长度6位以上、且含有标准符号
                    Log.v("浩瀚银河:您华为云账号密码很难破解吗?","难，需要正常速度5秒以上不间断的破解(有可能)");
                } else {
                    Log.v("浩瀚银河:您华为云账号密码很难破解吗?","简单，低于正常速度5秒以上不间断的破解(有可能)");
                }
                dialogInterface.dismiss();
                Log.v("浩瀚银河:", (INPUTIAMDomainName && INPUTIAMName && IAMPassword.length() > 6) ? "我们得到了您的华为云信息" : "我们未能得到您的华为云信息");
                NormalStartLogin(IAMDomainName, IAMName, IAMPassword);
//                if(!NormalStartLogin(IAMDomainName, IAMName, IAMPassword)){
//                    ((ViewGroup) tableLayout.getParent()).removeView(tableLayout);
//                    alertDialog.create().show();
//                }
            }
        });
        alertDialog.setNegativeButton("点我退出", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                makeText(MainActivity.this,"您退出了正常启动...",Toast.LENGTH_SHORT).show();
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        });
        alertDialog.setNeutralButton("调试API", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                makeText(MainActivity.this,"正在完全重启进程...",Toast.LENGTH_SHORT).show();
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        });
        alertDialog.create().show();
    }

    /*
     * NormalStartLogin()为NormalStart()附属进程，填写华为云信息进行API判断。
     * @String IAMDomainName: 华为云账号名，如果是华为账号一般为账号用户名
     * @String IAMName: 华为云用户名
     * @String IAMPassword: 华为云密码
     */
    private void NormalStartLogin(String IAMDomainName, String IAMName, String IAMPassword) {
        if(IAMDomainName.length() > 0 && IAMName.length() > 0 && IAMPassword.length() > 6) {
            makeText(getApplicationContext(),"正常启动: 正在登录中",Toast.LENGTH_SHORT).show();
            Log.v("浩瀚银河:","传值成功");
            //正常启动流程：
            //传值->组成JSON->Post传JSON值获取Token->获取项目ID->创建产品->创建IoTDA设备->查询IoTDA设备是否创建成功↴
            //                  进入NFC页面交给NFC类处理剩下的吧<-填充WPA账号，密码需要用户手动填写<-获取用户此时手机WiFi数据
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    TimerTask task = new TimerTask() {
                        @Override
                        public void run() {
                            count++;
                            Log.v("浩瀚银河HAMOS，2.2正常启动计时:", String.valueOf(count));
                            if(count >= 3 && count <= 4)       { Log.v("浩瀚银河:", "正在Post传JSON值获取Token->获取项目ID"); IAMUserBeijing4th();}
                            if(count == 5)                              { Log.v("浩瀚银河:", "创建产品->创建IoTDA设备->查询IoTDA设备是否创建成功，即将成功啦~"); Product();}
                            if(count == 6)                              { Device_info = GetHuawei().getDevice_info();
                                                                                Device_secret = GetHuawei().getDevice_secret();
                                                                                Product_name = GetHuawei().getProduct_name();
                                                                                Log.v("浩瀚银河:","\n2.2正常启动，设备ID="+Device_info);
                                                                                Log.v("浩瀚银河:","\n2.2正常启动，设备密钥="+Device_secret);
                                                                                Log.v("浩瀚银河:","\n2.2正常启动，产品名称="+Product_name); }
                            if(count == 8)                              { count = 9; }
                        }
                    };
                    newtimer.schedule(task,300,1000);
                }
            });thread.start();
            //弹窗代码了，最为重要。
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this,R.style.ConsoleDialog);
            alertDialog.setTitle("键入"+WiFi()+"的密码");
            alertDialog.setIcon(R.drawable.a1);
            alertDialog.setView(tableLayoutIII);
            alertDialog.setPositiveButton("输入完啦", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    //两行，处理Wi-Fi信息，一行，处理正常启动JSON，再来两行，去NFC页面开启NFC写入数据。
                    WPA_address_normal = WiFi();
                    WPA_pwd_normal = String.valueOf(Edittext_WPA_Secret.getText());
                    ToBearPiJSON = "{\"product\": \""+ Product_name +"\",\"device_id\": \""+Device_info+"\",\"secret\": \""+ Device_secret +"\",\"ssid\": \""+ WPA_address_normal +"\",\"pwd\": \""+ WPA_pwd_normal +"\"}";
                    Intent normali = new Intent(MainActivity.this,NFCActivity.class);
                    startActivity(normali);
                }
            });
            alertDialog.create().show();
        } else {
            makeText(getApplicationContext(),"您输入的不正确...",Toast.LENGTH_SHORT).show();
            Log.v("浩瀚银河:","传值失败");
        }
    }

    /*
     * WiFi()为NormalStart()附属进程，读取Wi-Fi账号，安卓11.0以下设备均测试过。
     */
    private String WiFi() {
        //以下为获取Wifi的SSID方法，密码我们不获取，这样做的话软件得ROOT
        //（或者各种恶心的办法，比如捕获屏幕得到分享二维码读JSON，我们不做，因为我们是浩瀚银河）
        //搞一个弹窗，输入得到的WiFi名称的密码，因为要组成NFC的JSON内容，给小熊派护花使者。
        String WPAAddress = "";
        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        if (wifiInfo != null) {
            WPAAddress = wifiInfo.getSSID();
            Log.v("浩瀚银河:","Wi-Fi名称："+WPAAddress);
            WPA_address_normal = WPAAddress;
            if (WPAAddress.length() > 2 && WPAAddress.charAt(0) == '"' && WPAAddress.charAt(WPAAddress.length() - 1) == '"') {
                WPAAddress = WPAAddress.substring(1,WPAAddress.length() - 1);
                if (WPAAddress.equals("<unknown ssid>")) {
                    ConnectivityManager connManager = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
                    assert connManager != null;
                    NetworkInfo networkInfo = connManager.getActiveNetworkInfo();
                    if (networkInfo.isConnected()) {
                        if (networkInfo.getExtraInfo() != null) {
                            WPAAddress = networkInfo.getExtraInfo().replace("\"", "");
                            Log.v("浩瀚银河:","Wi-Fi名称："+WPAAddress);
                            WPA_address_normal = WPAAddress;
                            return WPA_address_normal;
                        }
                    }
                }
            }
        }
        return WPA_address_normal;
    }

    /*
     * NormalStartNeedGPS()为NormalStart()附属进程，获取GPS权限并且做读取Wi-Fi账号的准备
     */
    private void NormalStartNeedGPS() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {//未开启定位权限
            //开启定位权限,201是标识码
            this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 201);
        }
        LocationManager alm= (LocationManager) this.getSystemService(Context.LOCATION_SERVICE );
        if(alm.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER )){
            Toast toastGPS = makeText( this, "您已打开GPS", Toast.LENGTH_SHORT );
            toastGPS.show();
            try {
                Thread.sleep(500);
                toastGPS.cancel();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText( this, "请打开GPS定位\n方便获取当前连接的Wi-Fi名称", Toast.LENGTH_SHORT ).show();
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivityForResult(intent,0);
        }
    }



    /*
     * ConsoleAPI()为2.2未来功能，由于我们灰度测试发现，华为云很多函数可能在某些宽带下，极易造成使用不佳。
     * 那么我们可以为开发者，提供一个API测试桥梁，让开发者更好的更清楚的测试API情况。
     * 当然，目前做个入口，现在不能用哒~
     * 推动华为函数1.4版本，迈向调试新方向。
     */
    private void ConsoleAPI() {
        final String [] s ={
                "KeystoneCreateUserTokenByPassword\n\t\t获取IAM用户Token(使用密码)"
                ,"KeystoneListProjects\n\t\t查询指定条件下的项目列表"
                ,"CreateProduct\n\t\t创建产品"
                ,"AddDevice\n\t\t创建IoTDA设备"
                ,"ListDevices\n\t\t查询设备列表"
                ,"ResetDeviceSecret\n\t\t重置设备密钥"
                ,"ShowDeviceShadow\n\t\t查询设备影子数据"
                ,"CreateCommand\n\t\t下发设备命令"
                ,"Haohanyh_OranMeCDN_ListDevices\n\t\t浩瀚银河居若科技联动函数"};
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this,R.style.ConsoleDialog);
        alertDialog.setTitle("root@HAMOS:/projecty# _");
        alertDialog.setIcon(R.drawable.a1);
        alertDialog.setItems(s, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                makeText(MainActivity.this,"您选择了: " + s[which],Toast.LENGTH_SHORT).show();
                Log.v("浩瀚银河:","您选择了: " + s[which]);
                alertDialog.create().show();
            }
        });
        alertDialog.setNeutralButton("离开调试", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                makeText(MainActivity.this,"正在完全重启进程...",Toast.LENGTH_SHORT).show();
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        });
        alertDialog.create().show();
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
        //3+3+1，7行代码，华为函数轻而易举。
        Device_info = GetHuawei().getDevice_info();
        Device_secret = GetHuawei().getDevice_secret();
        Product_name = GetHuawei().getProduct_name();
        Log.v("浩瀚银河:","设备ID="+Device_info);
        Log.v("浩瀚银河:","设备密钥="+Device_secret);
        Log.v("浩瀚银河:","产品名称="+Product_name);
        ToBearPiJSON = "{\"product\": \""+ Product_name +"\",\"device_id\": \""+Device_info+"\",\"secret\": \""+ Device_secret +"\",\"ssid\": \""+ WPA_address +"\",\"pwd\": \""+ WPA_pwd +"\"}";
    }
}