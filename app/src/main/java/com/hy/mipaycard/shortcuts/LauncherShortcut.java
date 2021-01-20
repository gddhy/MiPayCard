package com.hy.mipaycard.shortcuts;

import android.annotation.SuppressLint;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Icon;
import android.os.Binder;
import android.os.Build;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.hy.mipaycard.MainUtils;
import com.hy.mipaycard.R;
import com.hy.mipaycard.online_card.OnlineCardActivity;

import java.lang.reflect.Method;

public class LauncherShortcut {

    public static void addMiPay(Context context){
        showToast(context);
        createLauncherShortcut(context,OpenMiPayActivity.class,"openMiPay","MiPay", BitmapFactory.decodeResource(context.getResources(), R.drawable.mi_pay));
    }

    public static void addOnlineCard(Context context){
        showToast(context);
        createLauncherShortcut(context, OnlineCardActivity.class,"openOnlineCard","在线卡面",BitmapFactory.decodeResource(context.getResources(), R.drawable.online));
    }

    private static void showToast(final Context context){
        if(Build.VERSION.SDK_INT<Build.VERSION_CODES.N){
            Toast.makeText(context,"当前版本系统可能不支持此功能",Toast.LENGTH_LONG).show();
        }
        if(getPropInfo().length()!=0){
            boolean b = canInstallShortcut(context);
            if(!b){
                new AlertDialog.Builder(context)
                        .setTitle("缺少必要权限（MIUI）")
                        .setMessage("程序当前缺少添加桌面快捷方式权限，请前往权限管理授予权限")
                        .setPositiveButton("应用管理", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                MainUtils.toSelfSetting(context);
                            }
                        })
                        .show();
            } else {
                Toast.makeText(context,"已尝试添加，请前往桌面查看",Toast.LENGTH_LONG).show();
            }
        }
    }
    public static boolean canInstallShortcut(Context context) {
        AppOpsManager ops = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
        try {
            int op = 10017; // >= 23
            // ops.checkOpNoThrow(op, uid, packageName)
            Method method = ops.getClass().getMethod("checkOpNoThrow",
                    int.class, int.class, String.class);
            Integer result = (Integer) method.invoke(ops, op, Binder.getCallingUid(), context.getPackageName());
            return result == AppOpsManager.MODE_ALLOWED;
        } catch (Exception e) {
            //Log.e(TAG, "not support", e);
        }
        return false;
    }


        private static String getPropInfo(){
        String info = "";
        try {
            @SuppressLint("PrivateApi") Class<?> c =Class.forName("android.os.SystemProperties");
            Method get =c.getMethod("get", String.class);
            info = (String)get.invoke(c, "ro.miui.ui.version.name");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return info;
    }


    //https://blog.csdn.net/qq_35080853/article/details/103767422
    private static void createLauncherShortcut(Context context, Class<?> cls, String sellerId, String name, Bitmap icon){
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            ShortcutManager shortcutManager = (ShortcutManager) context.getSystemService(Context.SHORTCUT_SERVICE);
            if (shortcutManager != null && shortcutManager.isRequestPinShortcutSupported()) {
                Intent intent = new Intent();
                intent.setClass(context, cls);
                intent.setAction(Intent.ACTION_CREATE_SHORTCUT);
                intent.putExtra("sellerId", sellerId);
                intent.putExtra("duplicate", false);
                intent.addCategory(Intent.CATEGORY_LAUNCHER);
                ShortcutInfo.Builder builder = new ShortcutInfo.Builder(context, sellerId)
                        .setShortLabel(name)
                        .setIcon(Icon.createWithBitmap(icon))
                        .setIntent(intent);
                shortcutManager.requestPinShortcut(builder.build(), null);
            }
        } else {
            Intent intent = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");//ACTION_ADD_SHORTCUT);
            // 是否允许重复创建
            intent.putExtra("duplicate", false);
            // 快捷方式的标题
            intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, name);
            // 快捷方式的图标
            intent.putExtra(Intent.EXTRA_SHORTCUT_ICON, icon);
            Intent launcherIntent = new Intent();
            launcherIntent.setClass(context, cls);
            launcherIntent.putExtra("sellerId", sellerId);
            launcherIntent.setAction(Intent.ACTION_CREATE_SHORTCUT);
            launcherIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            // 快捷方式的动作
            intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, launcherIntent);
            context.sendBroadcast(intent);
        }
    }
}
