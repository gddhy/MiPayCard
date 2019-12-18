package com.hy.mipaycard;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Toast;

import com.by_syk.lib.uri.UriAnalyser;
import com.hy.mipaycard.shortcuts.CardDefaultActivity;

import java.io.File;

import static com.hy.mipaycard.MainUtils.getCard;
import static com.hy.mipaycard.MainUtils.getTsm;
import static com.hy.mipaycard.MainUtils.initOther;
import static com.hy.mipaycard.MainUtils.showAboutDialog;
import static com.hy.mipaycard.shortcuts.OpenMiPayActivity.openMiPay;

public class MainDialogActivity extends AppCompatActivity {

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        init();
        pref = PreferenceManager.getDefaultSharedPreferences(MainDialogActivity.this);
        initOther(MainDialogActivity.this);
        getTsm(MainDialogActivity.this);
    }

    private void init(){
        final String[] list = {"招行初音卡","天依柠檬卡","选择图片","恢复默认","打开MiPay","提取卡面","卡面列表","关于"};
        new AlertDialog.Builder(MainDialogActivity.this)
                .setTitle("MiPay卡面工具")
                .setItems(list,new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog,int i){
                        if (i == list.length-1) i = -1;
                        switch(i){
                            case 0:
                            case 1:
                                setCard(i==0);
                                break;
                            case 2:
                                if (ContextCompat.checkSelfPermission(MainDialogActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                                    ActivityCompat.requestPermissions(MainDialogActivity.this, new String[]{ Manifest.permission. WRITE_EXTERNAL_STORAGE }, 1);
                                } else {
                                    openAlbum();
                                }
                                break;
                            case 3:
                                init();
                                startActivity(new Intent(MainDialogActivity.this, CardDefaultActivity.class));
                                break;
                            case 4:
                                init();
                                openMiPay(MainDialogActivity.this);
                                break;
                            case 5:
                                if (ContextCompat.checkSelfPermission(MainDialogActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                                    ActivityCompat.requestPermissions(MainDialogActivity.this, new String[]{ Manifest.permission. WRITE_EXTERNAL_STORAGE }, 2);
                                } else {
                                    init();
                                    getCard(MainDialogActivity.this);
                                }
                                break;
                            case 6:
                                if (ContextCompat.checkSelfPermission(MainDialogActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                                    ActivityCompat.requestPermissions(MainDialogActivity.this, new String[]{ Manifest.permission. WRITE_EXTERNAL_STORAGE }, 3);
                                } else {
                                    userCardList();
                                }
                                break;
                            case -1:
                                init();
                                showAboutDialog(MainDialogActivity.this);
                                break;
                            default:
                                init();
                        }
                    }
                })
                .setCancelable(false)
                .setPositiveButton("退出",new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog,int i){
                        finish();
                    }
                })
                .show();

    }

    private void openAlbum(){
        Intent intent = new Intent("android.intent.action.GET_CONTENT");
        intent.setType("image/*");
        startActivityForResult(intent,0);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openAlbum();
                } else {
                    init();
                    Toast.makeText(MainDialogActivity.this, "缺少必要权限", Toast.LENGTH_SHORT).show();
                }
                break;
            case 2:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    init();
                    getCard(MainDialogActivity.this);
                } else {
                    init();
                    Toast.makeText(MainDialogActivity.this, "缺少必要权限", Toast.LENGTH_SHORT).show();
                }
                break;
            case 3:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    userCardList();
                } else {
                    init();
                    Toast.makeText(MainDialogActivity.this, "缺少必要权限", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                init();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK ) {
            switch (requestCode) {
                case 0:
                    init();
                    Uri uri = data.getData();
                    Intent i = new Intent(MainDialogActivity.this, BitmapCropActivity.class);
                    i.putExtra(Config.open_Crop, UriAnalyser.getRealPath(this, uri));
                    startActivity(i);
                    break;
                default:
                    init();
            }
        } else {
            init();
        }
    }

    private void setCard(final boolean isMiku){
        setCard(new File(MainDialogActivity.this.getFilesDir(),isMiku?"miku.png":"luotianyi.png").getPath());
    }

    private void setCard(String path){
        setCard(path,false);
    }

    private void setCard(String path, final boolean isAuto){
        init();
        Intent intent = new Intent(MainDialogActivity.this, SetCardActivity.class);
        intent.putExtra(Config.file_Path, path);
        intent.putExtra(Config.is_Auto, isAuto);
        startActivity(intent);
    }

    private void userCardList(){
        final File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),"MiPayCard/List");
        if(!file.exists()){
            file.mkdirs();
        }
        final String[] userList = file.list();
        new AlertDialog.Builder(MainDialogActivity.this)
                .setTitle("自定义卡面列表")
                .setItems(userList, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        setCard(new File(file,userList[i]).getPath());
                    }
                })
                .setCancelable(false)
                .setPositiveButton("返回", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        init();
                    }
                })
                .show();
    }
}
