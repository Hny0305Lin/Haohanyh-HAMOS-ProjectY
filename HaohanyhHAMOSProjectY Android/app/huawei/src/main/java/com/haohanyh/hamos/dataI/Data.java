package com.haohanyh.hamos.dataI;

public class Data {
    //华为云IAM账号
    private static final String JsonDomainName = "";
    private static final String JsonName = "";
    private static final String JsonPassword = "";
    //重构，让其他类获取Data类的数据
    public String getJsonDomainName() { return JsonDomainName; }
    public String getJsonName() { return JsonName; }
    public String getJsonPassword() { return JsonPassword; }
    //下面老三样了，建议是不要动
    protected Data() { }
    public static Data GetData() { return data.shuju; }
    protected static class data { private static final Data shuju = new Data(); }
}