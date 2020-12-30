package com.hy.mipaycard;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import androidx.core.content.FileProvider;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Toast;

import com.hy.mipaycard.Utils.CardList;
import com.hy.mipaycard.Utils.cmdUtil;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.hy.mipaycard.Config.getExternalCache;

public class EmailActivity extends AppCompatActivity {
    private File file;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        file = new File(getExternalCache(),"Card2");
        init();
    }

    private void init(){
        String[] list = CardList.getWeiShiPeiFileName();
        StringBuilder str = new StringBuilder();
        StringBuilder info = new StringBuilder();
        String deviceInfo = "";//DeviceInfo.getBaseInfo(EmailActivity.this);
        if (list == null || list.length == 0){
            str = new StringBuilder("本机所有卡面都已适配");
        } else {
            info.append("若文本格式不对，请更换邮箱软件\nCardFile后文本请勿修改，请在CardName后输入卡片具体名称(请与图片FileName对应)\n\n");
            str.append("未适配卡面：");
            if(!file.exists()){
                file.mkdirs();
            }
            List<String> cmdList = new ArrayList<String>();
            for (int i =0;i<list.length;i++){
                cmdList.add("cp /data/data/com.miui.tsmclient/cache/image_manager_disk_cache/"+list[i]+" "+new File(file,"Card2"+ (i+1) + ".png").getPath());
                info.append("CardFile: ").append(list[i]).append("\nCardName: ").append("\nFileName: Card2").append(i+1).append(".png").append("\n\n");
                str.append("\n").append(i+1).append("：").append(list[i]);
            }
            cmdUtil.runRootShell(cmdList.toArray(new String[cmdList.size()]));
            save(info.toString()+"\n\n"+deviceInfo);
            str.append("\n将通过邮箱提交适配请求，需要手机已安装邮箱类软件(不建议使用小米邮件，会导致排版错乱)\n请先恢复默认卡面");
        }
        final String text = info.toString()+"\n\n"+deviceInfo;
        AlertDialog.Builder builder = new AlertDialog.Builder(EmailActivity.this)
                .setTitle("卡面适配")
                .setMessage(str.toString());

                if (list != null && list.length != 0) {
                    builder.setNegativeButton("提交适配", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
                            String[] tos = { "gddhy@foxmail.com","2715655450@qq.com" };
                            intent.putExtra(Intent.EXTRA_EMAIL, tos);
                            intent.putExtra(Intent.EXTRA_TEXT, text);
                            intent.putExtra(Intent.EXTRA_SUBJECT, "卡面适配");

                            ArrayList<Uri> fileUris = new ArrayList<Uri>();

                            String[] fileList = file.list();
                            for(int i2 = 0;i2<fileList.length;i2++){
                                File fileItems = new File(file,fileList[i2]);
                                if (fileItems.isFile()){
                                    Uri uri;
                                    //安卓7.0+用uri传递
                                    if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.N){
                                        uri = FileProvider.getUriForFile(EmailActivity.this,EmailActivity.this.getPackageName()+".FileProvider",fileItems);
                                    } else {
                                        uri = Uri.fromFile(fileItems);
                                    }
                                    fileUris.add(uri);
                                }
                            }
                            intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM,fileUris);
                            intent.setType("*/*");
                            intent.setType("message/rfc882");
                            Intent.createChooser(intent, "请选择邮箱应用");

                            try {
                                EmailActivity.this.startActivity(intent);
                                Toast.makeText(EmailActivity.this,"请选择邮箱应用",Toast.LENGTH_LONG).show();
                            } catch (Exception e){
                                e.printStackTrace();
                                Toast.makeText(EmailActivity.this,"您手机未安装邮箱类应用，无法使用提交卡面",Toast.LENGTH_LONG).show();
                            }

                            finish();
                        }
                    });
                }
                builder.setPositiveButton("返回", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                finish();
                            }
                        })
                        .setNeutralButton("加群交流", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if(!joinQQGroup(EmailActivity.this,"N-A6occcPxGl_v-PEHCr3Hi0XwyQXyii")){
                                    Toast.makeText(EmailActivity.this,"请安装或升级QQ",Toast.LENGTH_LONG).show();
                                }
                                finish();
                            }
                        })
                        .setOnDismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialogInterface) {
                                finish();
                            }
                        })
                        //.setCancelable(false)
                        .show();

    }

    private void save(String data) {
        BufferedWriter writer = null;
        File fileName = new File(file,"CardInfo.txt");
        if(!fileName.exists()){
            try {
                fileName.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        @SuppressLint("SimpleDateFormat") String time = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss").format(new Date());
        try {
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName,false), StandardCharsets.UTF_8));
            writer.write("----"+time+"----\n\n");
            writer.write(data);
            writer.write("\n");
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                if(writer != null){
                    writer.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /****************
     *
     * 发起添加群流程。群号：MI Pay卡片美化交流群(580239210) 的 key 为： N-A6occcPxGl_v-PEHCr3Hi0XwyQXyii
     * 调用 joinQQGroup(N-A6occcPxGl_v-PEHCr3Hi0XwyQXyii) 即可发起手Q客户端申请加群 MI Pay卡片美化交流群(580239210)
     *
     * @param key 由官网生成的key
     * @return 返回true表示呼起手Q成功，返回fals表示呼起失败
     ******************/
    public static boolean joinQQGroup(Context context, String key) {
        Intent intent = new Intent();
        intent.setData(Uri.parse("mqqopensdkapi://bizAgent/qm/qr?url=http%3A%2F%2Fqm.qq.com%2Fcgi-bin%2Fqm%2Fqr%3Ffrom%3Dapp%26p%3Dandroid%26k%3D" + key));
        // 此Flag可根据具体产品需要自定义，如设置，则在加群界面按返回，返回手Q主界面，不设置，按返回会返回到呼起产品界面    //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            context.startActivity(intent);
            return true;
        } catch (Exception e) {
            // 未安装手Q或安装的版本不支持
            return false;
        }
    }
}
