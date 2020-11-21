package com.hy.mipaycard;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.by_syk.lib.uri.UriAnalyser;

import java.io.File;
import java.util.Objects;

import static com.by_syk.lib.uri.UriAnalyser.getRealPath;
import static com.hy.mipaycard.Config.fileWork;
import static com.hy.mipaycard.Config.getExternalCache;
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
    ProgressBar progressBar;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_round_image);
        pref = PreferenceManager.getDefaultSharedPreferences(this);
        imageView = (ImageView)findViewById(R.id.mainImageView);
        editText = (EditText)findViewById(R.id.mainEditText);
        textView = (TextView)findViewById(R.id.mainTextView);
        progressBar =  findViewById(R.id.progress_bar);
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


        //todo
        Intent intent = getIntent();
        String action = intent.getAction();
        if (Intent.ACTION_VIEW.equals(action)) {
            Uri uri = intent.getData();
            String data = intent.getDataString();
            String str = null;
            if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.Q) {
                try {
                    if (!data.contains("file://")) {
                        File f = MainActivity.saveFileFromSAF(this, uri);
                        if (f != null)
                            str = f.getPath();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(this, "文件读取失败", Toast.LENGTH_LONG).show();
                }
            } else{
                filePath=getRealPath(this,uri);
                displayImage(filePath);
            }

            if (str!=null){
                filePath =str;
                displayImage(filePath);
            }
        } else if(Objects.equals(action, "Round_Image")){
            String path = null;
            try {
                path = intent.getExtras().getString("path");
            } catch (Exception e){
                e.printStackTrace();
            }
            if(path!=null){
                filePath =path;
                displayImage(filePath);
            }
        } else if(Intent.ACTION_SEND.equals(action)){
            String path = null;
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                Uri uri = (Uri) bundle.get(Intent.EXTRA_STREAM);
                if (uri != null) {
                    if (Build.VERSION.SDK_INT >=Build.VERSION_CODES.Q) {
                        String u = uri.toString();
                        Log.d("URI: ", "" + u);
                        try {
                            if (!u.contains("file://")) {
                                File f = MainActivity.saveFileFromSAF(this, uri);
                                if (f != null) {
                                    path = f.getPath();
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        path = UriAnalyser.getRealPath(this, uri);
                    }
                }
            }

            if(path!=null){
                filePath =path;
                displayImage(filePath);
            }
        }
    }

    public static void openRoundImage(Context context,String file_Path){
        Intent intent = new Intent(context,RoundImageActivity.class);
        intent.setAction("Round_Image");
        intent.putExtra("path",file_Path);
        context.startActivity(intent);
    }

    public void onClick(View v){
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.Q){
            startActivityForResult(intent, 2);
        } else {
            startActivityForResult(intent, 1);
        }
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
            //imageView.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    final String savePath = makeRoundBitmapFile(RoundImageActivity.this,filePath,pref);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //imageView.setVisibility(View.VISIBLE);
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(RoundImageActivity.this,"已保存到\n"+savePath,Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }).start();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case 1:
                    filePath = getRealPath(RoundImageActivity.this, data.getData());
                    displayImage(filePath);
                    break;
                case 2:
                    File f = MainActivity.saveFileFromSAF(this, data.getData());
                    if (f != null) {
                        filePath = f.getPath();
                        displayImage(filePath);
                    }
                    break;
                default:
                    break;
            }
        } else {
            Toast.makeText(this, "未选择文件", Toast.LENGTH_LONG).show();
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

    public static String makeRoundBitmapFile(Context context, String filePath, SharedPreferences pref){
        Bitmap bitmap = BitmapFactory.decodeFile(filePath);
        bitmap = toRoundCorner(bitmap,pref.getInt("pixels",40));
        String fileName = new File(filePath).getName();
        if(fileName.contains(".")){
            fileName = fileName.substring(0,fileName.lastIndexOf("."));
        }
        File file ;
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.Q){
            file = new File(fileWork(context),fileName+"_round.png");
        } else if(filePath.contains(new File(getExternalCache(),"SAF").getPath())) {
            file = new File(fileWork(context),fileName+"_round.png");
        } else {
            file = new File(new File( filePath).getParentFile(),fileName+"_round.png");
        }
        saveBitmapAsPng(bitmap,file);
        ref_media(context,file);
        //Toast.makeText(context,"已保存到\n"+file.getPath(),Toast.LENGTH_LONG).show();
        if(fileWork(context).getPath().equals(file.getParent())){
            LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(Config.localAction));
        }
        return file.getPath();
    }
}
