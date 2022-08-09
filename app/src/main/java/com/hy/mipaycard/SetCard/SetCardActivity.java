package com.hy.mipaycard.SetCard;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import com.hy.mipaycard.Config;
import com.hy.mipaycard.R;
import com.hy.mipaycard.SetCard.OnePlus.SetCardOnePlusActivity;
import com.hy.mipaycard.Utils.BitmapUtils;
import com.hy.mipaycard.Utils.CardList;
import com.hy.mipaycard.Utils.PhotoUtils;
import com.hy.mipaycard.Utils.cmdUtil;
import com.hy.mipaycard.SetCard.set_card.SetCardNewActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.hy.mipaycard.Config.defaultSet;
import static com.hy.mipaycard.Config.getTempFile;
import static com.hy.mipaycard.Config.mi_wallet;
import static com.hy.mipaycard.Config.pay_pic;
import static com.hy.mipaycard.MainUtils.AddTextToBitmap;
import static com.hy.mipaycard.MainUtils.saveBitmapAsPng;
import static com.hy.mipaycard.Utils.CardList.getCardName;
import static com.hy.mipaycard.Utils.cmdUtil.isRooted;
import static com.hy.mipaycard.Utils.cmdUtil.runRootShell;
import static com.hy.mipaycard.SetCard.set_card.SetCardNewActivity.pKillServer;
import static com.hy.mipaycard.shortcuts.SetMenuPermissionActivity.onlyRead;

public class SetCardActivity extends AppCompatActivity {
    private SharedPreferences pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pref = getSharedPreferences("set", Context.MODE_PRIVATE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        Intent intent = getIntent();
        String filePath = intent.getStringExtra(Config.file_Path);
        boolean isAuto = intent.getBooleanExtra(Config.is_Auto,false);
        int isUseNew = pref.getInt("isUseNew",defaultSet);
        if(isUseNew != 0){
            Intent intent2 = new Intent(SetCardActivity.this, isUseNew == 3 ? SetCardOnePlusActivity.class : SetCardNewActivity.class);
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
        if(path.contains(" ") || CardList.checkChinese(path)){
            File file = getTempFile();
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
                            File file = getTempFile();
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

                        String[] cmd ;
                        String tips = "";
                        if(CardList.checkChinese(cardName[i])&&pref.getBoolean("AssociativeReplacement",false)){
                            List<String> stringList = new ArrayList<String>();
                            int times = 0;
                            for(int j=0;j<cardList.length;j++){
                                String chineseName = cardName[j];
                                if(cardName[i].equals(chineseName)){
                                    String filePath2 = pay_pic+"/"+cardList[j];
                                    stringList.add("cp "+ tempPath +" "+filePath2);
                                    stringList.add("chmod 444 "+filePath2);
                                    times++;
                                }
                            }
                            if (pref.getBoolean("setAuto",false)){
                                stringList.add(onlyRead);
                            }
                            cmd = stringList.toArray(new String[0]);
                            tips= "\n关联替换"+(--times)+"次";
                        } else {
                            String filePath2 = pay_pic+"/"+cardList[i];
                            if (pref.getBoolean("setAuto",false)){
                                cmd = new String[]{"cp "+ tempPath +" "+filePath2,"chmod 444 "+filePath2 , onlyRead };
                            } else {
                                cmd = new String[]{"cp "+ tempPath +" "+filePath2,"chmod 444 "+filePath2 };
                            }
                        }
                        runRootShell(cmd);
                        Toast.makeText(SetCardActivity.this,"已替换"+tips,Toast.LENGTH_LONG).show();
                        pKillServer(pref);
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
                .setPositiveButton("返回", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                })
                .show();
    }
}
