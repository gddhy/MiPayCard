package com.hy.mipaycard;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import com.by_syk.lib.uri.UriAnalyser;

import java.io.File;

import static com.hy.mipaycard.Config.debug_Api;

public class ShareActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        if(Build.VERSION.SDK_INT>=debug_Api){
            //TODO
            //Toast.makeText(this,"未适配当前安卓版本",Toast.LENGTH_LONG).show();
            Intent intent = getIntent();
            if (Intent.ACTION_SEND.equals(intent.getAction())) {
                Bundle bundle = intent.getExtras();
                if (bundle == null) {
                    Toast.makeText(ShareActivity.this, "文件读取失败", Toast.LENGTH_SHORT).show();
                } else {
                    Uri uri = (Uri) bundle.get(Intent.EXTRA_STREAM);
                    if (uri != null) {
                        String u =uri.toString();
                        Log.d("URI: ",""+u);
                        try {
                            if (u.contains("file://")) {
                                Toast.makeText(ShareActivity.this, "文件读取失败", Toast.LENGTH_SHORT).show();
                            } else {
                                File f = MainActivity.saveFileFromSAF(this, uri);
                                if (f != null) {
                                    Intent i = new Intent(ShareActivity.this, BitmapCropActivity.class);
                                    i.putExtra(Config.open_Crop, f.getPath());
                                    startActivity(i);
                                } else {
                                    Toast.makeText(ShareActivity.this, "文件读取失败", Toast.LENGTH_SHORT).show();
                                }
                            }
                        } catch (Exception e){
                            e.printStackTrace();
                            Toast.makeText(ShareActivity.this, "文件读取失败", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(ShareActivity.this, "文件读取失败", Toast.LENGTH_SHORT).show();
                    }

                }
            }
            finish();
        } else {
            if (ContextCompat.checkSelfPermission(ShareActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(ShareActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            } else {
                getFile();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getFile();
            } else {
                Toast.makeText(ShareActivity.this, "缺少必要权限", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private void getFile() {
        Intent intent = getIntent();
        if (Intent.ACTION_SEND.equals(intent.getAction())) {
            Bundle bundle = intent.getExtras();
            if (bundle == null) {
                Toast.makeText(ShareActivity.this, "文件读取失败", Toast.LENGTH_SHORT).show();
            } else {
                Uri uri = (Uri) bundle.get(Intent.EXTRA_STREAM);
                if (uri !=null) {
                    Intent i = new Intent(ShareActivity.this, BitmapCropActivity.class);
                    i.putExtra(Config.open_Crop, UriAnalyser.getRealPath(this, uri));
                    startActivity(i);
                } else {
                    Toast.makeText(ShareActivity.this, "文件读取失败", Toast.LENGTH_SHORT).show();
                }

            }
        }
        finish();
    }
}
