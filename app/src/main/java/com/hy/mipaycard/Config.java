package com.hy.mipaycard;

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
        if(Build.VERSION.SDK_INT>=debug_Api){
            return new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),"MiPayCard/List");
        } else {
            return new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "MiPayCard/List");
        }
    }
    public static String pay_pic = "/data/data/com.miui.tsmclient/cache/image_manager_disk_cache";
    public static String mi_wallet = "/data/data/com.mipay.wallet/cache/image_manager_disk_cache";
    public static File getTempFile(Context context){
        return new File(context.getExternalCacheDir(), "temp_MiPayCard.png");
    }

    public static String localAction = "com.hy.mipaycard.ref_ui";
    public static String localAction_online = "com.hy.mipaycard.ref_flag_online";

    public static String git_url = "https://gddhy.github.io/MiPayCard/"; //"https://raw.githubusercontent.com/gddhy/MiPayCard/master/"
    public static int defaultSet = 1;

    public static int debug_Api = 29;
}
