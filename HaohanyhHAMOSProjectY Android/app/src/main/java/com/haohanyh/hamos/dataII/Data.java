/* 受Haohanyh Computer Software Products Open Source LICENSE保护 https://git.haohanyh.top:3001/Haohanyh/LICENSE */
package com.haohanyh.hamos.dataII;

public class Data {
    //Data类，为2.1版本助力，让关键信息填写更简单而不杂乱。

    //Wi-Fi名称和Wi-Fi密码
    private String WPA_address = "";
    private String WPA_pwd = "";

    //下面老三样了，建议是不要动
    protected Data() { }
    public static Data GetData() { return data.shuju; }
    protected static class data { private static final Data shuju = new Data(); }

    //2.1更新，重写构造函数
    public String getWPA_pwd() { return WPA_pwd; }
    public String getWPA_address() { return WPA_address; }
    public void setWPA_pwd(String Data) { WPA_pwd = Data; }
    public void setWPA_address(String Data) { WPA_address = Data; }
}
