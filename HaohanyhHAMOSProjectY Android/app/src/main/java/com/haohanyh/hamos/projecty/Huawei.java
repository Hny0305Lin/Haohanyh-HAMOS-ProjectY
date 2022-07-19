/* 受Haohanyh Computer Software Products Open Source LICENSE保护 https://git.haohanyh.top:3001/Haohanyh/LICENSE */
package com.haohanyh.hamos.projecty;

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

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Huawei {
    //获取得到的HUAWEI华为云Token，不许填写！！！
    protected String HUAWEITOKEN = "";
    //需要添加的IAM账号名、账号密码。想要获取IAM信息？点它即可→https://support.huaweicloud.com/api-iam/iam_17_0002.html
    protected String JsonDomainName = "";
    protected String JsonName = "";
    protected String JsonPassword = "";
    //下面老三样，不要动！！！
    protected Huawei() { }
    protected static Huawei getHuawei() { return huawei.network; }
    protected static class huawei { private static final Huawei network = new Huawei(); }

    /*
     * 灰度测试，创建JSON转String，方便post函数进行使用和理解。
     */
    protected String CreateJsonToPost() {
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
     * ShowDeviceShadow 查询设备影子数据
     * 华为云API调试地址:https://apiexplorer.developer.huaweicloud.com/apiexplorer/doc?product=IoTDA&api=ShowDeviceShadow
     */
    protected String get(String url) {
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
                System.out.println("浩瀚银河Get函数灰度测试:failed");
            }
            return content;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return content;
    }

    /*
     * KeystoneCreateUserTokenByPassword 获取IAM用户Token(使用密码)
     * 华为云API调试地址：https://apiexplorer.developer.huaweicloud.com/apiexplorer/doc?product=IAM&api=KeystoneCreateUserTokenByPassword
     */
    protected void post() {
        String jsonwenben = CreateJsonToPost();
        Log.w("浩瀚银河灰度测试创建JSON操作",CreateJsonToPost());
        RequestBody body = RequestBody.create(MediaType.parse("application/json;charset=utf-8"), jsonwenben);
        Request request = new Request.Builder()
                .url("https://iam.cn-north-4.myhuaweicloud.com/v3/auth/tokens")
                .post(body)
                .build();
        OkHttpClient mOkHttpClient = new OkHttpClient();
        mOkHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                System.out.println("浩瀚银河post函数情况: " + e.getLocalizedMessage() + "，链接失败");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                System.out.println("浩瀚银河post函数链接: " + response.body().string());
                System.out.println("浩瀚银河post函数头部读取: " + response.header("x-subject-token"));
                HUAWEITOKEN = response.header("x-subject-token");
            }
        });
    }
}
