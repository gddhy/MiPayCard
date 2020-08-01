package com.hy.mipaycard.set_card_new;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.preference.PreferenceManager;
import androidx.appcompat.app.AppCompatActivity;
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

import static com.hy.mipaycard.Config.defaultSet;
import static com.hy.mipaycard.Config.getExternalCache;
import static com.hy.mipaycard.Config.getTempFile;
import static com.hy.mipaycard.Config.mi_wallet;
import static com.hy.mipaycard.Config.pay_pic;
import static com.hy.mipaycard.MainUtils.AddTextToBitmap;
import static com.hy.mipaycard.MainUtils.saveBitmapAsPng;
import static com.hy.mipaycard.Utils.CardList.getCardName;
import static com.hy.mipaycard.Utils.cmdUtil.getMiWallet;
import static com.hy.mipaycard.Utils.cmdUtil.getTsmclient;
import static com.hy.mipaycard.Utils.cmdUtil.isRooted;
import static com.hy.mipaycard.Utils.cmdUtil.runRootShell;
import static com.hy.mipaycard.shortcuts.SetMenuPermissionActivity.onlyRead;

public class SetCardNewActivity extends AppCompatActivity {
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private boolean isSetMipay;
    private List<List_card> cardList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_card_new);
        pref = PreferenceManager.getDefaultSharedPreferences(SetCardNewActivity.this);
        final Intent intent = getIntent();
        String filePath = intent.getStringExtra(Config.file_Path);
        final boolean isAuto = intent.getBooleanExtra(Config.is_Auto,false);
        isSetMipay = pref.getInt("isUseNew",defaultSet) >= 0;
        CardList.initLocalCardList(SetCardNewActivity.this);
        if (!new File(filePath).exists()) {
            Toast.makeText(SetCardNewActivity.this, "图片不存在", Toast.LENGTH_LONG).show();
            finish();
        }
        String[] list;
        File file;
        if(isSetMipay) {
            list = getTsmclient();
            file = new File(getExternalCache(this),"mi_pay");
        } else {
            list = getMiWallet();
            file = new File(getExternalCache(this),"mi_wallet");
        }
        if(!file.exists()){
            file.mkdirs();
        }
        List<String> cmdList = new ArrayList<String>();
        for (int i =0;i<list.length;i++){
            File f = new File(file,list[i]);
            if(!f.exists())
                cmdList.add("cp "+(isSetMipay?pay_pic:mi_wallet)+"/"+list[i]+" "+f.getPath());
        }
        String log = runRootShell(cmdList.toArray(new String[cmdList.size()]));
        delTmpFile(file,list);
        final File[] files_list = file.listFiles();
        if (files_list != null) {
            for(int i = 0;i<files_list.length;i++){
                cardList.add(new List_card((isSetMipay?getCardName(files_list[i].getName()):files_list[i].getName()),files_list[i]));
            }
        }
        ArrayAdapter adapter = new List_Adapter(this, R.layout.list_item,cardList);
        ListView listView = findViewById(R.id.list_view);
        listView.setAdapter(adapter);
        if(CardList.checkChinese(filePath)){
            File tmp_file = getTempFile(SetCardNewActivity.this);
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
                if(isSetMipay && isAuto && CardList.checkChinese(cardName)){
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
                    File file = getTempFile(SetCardNewActivity.this);
                    Bitmap bmpBackground = PhotoUtils.getBitmapFromUri(Uri.fromFile(files_list[position]), SetCardNewActivity.this);
                    if(!cardName.contains("门禁卡")) {
                        bmpBackground = BitmapUtils.mergeBitmap(bmpBackground, bitmap);
                    }
                    bmpBackground = AddTextToBitmap(bmpBackground,name);
                    saveBitmapAsPng(bmpBackground, file);
                    tempPath = file.getPath();
                } else {
                    tempPath = finalPath;
                }

                String filePath = (isSetMipay? pay_pic : mi_wallet)+"/"+files_list[position].getName();
                String[] cmd ;
                if (pref.getBoolean("setAuto",false)&&isSetMipay){
                    //TOdo
                    cmd = new String[]{"cp "+ tempPath +" "+filePath,"chmod 444 "+filePath , onlyRead };
                } else {
                    cmd = new String[]{"cp "+ tempPath +" "+filePath,"chmod 444 "+filePath };
                }
                runRootShell(cmd);
                Toast.makeText(SetCardNewActivity.this,"已替换",Toast.LENGTH_LONG).show();
                        /*File tmpFile = getTempFile(SetCardActivity.this);
                        if (tempPath.equals(tmpFile.getPath())){
                            tmpFile.delete();
                        }*/
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
                runRootShell(new String[]{"rm -rf "+(isSetMipay? pay_pic : mi_wallet)+"/"+cardList.get(menuInfo.position).getImageFile().getName()});
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()) {
            case R.id.menu_set_pkill:
                boolean isChecked = !pref.getBoolean("pkill",false);
                //Toast.makeText(this,"checked="+isChecked,Toast.LENGTH_LONG).show();
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
        menu.findItem(R.id.menu_set_pkill).setChecked(pref.getBoolean("pkill",false));
        return true;
    }

    public static void pKillServer(SharedPreferences pref){
        boolean isSetMiWallet = pref.getInt("isUseNew",defaultSet)<0;
        boolean isChecked = pref.getBoolean("pkill",false);
        if(isChecked){
            runRootShell(new String[]{"pkill -f "+(isSetMiWallet?"com.mipay.wallet":"com.miui.tsmclient")});
        }
    }
}
