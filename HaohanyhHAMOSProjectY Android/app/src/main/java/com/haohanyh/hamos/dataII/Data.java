package com.haohanyh.hamos.dataII;

public class Data {
    //Data类，为2.1版本助力，让关键信息填写更简单而不杂乱。

    //Wi-Fi名称和Wi-Fi密码
    private final String WPA_address = "";
    private final String WPA_pwd = "";

    //重构，让其他类获取Data类的数据
    public String getWPA_address() { return WPA_address; }
    public String getWPA_pwd() { return WPA_pwd; }

    //下面老三样了，建议是不要动
    protected Data() { }
    public static Data GetData() { return data.shuju; }
    protected static class data { private static final Data shuju = new Data(); }
}