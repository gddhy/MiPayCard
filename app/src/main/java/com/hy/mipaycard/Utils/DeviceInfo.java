package com.hy.mipaycard.Utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;

import java.lang.reflect.Method;

public class DeviceInfo {
    //应酷安App收集个人隐私信息要求，不再收集设备基础信息

    public static String getBaseInfo(Context context){
        return  "设备信息：\n"+getPropInfo()+getMiWalletInfo(context)+getMiPayInfo(context);
    }

    private static PackageInfo getPackageInfo(Context context, String packageName){
        PackageInfo packageInfo = null;
        PackageManager packageManager = context.getPackageManager();
        try {
            packageInfo = packageManager.getPackageInfo(packageName,PackageManager.GET_CONFIGURATIONS);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return packageInfo;
    }

    private static String getMiWalletInfo(Context context){
        PackageInfo packageInfo = getPackageInfo(context,"com.mipay.wallet");
        String verName = packageInfo.versionName;
        int verCode = packageInfo.versionCode;
        return "小米钱包版本: "+verName+"("+verCode+")\n";
    }

    private static String getMiPayInfo(Context context){
        PackageInfo packageInfo = getPackageInfo(context,"com.miui.tsmclient");
        String verName = packageInfo.versionName;
        int verCode = packageInfo.versionCode;
        return "小米智能卡版本: "+verName+"("+verCode+")\n";
    }

    //通过反射获取build.prop
    private static String getPropInfo(){
        String info = null;
        String verName = null;
        try {
            @SuppressLint("PrivateApi") Class<?> c =Class.forName("android.os.SystemProperties");
            Method get =c.getMethod("get", String.class);
            verName = (String) get.invoke(c,"ro.miui.ui.version.name");
            info = (String)get.invoke(c, "ro.build.version.incremental");

        } catch (Exception e) {
            e.printStackTrace();
        }
        return "MIUI版本: "+verName+"("+info+")_"+Build.VERSION.RELEASE+"\n";
    }

}
