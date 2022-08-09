package com.hy.mipaycard;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Environment;

import java.io.File;

import static com.hy.mipaycard.BaseApplication.getContext;

public class Config {
    public static String file_Path = "filePath";
    public static String is_Auto = "isAuto";
    public static String open_Crop = "openCrop";
    public static String Get_path_key = "PATH_KEY";
    public static String CDN_Domain = "fastly.jsdelivr.net";

    public static File fileWork(Context context){
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.Q){
            return new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),"MiPayCard/List");
        } else {
            return new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "MiPayCard/List");
        }
    }
    @SuppressLint("SdCardPath")
    public static final String pay_pic = "/data/data/com.miui.tsmclient/cache/image_manager_disk_cache";
    @SuppressLint("SdCardPath")
    public static final String mi_wallet = "/data/data/com.mipay.wallet/cache/image_manager_disk_cache";
    public static final String OnePlusPath = "/storage/self/primary/Android/data/com.finshell.wallet/files/Finshell/Wallet/DownloadPics";

    public static File getTempFile(){
        return new File(getExternalCache(), "temp_MiPayCard.png");
    }

    public static File getExternalCache(){
        Context context = getContext();
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.R){
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

    //域名
    public static String WEBSITE = "https://gddhy.net/";

    //优先从mi-pay-card-api请求数据，如果失败该用jsdelivr，轮换使用
    public static String getApiLink(boolean isOnlineLink,boolean isUseJsDelivr){
        String api = "https://mi-pay-card-api.gddhy.net";
        String cdn_jsdelivr = "https://" + CDN_Domain + "/gh/gddhy/MiPayCard@master/";
        if(isUseJsDelivr){
            if(isOnlineLink){
                return cdn_jsdelivr + "online_card.json";
            } else {
                return cdn_jsdelivr + "card_list.json";
            }
        } else{
            if(isOnlineLink){
                return api + "?online=true";
            } else {
                return api + "?list=true";
            }
        }
    }

    public static int defaultSet = 1;
    public static boolean default_PKILL = true;

    public static String getOnlineGitLink(boolean isUseCDN){
        final String CDNLink = "https://" + CDN_Domain + "/gh/gddhy/MiPayCard-onlineCard/";
        final String link = WEBSITE+"MiPayCard-onlineCard/";
        return isUseCDN ? CDNLink : link;
    }
}
