package com.hy.mipaycard;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Environment;

import java.io.File;

public class Config {
    public static String file_Path = "filePath";
    public static String is_Auto = "isAuto";
    public static String open_Crop = "openCrop";
    public static String Get_path_key = "PATH_KEY";
    public static File fileWork(Context context){
        if(Build.VERSION.SDK_INT>=AndroidQ_Api){
            return new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),"MiPayCard/List");
        } else {
            return new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "MiPayCard/List");
        }
    }
    @SuppressLint("SdCardPath")
    public static String pay_pic = "/data/data/com.miui.tsmclient/cache/image_manager_disk_cache";
    @SuppressLint("SdCardPath")
    public static String mi_wallet = "/data/data/com.mipay.wallet/cache/image_manager_disk_cache";
    public static File getTempFile(Context context){
        return new File(getExternalCache(context), "temp_MiPayCard.png");
    }

    public static File getExternalCache(Context context){
        if(Build.VERSION.SDK_INT>=30){
            File tmp = new File(context.getExternalFilesDir(null),"cache");
            if(!tmp.exists()){
                tmp.mkdir();
            }
            return tmp;
        } else {
            return context.getExternalCacheDir();
        }
    }

    public static String localAction = "com.hy.mipaycard.ref_ui";
    public static String localAction_online = "com.hy.mipaycard.ref_flag_online";

    public static String git_url = "https://cdn.jsdelivr.net/gh/gddhy/MiPayCard/";
    /**
     * 请求在线卡面链接
     * https://raw.githubusercontent.com/gddhy/MiPayCard/master/
     * https://gddhy.github.io/MiPayCard/
     * https://cdn.jsdelivr.net/gh/gddhy/MiPayCard/
     *
     * 国内网络原因，暂时使用最后一个
     */

    public static int defaultSet = 1;

    public static int AndroidQ_Api = Build.VERSION_CODES.Q;

    public static String getOnlineGitLink(boolean isUseCDN){
        final String CDNLink = "https://cdn.jsdelivr.net/gh/gddhy/MiPayCard-onlineCard/";
        final String link = "https://gddhy.github.io/MiPayCard-onlineCard/";
        return isUseCDN ? CDNLink : link;
    }
}
