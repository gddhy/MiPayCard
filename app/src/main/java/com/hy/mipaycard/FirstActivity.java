package com.hy.mipaycard;

import static com.hy.mipaycard.BaseApplication.getContext;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.hy.mipaycard.online_card.OnlineCardActivity;

public class FirstActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        TextView textView = new TextView(this);
        textView.setText(Html.fromHtml(
                "<br/>根据相关政策规定，您需要先阅读《用户协议》与《隐私政策》后才能使用本软件。" + "<br/><br/>"
                        + "《用户协议》<br/><a href=\"https://gddhy.net/mipaycard/license\">https://gddhy.net/mipaycard/license</a>"  + "<br/><br/>"
                        + "《隐私政策》<br/><a href=\"https://gddhy.net/mipaycard/privacy\">https://gddhy.net/mipaycard/privacy</a>"
        ));
        textView.setMovementMethod(LinkMovementMethod.getInstance());

        new AlertDialog.Builder(this)
                .setIcon(R.mipmap.icon)
                .setTitle(R.string.app_name)
                .setView(textView)
                .setCancelable(false)
                .setPositiveButton("已阅读并同意", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        openMain();
                    }
                })
                .setNegativeButton("不同意", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                })
                .show();
    }

    public void openMain(){

        getPackageManager().setComponentEnabledSetting(new ComponentName(getContext(),getContext().getPackageName()+".MainActivity"),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);

        Toast.makeText(this,"感谢使用 "+getString(R.string.app_name),Toast.LENGTH_LONG).show();

        startActivity(new Intent(FirstActivity.this, MainActivity.class));

        getPackageManager().setComponentEnabledSetting(new ComponentName(getContext(),getContext().getPackageName()+".FirstActivity"),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);

        finish();
    }
}