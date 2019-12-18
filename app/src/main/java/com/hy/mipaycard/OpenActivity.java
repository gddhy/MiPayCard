package com.hy.mipaycard;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Toast;

import com.by_syk.lib.uri.UriAnalyser;

public class OpenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        if (ContextCompat.checkSelfPermission(OpenActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(OpenActivity.this, new String[]{ Manifest.permission. WRITE_EXTERNAL_STORAGE }, 1);
        } else {
            getFile();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getFile();
            } else {
                Toast.makeText(OpenActivity.this, "缺少必要权限", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private void getFile(){
        String str = null;
        Intent intent = getIntent();
        if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            Uri uri = intent.getData();
            str = UriAnalyser.getRealPath(OpenActivity.this,uri);
        }
        if (str==null){
            Toast.makeText(OpenActivity.this,"文件读取失败",Toast.LENGTH_LONG).show();
        } else {
            Intent i = new Intent(OpenActivity.this, BitmapCropActivity.class);
            i.putExtra(Config.open_Crop, str);
            startActivity(i);
        }
        finish();
    }
}
