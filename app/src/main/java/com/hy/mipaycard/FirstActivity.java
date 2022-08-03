package com.hy.mipaycard;

import static com.hy.mipaycard.BaseApplication.getContext;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.hy.mipaycard.online_card.OnlineCardActivity;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.List;

public class FirstActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        if(isDebug()){
            openMain();
        }

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

        Intent intent = new Intent(FirstActivity.this,MainActivity.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        startActivity(intent);

        if(!isDebug()) {

            getPackageManager().setComponentEnabledSetting(new ComponentName(getContext(), getContext().getPackageName() + ".FirstActivity"),
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP);
        }

        finish();
    }

    public boolean isDebug(){
        final String release = "D613D48FA57FCFDA4025A65701D3DEBC";
        String debug = getSign(FirstActivity.this);
        return ! release.equals(debug);
    }

    public static String getSign(Context context) {
        String sign = "";
        PackageManager pm = context.getPackageManager();
        List<PackageInfo> apps = pm.getInstalledPackages(PackageManager.GET_SIGNATURES);
        for (PackageInfo packageinfo : apps) {
            String packageName = packageinfo.packageName;

            if (packageName.equals(context.getPackageName())) {
                sign = MD5Encode(packageinfo.signatures[0].toByteArray());
            }
        }
        return sign;
    }

    private static String MD5Encode(byte[] toEncode) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.reset();
            md5.update(toEncode);
            return HexEncode(md5.digest());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    private static String HexEncode(byte[] toEncode) {
        StringBuilder sb = new StringBuilder(toEncode.length * 2);
        for (byte b : toEncode) {
            sb.append(Integer.toHexString((b & 0xf0) >>> 4));
            sb.append(Integer.toHexString(b & 0x0f));
        }
        return sb.toString().toUpperCase();
    }
}