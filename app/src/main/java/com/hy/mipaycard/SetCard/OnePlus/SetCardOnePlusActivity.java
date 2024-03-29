package com.hy.mipaycard.SetCard.OnePlus;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.hy.mipaycard.Config;
import com.hy.mipaycard.R;
import com.hy.mipaycard.Utils.BitmapUtils;
import com.hy.mipaycard.Utils.CardList;
import com.hy.mipaycard.Utils.PhotoUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.hy.mipaycard.Config.OnePlusPath;
import static com.hy.mipaycard.Config.default_PKILL;
import static com.hy.mipaycard.Config.getExternalCache;
import static com.hy.mipaycard.Config.getTempFile;
import static com.hy.mipaycard.MainUtils.AddTextToBitmap;
import static com.hy.mipaycard.MainUtils.saveBitmapAsPng;
import static com.hy.mipaycard.Utils.CardList.getCardName;
import static com.hy.mipaycard.Utils.cmdUtil.getOnePlusList;
import static com.hy.mipaycard.Utils.cmdUtil.isRooted;
import static com.hy.mipaycard.Utils.cmdUtil.runRootShell;
import static com.hy.mipaycard.Utils.cmdUtil.runWhoami;

public class SetCardOnePlusActivity extends AppCompatActivity {
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private List<List_card> cardList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_card_new);
        pref = getSharedPreferences("set", Context.MODE_PRIVATE);
        final Intent intent = getIntent();
        String filePath = intent.getStringExtra(Config.file_Path);
        final boolean isAuto = intent.getBooleanExtra(Config.is_Auto,false);
        CardList.initLocalCardList(this);
        if (!new File(filePath).exists()) {
            Toast.makeText(this, "图片不存在", Toast.LENGTH_LONG).show();
            finish();
        }
        String[] list = getOnePlusList();
        File file = new File(getExternalCache(),"OnePlus");
        if(!file.exists()){
            file.mkdirs();
        }
        List<String> cmdList = new ArrayList<String>();
        for (int i =0;i<list.length;i++){
            File f = new File(file,list[i]);
            if(!f.exists())
                cmdList.add("cp "+OnePlusPath+"/"+list[i]+" "+f.getPath());
        }
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.R){
            cmdList.add("chown -R " + runWhoami() + " "+file.getPath());
        }
        String log = runRootShell(cmdList.toArray(new String[cmdList.size()]));
        delTmpFile(file,list);
        final File[] files_list = file.listFiles();
        if (files_list != null) {
            for(int i = 0;i<files_list.length;i++){
                cardList.add(new List_card(getCardName(files_list[i].getName()),files_list[i]));
            }
        }
        ArrayAdapter adapter = new List_Adapter(this, R.layout.list_item,cardList);
        ListView listView = findViewById(R.id.list_view);
        listView.setAdapter(adapter);
        if(filePath.contains(" ") || CardList.checkChinese(filePath)){
            File tmp_file = getTempFile();
            CardList.copyFile(filePath,tmp_file.getPath());
            filePath = tmp_file.getPath();
        }
        File[] listFiles = file.listFiles();
        if(listFiles == null || listFiles.length==0){
            if(!isRooted()) {
                Toast.makeText(this, "请授予软件root权限后再使用该功能", Toast.LENGTH_LONG).show();
                finish();
            } else {
                Toast.makeText(this, "未读到卡面列表或您的设备不支持", Toast.LENGTH_LONG).show();
            }
        }
        final String finalPath = filePath;
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String tempPath;
                String cardName = getCardName(files_list[position].getName());

                //自动加水印
                if( isAuto && CardList.checkChinese(cardName)){
                    Bitmap bitmap ;
                    if (cardName.contains("银行")||cardName.contains("银")||cardName.contains("行")||cardName.contains("信")||cardName.contains("闪付")){
                        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.union_pay);
                    } else {
                        bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.china_t_union);
                    }
                    String name;
                    if (cardName.contains(" ")){
                        name = cardName.substring(0,cardName.indexOf(" "));
                    } else {
                        name = cardName;
                    }
                    File file = getTempFile();
                    Bitmap bmpBackground = PhotoUtils.getBitmapFromUri(Uri.fromFile(files_list[position]), SetCardOnePlusActivity.this);
                    if(!cardName.contains("门禁卡")) {
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
                if(CardList.checkChinese(cardName)&&pref.getBoolean("AssociativeReplacement",false)){
                    List<String> stringList = new ArrayList<String>();
                    int times = 0;
                    for(int i=0;i<files_list.length;i++){
                        String chineseName = getCardName(files_list[i].getName());
                        if(cardName.equals(chineseName)){
                            String filePath = OnePlusPath+"/"+files_list[i].getName();
                            stringList.add("cp "+ tempPath +" "+filePath);
                            stringList.add("chmod 444 "+filePath);
                            times++;
                        }
                    }
                    if (pref.getBoolean("setAuto",false)){
                        stringList.add("chmod 2500 "+OnePlusPath );
                    }
                    cmd = stringList.toArray(new String[0]);
                    tips= "\n关联替换"+(--times)+"次";
                } else {
                    String filePath = OnePlusPath+"/"+files_list[position].getName();
                    if (pref.getBoolean("setAuto",false)){
                        //TOdo
                        cmd = new String[]{"cp "+ tempPath +" "+filePath,"chmod 444 "+filePath , "chmod 2500 "+OnePlusPath };
                    } else {
                        cmd = new String[]{"cp "+ tempPath +" "+filePath,"chmod 444 "+filePath };
                    }
                }
                runRootShell(cmd);
                Toast.makeText(SetCardOnePlusActivity.this,"已替换"+tips,Toast.LENGTH_LONG).show();
                pKillServer(pref);
                finish();
            }
        });

        //长按菜单
        listView.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
            @Override
            public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {
                contextMenu.add(Menu.NONE, 0, 0, "恢复默认卡面");
                //contextMenu.add(Menu.NONE, 1, 0, "恢复默认");
            }
        });//https://blog.csdn.net/hello_1s/article/details/51837394
        invalidateOptionsMenu();//通知系统刷新Menu
    }

    //选中菜单Item后触发
    public boolean onContextItemSelected(MenuItem item){
        //关键代码在这里
        AdapterView.AdapterContextMenuInfo menuInfo;
        menuInfo =(AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        switch (item.getItemId()){
            /* case 0:
                //点击第一个菜单项要做的事，如获取点击listview的位置
                Toast.makeText(this, String.valueOf(menuInfo.position), Toast.LENGTH_LONG).show();
                break;*/
            case 0:
                runRootShell(new String[]{"rm -rf "+OnePlusPath+"/"+cardList.get(menuInfo.position).getImageFile().getName()});
                //点击第二个菜单项要做的事，如获取点击的数据
                Toast.makeText(this, "已恢复"+cardList.get(menuInfo.position).getName()+"默认卡面", Toast.LENGTH_LONG).show();
                break;
        }
        return super.onContextItemSelected(item);
    }


    private void delTmpFile(File workDir,String[] names){
        File[] files = workDir.listFiles();
        if (files != null) {
            for(int i = 0 ;i < files.length ; i++){
                if (isMoreFile(files[i],names))
                    files[i].delete();
            }
        }
    }

    private boolean isMoreFile(File file,String[] listName){
        for(int i = 0 ; i < listName.length ; i++){
            if(file.getName().equals(listName[i])){
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_set,menu);
        return true;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()) {
            case R.id.menu_set_pkill:
                boolean isChecked = !pref.getBoolean("pkill",default_PKILL);
                editor = pref.edit();
                editor.putBoolean("pkill",isChecked);
                editor.apply();
                pKillServer(pref);
            default:
        }
        return true;
    }

    //刷新menu
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.menu_set_pkill).setChecked(pref.getBoolean("pkill",default_PKILL));
        return true;
    }

    public static void pKillServer(SharedPreferences pref){
        boolean isChecked = pref.getBoolean("pkill",default_PKILL);
        if(isChecked){
            runRootShell(new String[]{"pkill -f com.finshell.wallet"});
        }
    }
}
