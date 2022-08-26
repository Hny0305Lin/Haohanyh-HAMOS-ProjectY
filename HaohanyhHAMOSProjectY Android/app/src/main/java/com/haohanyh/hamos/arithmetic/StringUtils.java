/* 受Haohanyh Computer Software Products Open Source LICENSE保护 https://git.haohanyh.top:3001/Haohanyh/LICENSE */
package com.haohanyh.hamos.arithmetic;

public class StringUtils {//字符串处理算法

    //下面老三样了，建议是不要动
    protected StringUtils() { }
    public static StringUtils GetData() { return Stringshuju.Stringshuju; }
    protected static class Stringshuju { private static final StringUtils Stringshuju = new StringUtils(); }

    //2.2判断华为云账号所使用
    private final String HuaweiSmallLetter = "abcdefghijklmnopqrstuvwxyz";
    private final String NormalCharacter = "`~!@#$%^&*()-_=+[{}]|\\:;\"',<.>/?";
    public String getHuaweiSmallLetter() { return HuaweiSmallLetter; }
    public String getNormalCharacter() { return NormalCharacter; }

}
