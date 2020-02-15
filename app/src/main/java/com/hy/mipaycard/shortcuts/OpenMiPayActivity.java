package com.hy.mipaycard.shortcuts;

import android.content.Context;
import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import static com.hy.mipaycard.MainUtils.isInstallApp;

public class OpenMiPayActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        openMiPay(OpenMiPayActivity.this);
        finish();
    }

    public static void openMiPay(Context context){
        //runRootShell(new String[]{"am start -n com.miui.tsmclient/com.miui.tsmclient.ui.quick.DoubleClickActivity"});
        if(isInstallApp(context,"com.miui.tsmclient")) {
            Intent i = new Intent();
            i.setClassName("com.miui.tsmclient", "com.miui.tsmclient.ui.quick.DoubleClickActivity");
            context.startActivity(i);
        } else {
            Toast.makeText(context,"您的设备不支持MiPay",Toast.LENGTH_LONG).show();
        }

    }
}
