package com.hy.mipaycard;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.preference.PreferenceManager;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Toast;

import com.hy.mipaycard.Utils.BitmapUtils;
import com.hy.mipaycard.Utils.CardList;
import com.hy.mipaycard.Utils.PhotoUtils;
import com.hy.mipaycard.Utils.cmdUtil;
import com.hy.mipaycard.set_card_new.SetCardNewActivity;

import java.io.File;

import static com.hy.mipaycard.Config.defaultSet;
import static com.hy.mipaycard.Config.getTempFile;
import static com.hy.mipaycard.Config.pay_pic;
import static com.hy.mipaycard.MainUtils.AddTextToBitmap;
import static com.hy.mipaycard.MainUtils.saveBitmapAsPng;
import static com.hy.mipaycard.Utils.cmdUtil.isRooted;
import static com.hy.mipaycard.Utils.cmdUtil.runRootShell;
import static com.hy.mipaycard.set_card_new.SetCardNewActivity.pKillServer;
import static com.hy.mipaycard.shortcuts.SetMenuPermissionActivity.onlyRead;

public class SetCardActivity extends AppCompatActivity {
    private SharedPreferences pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pref = PreferenceManager.getDefaultSharedPreferences(SetCardActivity.this);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        Intent intent = getIntent();
        String filePath = intent.getStringExtra(Config.file_Path);
        boolean isAuto = intent.getBooleanExtra(Config.is_Auto,false);
        int isUseNew = pref.getInt("isUseNew",defaultSet);
        if(isUseNew != 0){
            Intent intent2 = new Intent(SetCardActivity.this, SetCardNewActivity.class);
            intent2.putExtra(Config.file_Path, filePath);
            intent2.putExtra(Config.is_Auto, isAuto);
            startActivity(intent2);
            finish();
        } else {
            CardList.initLocalCardList(SetCardActivity.this);
            if (!new File(filePath).exists()) {
                Toast.makeText(SetCardActivity.this, "图片不存在", Toast.LENGTH_LONG).show();
                finish();
            }
            setCard(filePath, isAuto);
        }
    }

    private void setCard(String path, final boolean isAuto){
        if(CardList.checkChinese(path)){
            File file = getTempFile(SetCardActivity.this);
            CardList.copyFile(path,file.getPath());
            path = file.getPath();
        }
        final String[] cardList = cmdUtil.getTsmclient();
        final String[] cardName = CardList.getCardName(cardList);
        if(cardName.length==0){
            if(!isRooted()) {
                Toast.makeText(this, "请授予软件root权限后再使用该功能", Toast.LENGTH_LONG).show();
                finish();
            }  else {
                Toast.makeText(this, "未读到卡面列表或您的设备不支持", Toast.LENGTH_LONG).show();
            }
        }
        final String finalPath = path;
        new AlertDialog.Builder(SetCardActivity.this)
                .setTitle("请选择要替换的卡面")
                .setItems(cardName,new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int i){
                        String tempPath;

                        //自动加水印
                        if(isAuto && CardList.checkChinese(cardName[i])){
                            Bitmap bitmap ;
                            if (cardName[i].contains("银行")||cardName[i].contains("银")||cardName[i].contains("行")||cardName[i].contains("信")||cardName[i].contains("闪付")){
                                bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.union_pay);
                            } else {
                                bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.china_t_union);
                            }
                            String name;
                            if (cardName[i].contains(" ")){
                                name = cardName[i].substring(0,cardName[i].indexOf(" "));
                            } else {
                                name = cardName[i];
                            }
                            File file = getTempFile(SetCardActivity.this);
                            Bitmap bmpBackground = PhotoUtils.getBitmapFromUri(Uri.fromFile(new File(finalPath)), SetCardActivity.this);
                            if(!cardName[i].contains("门禁卡")) {
                                bmpBackground = BitmapUtils.mergeBitmap(bmpBackground, bitmap);
                            }
                            bmpBackground = AddTextToBitmap(bmpBackground,name);
                            saveBitmapAsPng(bmpBackground, file);
                            tempPath = file.getPath();
                        } else {
                            tempPath = finalPath;
                        }

                        String filePath = pay_pic+"/"+cardList[i];
                        String[] cmd ;
                        if (pref.getBoolean("setAuto",false)){
                            cmd = new String[]{"cp "+ tempPath +" "+filePath,"chmod 444 "+filePath , onlyRead };
                        } else {
                            cmd = new String[]{"cp "+ tempPath +" "+filePath,"chmod 444 "+filePath };
                        }
                        runRootShell(cmd);
                        Toast.makeText(SetCardActivity.this,"已替换",Toast.LENGTH_LONG).show();
                        /*File tmpFile = getTempFile(SetCardActivity.this);
                        if (tempPath.equals(tmpFile.getPath())){
                            tmpFile.delete();
                        }*/
                        pKillServer(pref);
                        finish();
                    }
                })
                .setCancelable(false)
                .setPositiveButton("返回", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                })
                .show();
    }
}
