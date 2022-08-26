/* 受Haohanyh Computer Software Products Open Source LICENSE保护 https://git.haohanyh.top:3001/Haohanyh/LICENSE */
package com.haohanyh.hamos.dataI;

public class Data {
    //华为云IAM账号
    private static String JsonDomainName = "";
    private static String JsonName = "";
    private static String JsonPassword = "";
    //重构，让其他类获取Data类的数据
    public String getJsonDomainName() { return JsonDomainName; }
    public String getJsonName() { return JsonName; }
    public String getJsonPassword() { return JsonPassword; }
    public void setJsonDomainName(String Data) { JsonDomainName = Data; }
    public void setJsonName(String Data) { JsonName = Data; }
    public void setJsonPassword(String Data) { JsonPassword = Data; }
    //下面老三样了，建议是不要动
    protected Data() { }
    public static Data GetData() { return data.shuju; }
    protected static class data { private static final Data shuju = new Data(); }
}