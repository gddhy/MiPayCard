package com.hy.mipaycard.shortcuts;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.Toast;

import static com.hy.mipaycard.Config.pay_pic;
import static com.hy.mipaycard.Utils.cmdUtil.runRootShell;

public class SetMenuPermissionActivity extends AppCompatActivity {
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    public static String onlyRead = "chmod 2500 "+pay_pic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        pref = PreferenceManager.getDefaultSharedPreferences(SetMenuPermissionActivity.this);
        final CheckBox checkBox = new CheckBox(SetMenuPermissionActivity.this);
        checkBox.setChecked(pref.getBoolean("setAuto",false));
        checkBox.setText("临时恢复默认");
        new AlertDialog.Builder(SetMenuPermissionActivity.this)
                //.setCancelable(false)
                .setTitle("设置权限")
                .setMessage(".将卡面目录权限设为只读，可以防止卡面被系统替换为默认卡面\n.此操作不影响本app修改卡面，需要在MiPay添加卡片时请先恢复默认权限，以保证卡面可以正常添加"
                        +"\n.临时恢复默认会在下次更换卡面时自动设为只读权限")
                .setView(checkBox)
                .setPositiveButton("设为只读", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        runRootShell(new String[]{onlyRead});
                        Toast.makeText(SetMenuPermissionActivity.this,"已设为只读权限",Toast.LENGTH_LONG).show();
                        finish();
                    }
                })
                .setNegativeButton("恢复默认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        runRootShell(new String[]{"chmod 2700 "+pay_pic});
                        boolean b = checkBox.isChecked();
                        editor = pref.edit();
                        editor.putBoolean("setAuto",b);
                        editor.apply();
                        Toast.makeText(SetMenuPermissionActivity.this,"已"+(b?"临时":"")+"恢复默认权限",Toast.LENGTH_LONG).show();
                        finish();
                    }
                })
                .setNeutralButton("返回", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                })
                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        finish();
                    }
                })
                .show();
    }
}
