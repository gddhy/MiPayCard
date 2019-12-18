package com.hy.mipaycard;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

import static com.by_syk.lib.uri.UriAnalyser.getRealPath;
import static com.hy.mipaycard.MainActivity.ref_media;
import static com.hy.mipaycard.MainUtils.saveBitmapAsPng;
import static com.hy.mipaycard.MainUtils.toRoundCorner;

public class RoundImageActivity extends AppCompatActivity {
    ImageView imageView;
    EditText editText;
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    String filePath = null;
    TextView textView;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_round_image);
        pref = PreferenceManager.getDefaultSharedPreferences(this);
        imageView = (ImageView)findViewById(R.id.mainImageView);
        editText = (EditText)findViewById(R.id.mainEditText);
        textView = (TextView)findViewById(R.id.mainTextView);
        editText.setText(""+pref.getInt("pixels",40));
        editText.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // 输入的内容变化的监听
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // 输入前的监听
            }

            @Override
            public void afterTextChanged(Editable s) {
                // 输入后的监听
                String str = editText.getText().toString();
                if(str.length()!=0){
                    int p = Integer.parseInt(str);
                    if(p>=0){
                        editor = pref.edit();
                        editor.putInt("pixels",p);
                        editor.apply();
                        if(filePath!=null){
                            displayImage(filePath);
                        }
                    } else {
                        editText.setText(0+"");
                        Toast.makeText(RoundImageActivity.this,"圆角不能低于0", Toast.LENGTH_LONG).show();
                    }
                }

            }
        });

        Intent intent = getIntent();
        String action = intent.getAction();
        if (Intent.ACTION_VIEW.equals(action)) {
            Uri uri = intent.getData();
            filePath=getRealPath(this,uri);
            displayImage(filePath);
        }
    }

    public void onClick(View v){
        Intent intent = new Intent("android.intent.action.GET_CONTENT");
        intent.setType("image/*");
        startActivityForResult(intent, 1);
    }

    @SuppressLint("SetTextI18n")
    public void on1Click(View v){
        String str = editText.getText().toString();
        if(str.length()!=0){
            int p = Integer.parseInt(str);
            editText.setText(--p+"");
        }
    }

    @SuppressLint("SetTextI18n")
    public void on2Click(View v){
        String str = editText.getText().toString();
        if(str.length()!=0){
            int p = Integer.parseInt(str);
            editText.setText(++p+"");
        }
    }

    @SuppressLint("SetTextI18n")
    public void on10Click(View v){
        String str = editText.getText().toString();
        if(str.length()!=0){
            int p = Integer.parseInt(str);
            p = p-10;
            editText.setText(p+"");
        }
    }

    @SuppressLint("SetTextI18n")
    public void on20Click(View v){
        String str = editText.getText().toString();
        if(str.length()!=0){
            int p = Integer.parseInt(str);
            p=p+10;
            editText.setText(p+"");
        }
    }

    public void on3Click(View v){
        if(filePath==null){
            Toast.makeText(this,"未选择文件",Toast.LENGTH_LONG).show();
        } else {
            makeRoundBitmapFile(this,filePath,pref);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 1:
                if (resultCode == RESULT_OK) {
                    filePath = getRealPath(RoundImageActivity.this,data.getData());
                    displayImage(filePath);
                }
                break;
            default:
                break;
        }
    }


    @SuppressLint("SetTextI18n")
    private void displayImage(String imagePath) {
        if (imagePath != null) {
            textView.setText("已选择："+imagePath);
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            bitmap = toRoundCorner(bitmap,pref.getInt("pixels",40));
            imageView.setImageBitmap(bitmap);
        } else {
            Toast.makeText(this, "图片获取失败", Toast.LENGTH_SHORT).show();
        }
    }

    public static void makeRoundBitmapFile(Context context, String filePath, SharedPreferences pref){
        Bitmap bitmap = BitmapFactory.decodeFile(filePath);
        bitmap = toRoundCorner(bitmap,pref.getInt("pixels",40));
        File file = new File(filePath+"_round.png");
        saveBitmapAsPng(bitmap,file);
        ref_media(context,file);
        Toast.makeText(context,"已保存到\n"+file.getPath(),Toast.LENGTH_LONG).show();
    }
}
