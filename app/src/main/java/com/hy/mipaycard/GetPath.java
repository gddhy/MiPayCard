package com.hy.mipaycard;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Toast;

import java.io.File;

public class GetPath extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.Q){
            //TODO
            Toast.makeText(this,"该功能暂不支持Q及以上系统",Toast.LENGTH_LONG).show();
            finish();
        } else {
            if (ContextCompat.checkSelfPermission(GetPath.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(GetPath.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            } else {
                get();
            }
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                get();
            } else {
                Toast.makeText(GetPath.this, "缺少必要权限", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
    private void get(){
        Bundle bundle = this.getIntent().getExtras();
        if (bundle != null){
            String value = bundle.getString(Config.Get_path_key);
            File f = new File(value);
            if(f.exists()){
                Intent i = new Intent(GetPath.this, BitmapCropActivity.class);
                i.putExtra(Config.open_Crop, value);
                startActivity(i);
            } else {
                Toast.makeText(GetPath.this, "文件不存在", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(GetPath.this, "未传入数据", Toast.LENGTH_SHORT).show();
        }
        finish();
    }
}
