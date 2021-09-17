package com.hy.mipaycard.Service;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;

import androidx.annotation.RequiresApi;

import static com.hy.mipaycard.shortcuts.OpenMiPayActivity.openMiPay;

import java.lang.reflect.Method;

@RequiresApi(api = Build.VERSION_CODES.N)
public class MiPayTitleService extends TileService {
    //添加磁贴时调用
    @Override
    public void onTileAdded() {
        super.onTileAdded();
    }

    //移除磁贴时调用
    @Override
    public void onTileRemoved() {
        super.onTileRemoved();
    }

    //点击事件
    @Override
    public void onClick() {
        super.onClick();
        collapseStatusBar(this);
        openMiPay(this);
    }

    //只有添加后才调用
    //通知栏下拉
    @Override
    public void onStartListening () {
        super.onStartListening();
        boolean status = getSharedPreferences("set", Context.MODE_PRIVATE).getBoolean("TileStatus",false);
        setQuickSettingColor(status);
    }

    //通知栏关闭
    @Override
    public void onStopListening () {
        super.onStopListening();
    }

    //设置磁贴颜色
    @RequiresApi(api = Build.VERSION_CODES.N)
    public void setQuickSettingColor(boolean status){
        if(status) {
            //更改成非活跃状态(灰色)
            getQsTile().setState(Tile.STATE_ACTIVE);
        } else {
            //更改成活跃状态(白色)
            getQsTile().setState(Tile.STATE_INACTIVE);
        }
        getQsTile().updateTile();
    }


    public static void collapseStatusBar(Context context){
        try{
            @SuppressLint("WrongConstant") Object statusBarManager = context.getSystemService("statusbar");
            Method collapse;

            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN){
                assert statusBarManager != null;
                collapse = statusBarManager.getClass().getMethod("collapse");
            } else {
                assert statusBarManager != null;
                collapse = statusBarManager.getClass().getMethod("collapsePanels");
            }
            collapse.invoke(statusBarManager);
        } catch (Exception localException) {
            localException.printStackTrace();
        }
    }
}
