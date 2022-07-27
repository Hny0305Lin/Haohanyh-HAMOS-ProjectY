/* 受Haohanyh Computer Software Products Open Source LICENSE保护 https://git.haohanyh.top:3001/Haohanyh/LICENSE */
package com.haohanyh.hamos.huawei;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Huawei {
    //获取得到的HUAWEI华为云Token、IAM账号下的北京4区Domain_id、IoTDA项目ID和设备数据，不许填写(因为我们写好了自动化处理)！！！
    protected String HUAWEITOKEN = "";
    protected String Domain_id = "";
    private String Project_id = "";
    private String S_data_device_id = "";
    protected String S_data_product_name = "";
    //需要添加的IAM账号名、账号密码。想要获取IAM信息？点它即可→https://support.huaweicloud.com/api-iam/iam_17_0002.html
    //这三个，请手动填写！！！！！！！！！！！！！！！！！！！！！！
    protected final String JsonDomainName = "";
    protected final String JsonName = "";
    protected final String JsonPassword = "";
    //保存的设备信息，不要填写！！！！！！！！
    private String Device_info = "";
    private String Device_secret = "";
    private String Product_name = "";
    //下面老三样，不要动！！！
    protected Huawei() { }
    public static Huawei GetHuawei() { return huawei.network; }
    protected static class huawei { private static final Huawei network = new Huawei(); }
    //重构，方便获取设备信息
    public String getProduct_name() { return Product_name; }
    public String getDevice_info() { return Device_info; }
    public String getProject_id() { return Project_id; }
    public String getDevice_secret() { return Device_secret; }

    /*
     * 创建JSON转String，方便Knowtokenneedpost()函数(KeystoneCreateUserTokenByPassword 获取IAM用户Token(使用密码))，进行使用和理解。
     */
    public String CreateJsonToKnowtokenneedpost() {
        JSONObject First = new JSONObject();
        try {
            JSONObject Auth = new JSONObject();
            First.put("auth",Auth);
            JSONObject Identity = new JSONObject();
            Auth.put("identity",Identity);
            JSONObject Password = new JSONObject();
            Identity.put("password",Password);
            JSONArray Methods = new JSONArray();
            Methods.put("password");
            Identity.put("methods",Methods);
            JSONObject User = new JSONObject();
            JSONObject UserDomain = new JSONObject();
            User.put("domain",UserDomain);
            UserDomain.put("name",JsonDomainName);
            Password.put("user",User);
            User.put("name",JsonName);
            User.put("password",JsonPassword);
            JSONObject Scope = new JSONObject();
            Auth.put("scope",Scope);
            JSONObject Domain = new JSONObject();
            Scope.put("domain",Domain);
            Domain.put("name",JsonDomainName);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return String.valueOf(First);
    }

    /*
     * 创建JSON转String，方便Knowtokenneedpost()函数(KeystoneCreateUserTokenByPassword 获取IAM用户Token(使用密码))，获取Domain_id_Temp，传值给MainActivity，然后使用Knowprojectneedget()函数。
     */
    public void CreateJsonToKnowDomainidneedpose(String json) {
        try {
            JSONObject jsonObjI = new JSONObject(json);
            JSONObject jsonObjII = jsonObjI.getJSONObject("token");
            JSONObject jsonObjIII = jsonObjII.getJSONObject("domain");
            Domain_id = jsonObjIII.getString("id");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /*
     * 创建JSON转String，方便ControlSenderneedpost()函数(CreateCommand 下发设备命令)，进行使用和理解。
     */
    public void CreateJsonToControlSenderneedpost(String project_id,String device_id,String service_id,String command_name,String command_param,String command_value) {
        String jsonParas = "";
        JSONObject object = new JSONObject();
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("service_id", service_id);
            jsonObject.put("command_name", command_name);
            object.put(command_param, command_value);
            jsonObject.put("paras", object);
            jsonParas = jsonObject.toString();
            ControlSenderneedpost(jsonParas,project_id,device_id);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /*
     * 创建String转Json，方便Knowprojectneedget()函数(KeystoneListProjects 查询指定条件下的项目列表)，进行数据处理。
     */
    public String CreateStringToKnowprojectneedget(String content) {
        String id = "";//id保存的地方
        try {
            JSONObject jsonObj = new JSONObject(content);
            JSONArray jsonArray = jsonObj.getJSONArray("projects");
            for (int i = 0; i < jsonArray.length(); i++) {
                //这个函数特别注意，我们得去取projects里面的数组，因为API获取到的是IAM用户名下所有华为云地区的所有id，我们只需要cn-north-4北京4区即可。
                Object json6 = jsonArray.getJSONObject(6);
                //然后我们就是去取id了。
                JSONObject north4id = new JSONObject(String.valueOf(json6));
                id = north4id.getString("id");
            }
            return id;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return id;
    }

    /*
     * 创建String转Json，方便CreateProductinIoTDAneedpost()函数(CreateProduct 创建产品)，提前数据处理提交JSON参数。
     * 后续会重点优化此处JSON代码，埋坑+1
     */
    public String CreateStringToCreateProductinIoTDAneedpost() {
        //华为云API调试亲测能通过
        //API调试Demo地址：https://apiexplorer.developer.huaweicloud.com/apiexplorer/debug?historyId=155555bba0a949228b9fd020862445f2
        //感谢华为云有这样的功能，可以给初学者，直接上手测试API在自己的账号的情况。Thanks~
        //提前写好需要提交进JSON参数的变量。
        final String JSON_product_id = "HaohanyhHhsz";
        final String JSON_name = "BearPi_AutoWater";
        final String JSON_device_type_service_type = "Senser";
        final String JSON_protocol_type = "MQTT";
        final String JSON_data_format = "json";
        //分割线，现在是服务参数变量
        final String JSON_service_id = "AutoWater";
        //分割线，现在是命令参数变量
        final String JSON_command_name_autowater_control = "AutoWater_Control_Pump";
        final String JSON_command_name_autowater_threshold = "AutoWater_Threshold";
        //分割线，现在是events参数，目前暂不清楚是什么，先创建再说
        final String JSON_events_event_type = "AAA";
        //分割线，厂商名称和设备所属行业，这个可以自定义
        final String JSON_manufacturer_name = "HAMOSProjectY-BearPi-Huhuashizhe";
        final String JSON_industry = "IOT-Industry";
        //写了这么多变量，那么接下来，就是组成一串JSON代码然后提交给华为云创建IoTDA产品了。
        String result = "";
        try {
            /*
             * UTC/GMT+08:00,2022.7.23,0:32
             * 我终于完成了这些代码，然后还有打印成Log.i输出在Console里了。
             * 首先，AA区域是service_capabilities.properties，记录了护花使者上报华为云IoTDA的传感器值和控制值、控制情况。
             * 其次，AB区域是service_capabilities.commands，记录了护花使者上报华为云IoTDA的命令情况。
             * 最后，AC区域是service_capabilities.events，记录了events参数，目前暂不清楚是什么，先创建再说。
             * 再来，A区域是service_capabilities，记录了service_capabilities一整个内容，包装成String类JSON东西打印出来，虽然有斜杠但是在JSON里面它们非常好。
             * 继续，First区域就是一整个最大的JSON数据体了。
             * 芜湖，我们可以写上报了~
             */
            String properties_temp = "{\"property_name\": \"Temperature\",\"data_type\": \"int\",\"min\": \"0\",\"max\": \"65535\",\"method\": \"RWE\"}";
            String properties_humi = "{\"property_name\": \"Humidity\",\"data_type\": \"int\",\"min\": \"0\",\"max\": \"65535\",\"method\": \"RWE\"}";
            String properties_soil = "{\"property_name\": \"Soil_Moisture\",\"data_type\": \"int\",\"min\": \"0\",\"max\": \"65535\",\"method\": \"RWE\"}";
            String properties_motor = "{\"property_name\": \"MotorStatus\",\"data_type\": \"string\",\"enum_list\": [\"ON\",\"OFF\"],\"max_length\": 3,\"method\": \"RWE\"}";
            String properties_current = "{\"property_name\": \"Current_Threshold\",\"data_type\": \"int\",\"min\": \"0\",\"max\": \"65535\",\"method\": \"RWE\"}";
            ArrayList<String> Properties = new ArrayList<>();
            Properties.add(0,properties_temp);
            Properties.add(1,properties_humi);
            Properties.add(2,properties_soil);
            Properties.add(3,properties_motor);
            Properties.add(4,properties_current);
            //去掉头尾[]的操作，很熟悉吧~
            String AA = Properties.toString().replaceFirst("\\[","");
            AA = AA.substring(0, AA.length() - 1);
            //Log.i("service_capabilities.properties",AA);
            //分割线，上面AA区域，下面AB区域
            String command_AutoWaterControlPump_paras = "[{\"para_name\": \"Motor\",\"data_type\": \"string\",\"enum_list\": [\"ON\",\"OFF\"],\"max_length\": 3}]";
            JSONArray Commands_AutoWaterControlPump_paras = new JSONArray(command_AutoWaterControlPump_paras);
            JSONObject Command_name_1st = new JSONObject();
            Command_name_1st.put("command_name",JSON_command_name_autowater_control);
            Command_name_1st.put("paras",Commands_AutoWaterControlPump_paras);
            String command_AutowaterThreshold_paras = "[{\"para_name\": \"Threshold\",\"data_type\": \"int\",\"min\": \"0\",\"max\": \"100\",\"step\": 0}]";
            JSONArray Commands_AutowaterThreshold_paras = new JSONArray(command_AutowaterThreshold_paras);
            JSONObject Command_name_2nd = new JSONObject();
            Command_name_2nd.put("command_name",JSON_command_name_autowater_threshold);
            Command_name_2nd.put("paras",Commands_AutowaterThreshold_paras);
            String AB = "[" + Command_name_1st.toString() + "," + Command_name_2nd.toString() + "]";
            //Log.i("service_capabilities.commands",AB);
            //分割线，上面AB区域，下面AC区域
            String events_paras = "[{\"para_name\": \"AAA\", \"data_type\": \"int\" }]";
            JSONArray Events_Paras = new JSONArray(events_paras);
            JSONObject Events = new JSONObject();
            Events.put("event_type",JSON_events_event_type);
            Events.put("paras",Events_Paras);
            String AC = "[" + Events.toString() + "]";
            //Log.i("service_capabilities.events", AC);
            //分割线，上面AC区域，下面A区域
            result = "{\"product_id\":\""+JSON_product_id+"\",\"name\":\""+JSON_name+"\",\"device_type\":\""+JSON_device_type_service_type+"\",\"protocol_type\":\""+JSON_protocol_type+"\",\"data_format\":\""+JSON_data_format+"\",\"service_capabilities\":[{\"service_id\":\""+JSON_service_id+"\",\"service_type\":\"Senser\",\"properties\":["+ AA +"],\"commands\":"+ AB +",\"events\":"+ AC +"}],\"manufacturer_name\":\""+JSON_manufacturer_name+"\",\"industry\":\""+JSON_industry+"\"}";
            //Log.i("ADistrict",result);
            //不到万不得已请不要使用↓下面这result，请保证函数的可阅读性和可维护性。
            //result = "{\"product_id\":\"HaohanyhHhsz\",\"name\":\"BearPi_AutoWater\",\"device_type\":\"Senser\",\"protocol_type\":\"MQTT\",\"data_format\":\"json\",\"service_capabilities\":[{\"service_id\":\"AutoWater\",\"service_type\":\"Senser\",\"properties\":[{\"property_name\":\"Temperature\",\"data_type\":\"int\",\"min\":\"0\",\"max\":\"65535\",\"method\":\"RE\"},{\"property_name\":\"Humidity\",\"data_type\":\"int\",\"min\":\"0\",\"max\":\"65535\",\"method\":\"RE\"},{\"property_name\":\"Soil_Moisture\",\"data_type\":\"int\",\"min\":\"0\",\"max\":\"65535\",\"method\":\"RE\"},{\"property_name\":\"MotorStatus\",\"data_type\":\"string\",\"enum_list\":[\"ON\",\"OFF\"],\"max_length\":3,\"method\":\"RE\"},{\"property_name\":\"Current_Threshold\",\"data_type\":\"int\",\"min\":\"0\",\"max\":\"65535\",\"method\":\"RE\"}],\"commands\":[{\"command_name\":\"AutoWater_Control_Pump\",\"paras\":[{\"para_name\":\"Motor\",\"data_type\":\"string\",\"enum_list\":[\"ON\",\"OFF\"],\"max_length\":3}]},{\"command_name\":\"AutoWater_Threshold\",\"paras\":[{\"para_name\":\"Threshold\",\"data_type\":\"int\",\"min\":\"0\",\"max\":\"100\",\"step\":0}]}],\"events\":[{\"event_type\":\"AAA\",\"paras\":[{\"para_name\":\"AAA\",\"data_type\":\"int\"}]}]}],\"manufacturer_name\":\"HAMOSProjectY-BearPi-Huhuashizhe\",\"industry\":\"IOT-industry\"}";
            //Log.i("JSONALL",result);
            return result;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }

    /*
     * 创建String转Json，方便CreateDeviceIoTDAneedpost()函数(AddDevice 创建IoTDA设备)，提前数据处理提交JSON参数。
     * 目前JSON我写怕了，而且那个JSON挺短的hh等我Update就完事了，先留这个口。
     */
    public String CreateStringToCreateDeviceIoTDAneedpost() {
        //华为云API调试亲测能通过
        //API调试Demo地址：https://apiexplorer.developer.huaweicloud.com/apiexplorer/debug?historyId=831b792395334d788f6aaa885db9ccf2
        //感谢华为云有这样的功能，可以给初学者，直接上手测试API在自己的账号的情况。Thanks~
        String result = "{\"node_id\":\"HaohanyhHhsz20220723\",\"device_name\":\"HaohanyhHhsz\",\"product_id\":\"HaohanyhHhsz\",\"shadow\":[{\"service_id\":\"AutoWater\",\"desired\":{\"Soil_Moisture\":null,\"Temperature\":null,\"Humidity\":null,\"MotorStatus\":null}}]}";
        return result;
    }

    /*
     * 创建String转Json，方便SearchDeviceIoTDAneedget()函数(ListDevices 查询设备列表)，提前数据处理并判断设备是否创建成功
     * （其实100%啦，只是多此一举一下，安全~）
     */
    public void CreateStringToSearchDeviceIoTDAneedget(String content) {
        S_data_device_id = "";                                  //准备判断的网关数据，这个很重要。
        String data_node_id = "";                             //准备判断的设备标识码，这个很重要。
        String data_product_id = "";                         //准备判断的设备名称，这个很重要。
        S_data_product_name = "";                          //准备判断的产品名称，就是看护花使者设备是否进入我们创建的产品里面了。
        try {
            JSONObject jsonObj = new JSONObject(content);
            JSONArray jsonArray = jsonObj.getJSONArray("devices");
            for (int i = 0; i < jsonArray.length(); i++) {
                //第一个数组数据居多，反正只创建了一台，我们就不要第二个数组的count了，麻烦。
                Object json6 = jsonArray.getJSONObject(0);
                //那么，我们就取出了所有数据了，当然我们很多都不需要，只需要判断重点的几个就行了。
                JSONObject ALLDATA = new JSONObject(String.valueOf(json6));
                S_data_device_id = ALLDATA.getString("device_id");
                Device_info = S_data_device_id;
                //Log.i("data_device_id:", getDevice_info());
                //Log.i("data_device_id:",S_data_device_id);
                data_node_id = ALLDATA.getString("node_id");
                //Log.i("data_node_id:",data_node_id);
                data_product_id = ALLDATA.getString("product_id");
                //Log.i("data_product_id:",data_product_id);
                S_data_product_name = ALLDATA.getString("product_name");
                Product_name = S_data_product_name;
                //Log.i("data_product_name:",S_data_product_name);
                //获取完数据，我们就得判断了。判断成功后，我们就得重置设备密钥了。
                if(HaohanyhOranMeCDNDecideData("https://oranme-cdn.haohanyh.com/HAMOS/ProjectY/Device_info.json", S_data_device_id, data_node_id, data_product_id, S_data_product_name)){
                    ResetSecretneedpost();
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /*
     * Haohanyh_OranMeCDN_ListDevices 浩瀚银河居若科技联动函数，前往浩瀚银河服务器上获取数据，进行比对判断
     * ▍浩瀚银河旗下网站和等服务，均严格遵守《中华人民共和国网络安全法》《深圳经济特区数据条例》《中华人民共和国香港特别行政区维护国家安全法》
     * ▍和中华人民共和国澳门特别行政区《网络安全法》
     * ▍(浩瀚银河旗下网站和等服务不会以任何形式收集敏感信息和大数据分析，并在遵守情况下最大限度公开用户上传数据）
     * 浩瀚银河数据保存地址:https://oranme-cdn.haohanyh.com/HAMOS/ProjectY/Device_info.json
     * 浩瀚银河CDN图形界面地址:https://oranme-cdn.haohanyh.com/files.php?HAMOS/ProjectY#Device_info.json
     * 本函数无任何上传到浩瀚银河的任何操作，仅为JSON数据下载判断行为，可安全使用
     * 浩瀚银河服务器已能做到7x24不间断运行，不会影响大量用户生产环境。
     */
    public boolean HaohanyhOranMeCDNDecideData(String Url, String S_data_device_id, String data_node_id, String data_product_id, String S_data_product_name) throws JSONException {
        String content = "";
        URLConnection urlConnection = null;
        //从浩瀚银河服务器上取到的数据，会保存到这里面，浩瀚银河不会修改文件不会删除文件。
        String If_S_data_device_id = "";
        String If_data_node_id = "";
        String If_data_product_id = "";
        String If_S_data_product_name = "";
        //下面就是一系列猛如虎的操作了
        try {
            urlConnection = new URL(Url).openConnection();
            HttpURLConnection connection = (HttpURLConnection) urlConnection;
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Content-Type","application/json");
            connection.connect();
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
                StringBuilder bs = new StringBuilder();
                String l;
                while ((l = bufferedReader.readLine()) != null) {
                    bs.append(l).append("\n");
                }
                content = bs.toString();
                //Log.i("成功GET:",content);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        //下面就是一系列猛如虎的取数据做判断的操作了
        JSONObject jsonObj = new JSONObject(content);
        If_S_data_device_id = jsonObj.getString("device_id");
        If_data_node_id = jsonObj.getString("node_id");
        If_data_product_id = jsonObj.getString("product_id");
        If_S_data_product_name = jsonObj.getString("product_name");

        if((S_data_device_id.equals(If_S_data_device_id)) && (data_node_id.equals(If_data_node_id)) && (data_product_id.equals(If_data_product_id)) && (S_data_product_name.equals(If_S_data_product_name))){
            Log.v("浩瀚银河:","恭喜您，华为云IoTDA上面有您的设备");
            return true;
        }
        return false;
    }

    /*
     *分水岭，上面都是JSON转String、String转JSON，下面都是华为云API函数。
     * （注意:很多函数之间，存在联动关系，请不要随便删除，避免影响环境，浩瀚银河已经对每一个函数做过苛刻的灰度测试，可以保证生产环境无任何问题）
     */

    /*
     * CreateCommand 下发设备命令
     * 华为云API调试地址:https://apiexplorer.developer.huaweicloud.com/apiexplorer/doc?product=IoTDA&api=CreateCommand
     * 护花使者：采用E53-IA1，MotorStatus
     * 小熊派：（E53-IA1 MotorStatus、LightStatus）（E53-ST1 BeepStatus）（E53_SC1 LightStatus）
     */
    public void ControlSenderneedpost(String json,String project_id,String device_id) {
        RequestBody body = RequestBody.create(MediaType.parse("application/json;charset=utf-8"), json);
        Request request = new Request.Builder()
                .url("https://iotda.cn-north-4.myhuaweicloud.com/v5/iot/" + project_id + "/devices/" + device_id + "/commands")
                .addHeader("X-Auth-Token",HUAWEITOKEN)
                .post(body)
                .build();

        OkHttpClient mOkHttpClient = new OkHttpClient();
        mOkHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("ControlSenderneedpost,Failed",e.getLocalizedMessage() + "，失败");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.i("ControlSenderneedpost,Finished","函数链接: " + Objects.requireNonNull(response.body()).string());
            }
        });
    }

    /*
     * ShowDeviceShadow 查询设备影子数据
     * 华为云API调试地址:https://apiexplorer.developer.huaweicloud.com/apiexplorer/doc?product=IoTDA&api=ShowDeviceShadow
     */
    public String Knowdeviceneedget(String url) {
        String content = "";
        URLConnection urlConnection = null;
        try {
            urlConnection = new URL(url).openConnection();
            HttpURLConnection connection = (HttpURLConnection) urlConnection;
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("X-Auth-Token", HUAWEITOKEN);
            connection.connect();
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
                StringBuilder bs = new StringBuilder();
                String l;
                while ((l = bufferedReader.readLine()) != null) {
                    bs.append(l).append("\n");
                }
                content = bs.toString();
            } else if (responseCode == 401) {
                Log.e("Knowdeviceneedget,Failed","401");
            }
            return content;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return content;
    }

    /*
     * ResetDeviceSecret 重置设备密钥
     * 华为云API调试地址:https://apiexplorer.developer.huaweicloud.com/apiexplorer/doc?product=IoTDA&api=ResetDeviceSecret
     */
    public void ResetSecretneedpost() {
        //随机方法：生成两段以时间戳为随机Random的值（不会一样），然后添加进去，公式：
        Random Rdm1 =new Random(System.currentTimeMillis());
        Random Rdm2 =new Random(System.currentTimeMillis() * System.currentTimeMillis());
        //浩瀚银河提醒您：请不要把secret连同您产品ID暴露出去！
        //以下的所有过程，浩瀚银河只是简单的处理而不是暴露您甚至保存您secret，那会是很愚蠢的行为。
        String String1 = String.valueOf(Rdm2.nextInt());
        String String2 = String.valueOf(Rdm1.nextInt());
        String DeviceSecret = ("Hhsz"+String1+String2+"NB").replaceAll("-","");
        //传给全局变量的同时，转JSON，然后API提交
        Device_secret = DeviceSecret;
        String SecretJSON = "{\"secret\": \""+ DeviceSecret +"\"}";
        //Log.i("设备密钥:",SecretJSON);
        RequestBody body = RequestBody.create(MediaType.parse("application/json"),SecretJSON);
        Request request = new Request.Builder()
                .url("https://iotda.cn-north-4.myhuaweicloud.com/v5/iot/" + getProject_id() + "/devices/"+ S_data_device_id +"/action?action_id=resetSecret")
                .addHeader("X-Auth-Token",HUAWEITOKEN)
                .addHeader("Content-type","application/json")
                .post(body)
                .build();
        OkHttpClient mOkHttpClient = new OkHttpClient();
        mOkHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("ResetSecretneedpost,Failed",e.getLocalizedMessage() + "，失败");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if(response.code() == 200){
                    //反正每次都是200，索性不看Log了
                    //Log.i("OK","code=200,才算成功");
                }else{
                    Log.w("Warn","code=200,才算成功;如果code=400,有可能是JSON问题也有可能是已经创建了请检查");
                    Log.w("Warn","如果使用的是浩瀚银河内置的JSON，请不用担心code=400问题");
                }
            }
        });
    }

    /*
     * ListDevices 查询设备列表
     * 华为云API调试地址:https://apiexplorer.developer.huaweicloud.com/apiexplorer/debug?product=IoTDA&api=ListDevices
     * 请注意，如果您在（CreateProduct 创建产品）和（AddDevice 创建IoTDA设备）两个API中均得到了400错误，如果您使用的是浩瀚银河的JSON数据，请不要担心，这里的结果一定是正确的。
     * 查询地址:https://iotda.cn-north-4.myhuaweicloud.com/v5/iot/{project_id}/devices?product_id=HaohanyhHhsz
     */
    public void SearchDeviceIoTDAneedget() {
        String content = "";
        String result = "";
        URLConnection urlConnection = null;
        String SUrl = "https://iotda.cn-north-4.myhuaweicloud.com/v5/iot/"+ getProject_id() +"/devices?product_id=HaohanyhHhsz";
        try {
            urlConnection = new URL(SUrl).openConnection();
            HttpURLConnection connection = (HttpURLConnection) urlConnection;
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Content-Type","application/json;charset=UTF-8");
            connection.setRequestProperty("X-Auth-Token", HUAWEITOKEN);
            connection.connect();
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
                StringBuilder bs = new StringBuilder();
                String l;
                while ((l = bufferedReader.readLine()) != null) {
                    bs.append(l).append("\n");
                }
                content = bs.toString();
                CreateStringToSearchDeviceIoTDAneedget(content);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
     * AddDevice 创建IoTDA设备
     * 华为云API调试地址:https://apiexplorer.developer.huaweicloud.com/apiexplorer/doc?product=IoTDA&api=AddDevice
     */
    public void CreateDeviceIoTDAneedpost() {
        //Project_id = Knowprojectneedget("https://iam.cn-north-4.myhuaweicloud.com/v3/projects?domain_id=");
        String jsonwenben = CreateStringToCreateDeviceIoTDAneedpost();
        RequestBody body = RequestBody.create(MediaType.parse("application/json;charset=utf-8"), jsonwenben);
        Request request = new Request.Builder()
                .url("https://iotda.cn-north-4.myhuaweicloud.com/v5/iot/"+ getProject_id() +"/devices")
                .addHeader("X-Auth-Token",HUAWEITOKEN)
                .addHeader("Content-type","application/json")
                .post(body)
                .build();
        OkHttpClient mOkHttpClient = new OkHttpClient();
        mOkHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("CreateDeviceIoTDAneedpost,Failed",e.getLocalizedMessage() + "，失败");
            }

            @Override
            public void onResponse(Call call, Response response) {
                if(response.code() == 201){
                    Log.i("OK","code=201,才算成功");
                }else{
                    Log.w("Warn","code=201,才算成功;如果code=400,有可能是JSON问题也有可能是已经创建了请检查");
                    Log.w("Warn","如果使用的是浩瀚银河内置的JSON，请不用担心code=400问题，说明已经创建在华为云IoTDA里面了");
                }
            }
        });

    }

    /*
     * CreateProduct 创建产品
     * 华为云API调试地址:https://apiexplorer.developer.huaweicloud.com/apiexplorer/debug?product=IoTDA&api=CreateProduct
     */
    public void CreateProductinIoTDAneedpost() {
        //Project_id = Knowprojectneedget("https://iam.cn-north-4.myhuaweicloud.com/v3/projects?domain_id=");
        String jsonwenben = CreateStringToCreateProductinIoTDAneedpost();
        RequestBody body = RequestBody.create(MediaType.parse("application/json;charset=utf-8"), jsonwenben);
        Request request = new Request.Builder()
                .url("https://iotda.cn-north-4.myhuaweicloud.com/v5/iot/" + getProject_id() + "/products")
                .addHeader("X-Auth-Token",HUAWEITOKEN)
                .post(body)
                .build();
        OkHttpClient mOkHttpClient = new OkHttpClient();
        mOkHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("CreateProductinIoTDAneedpost,Failed",e.getLocalizedMessage() + "，失败");
            }

            @Override
            public void onResponse(Call call, Response response) {
                //Log.e("Debug", String.valueOf(response.headers()));
                //Log.e("Debug", response.toString());
                if(response.code() == 201){
                    Log.i("OK","code=201,才算成功");
                }else{
                    Log.w("Warn","code=201,才算成功;如果code=400,有可能是JSON问题也有可能是已经创建了请检查");
                    Log.w("Warn","如果使用的是浩瀚银河内置的JSON，请不用担心code=400问题，说明已经创建在华为云IoTDA里面了");
                }
            }
        });
    }

    /*
     * KeystoneListProjects 查询指定条件下的项目列表
     * 华为云API调试地址:https://apiexplorer.developer.huaweicloud.com/apiexplorer/doc?product=IAM&api=KeystoneListProjects
     * 在APP不知情使用者IAM账号下的“华东-北京4区”项目ID，这个API就显得尤为重要，特别是为接下来的新版本做自动化准备，这个函数必须要调用。
     */
    public String Knowprojectneedget(String url) {
        String content = "";
        String project = "";
        URLConnection urlConnection = null;
        //API地址：https://iam.cn-north-4.myhuaweicloud.com/v3/projects?domain_id=，domain_id会自动填上去。
        String SUrl = url + Domain_id;
        try {
            urlConnection = new URL(SUrl).openConnection();
            HttpURLConnection connection = (HttpURLConnection) urlConnection;
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Content-Type","application/json;charset=UTF-8");
            connection.setRequestProperty("X-Auth-Token", HUAWEITOKEN);
            connection.connect();
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
                StringBuilder bs = new StringBuilder();
                String l;
                while ((l = bufferedReader.readLine()) != null) {
                    bs.append(l).append("\n");
                }
                content = bs.toString();
                project = CreateStringToKnowprojectneedget(content);
                Project_id = project;
            }
            return project;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return project;
    }

    /*
     * KeystoneCreateUserTokenByPassword 获取IAM用户Token(使用密码)
     * 华为云API调试地址：https://apiexplorer.developer.huaweicloud.com/apiexplorer/doc?product=IAM&api=KeystoneCreateUserTokenByPassword
     */
    public void Knowtokenneedpost() {
        String jsonwenben = CreateJsonToKnowtokenneedpost();
        final String[] Domain_id_Temp = {""};
        RequestBody body = RequestBody.create(MediaType.parse("application/json;charset=utf-8"), jsonwenben);
        Request request = new Request.Builder()
                .url("https://iam.cn-north-4.myhuaweicloud.com/v3/auth/tokens")
                .post(body)
                .build();
        OkHttpClient mOkHttpClient = new OkHttpClient();
        mOkHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("Knowtokenneedpost,Failed",e.getLocalizedMessage() + "，失败");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                //获取完token，还有domain_id，慢慢来就完事了。
                HUAWEITOKEN = response.header("x-subject-token");
                Domain_id_Temp[0] = Objects.requireNonNull(response.body()).string();
                CreateJsonToKnowDomainidneedpose(Domain_id_Temp[0]);
            }
        });
    }
}
