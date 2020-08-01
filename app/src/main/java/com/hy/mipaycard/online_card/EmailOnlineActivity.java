package com.hy.mipaycard.online_card;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.net.Uri;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.hy.mipaycard.R;

import static com.hy.mipaycard.online_card.online_utils.sendEmail;

public class EmailOnlineActivity extends AppCompatActivity {
    private EditText card_name;
    private EditText img_link;
    private EditText user_name;
    private EditText about_text;
    private EditText email;
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_online);
        card_name = findViewById(R.id.card_name);
        img_link = findViewById(R.id.pic_link);
        user_name = findViewById(R.id.user_name);
        about_text = findViewById(R.id.about);
        email = findViewById(R.id.email);
        imageView = findViewById(R.id.bmp);
        imageView.setImageBitmap(getTmpBitmap());
        img_link.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // 输入的内容变化的监听
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
                // 输入前的监听
            }
            @Override
            public void afterTextChanged(Editable s) {
                // 输入后的监听
                String link = img_link.getText().toString();
                if (link.length()!=0)
                    Glide.with(EmailOnlineActivity.this).load(link).into(imageView);
            }
        });
        //https://blog.csdn.net/sinat_35241409/article/details/53709537
    }

    public static String makeJsonData(String cardName,String link,String userName,String about,String email){
        return "{\"cardName\":\""+cardName+"\",\"link\":\""+link+"\",\"userName\":\""+userName+"\",\"about\":\""+about+"\",\"email\":\""+email+"\"}";
    }

    public void onEmailClick(View view) {
        if (card_name.getText().toString().length()==0||img_link.getText().toString().length()==0||user_name.getText().toString().length()==0||about_text.getText().toString().length()==0||email.getText().toString().length()==0){
            Toast.makeText(EmailOnlineActivity.this,"有项目未填写",Toast.LENGTH_LONG).show();
        } else {
            sendEmail(EmailOnlineActivity.this,makeJsonData(card_name.getText().toString(),img_link.getText().toString(),user_name.getText().toString(),about_text.getText().toString(),email.getText().toString()));
        }
    }

    public void onimgchrClick(View view) {
        openUrl(EmailOnlineActivity.this,"https://imgse.com/");
    }

    public static void openUrl(Context context, String url){
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        context.startActivity(intent);

    }

    public void onImgBoxClick(View view) {
        openUrl(EmailOnlineActivity.this,"https://sm.ms");
    }

    public void onGetClipClick(View view) {
        img_link.setText(getCopy(EmailOnlineActivity.this));
    }

    //系统剪贴板-复制:   s为内容
    public static void copy(Context context, String s) {
        // 获取系统剪贴板
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        // 创建一个剪贴数据集，包含一个普通文本数据条目（需要复制的数据）
        ClipData clipData = ClipData.newPlainText(null, s);
        // 把数据集设置（复制）到剪贴板
        clipboard.setPrimaryClip(clipData);
    }

    //系统剪贴板-获取:
    public static String getCopy(Context context) {
        // 获取系统剪贴板
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        // 返回数据
        ClipData clipData = clipboard.getPrimaryClip();
        if (clipData != null && clipData.getItemCount() > 0) {
            // 从数据集中获取（粘贴）第一条文本数据
            return clipData.getItemAt(0).getText().toString();
        }
        return null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 0, 0, "在线图片说明");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()) {
            case 0:
                showAbout(EmailOnlineActivity.this);
                break;
            default:
                break;
        }
        return true;
    }

    public static void showAbout(Context context){
        new AlertDialog.Builder(context)
                .setTitle("在线图片说明")
                .setMessage("程序在线图片由用户上传并储存在第三方图床中，app只提供图片展示与卡面替换等功能，用户提交图片可以获取图片直链后在app中提交，由回忆添加到程序中\n官方卡面只收集银行卡官方默认卡面\n填写图片直链时，若链接正确，下方会展示预览图")
                .setPositiveButton("确定",null)
                .show();
    }

    public Bitmap getTmpBitmap() {
        String text = "直链图片预览";
        Bitmap bitmap = Bitmap.createBitmap(605, 960, Bitmap.Config.ARGB_8888);
        Canvas mCanvas = new Canvas(bitmap);
        Paint mPaint = new Paint();
        mPaint.setColor(Color.GRAY);
        //mPaint.setColor(isDarkTheme(EmailOnlineActivity.this)?Color.WHITE:Color.BLACK);
        mPaint.setTextSize(72);
        Typeface font = Typeface.create(Typeface.SANS_SERIF,Typeface.BOLD);
        mPaint.setTypeface(font);
        mCanvas.drawText(text, 10,280,mPaint);
        mCanvas.save();
        mCanvas.restore();

        return bitmap;
    }

    public void onImgBedClick(View view) {
        openUrl(EmailOnlineActivity.this,"https://gddhy.github.io/img/");
    }
}
