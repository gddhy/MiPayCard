package com.hy.mipaycard;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.documentfile.provider.DocumentFile;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;

import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Toast;

import com.by_syk.lib.uri.UriAnalyser;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.hy.mipaycard.Utils.DataCleanManager;
import com.hy.mipaycard.SetCard.set_card_img_title.SetCardImgActivity;
import com.hy.mipaycard.online_card.EmailOnlineActivity;
import com.hy.mipaycard.online_card.OnlineCardActivity;
import com.hy.mipaycard.shortcuts.CardDefaultActivity;
import com.hy.mipaycard.shortcuts.LauncherShortcut;
import com.hy.mipaycard.shortcuts.SetMenuPermissionActivity;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static com.hy.mipaycard.Config.defaultSet;
import static com.hy.mipaycard.Config.fileWork;
import static com.hy.mipaycard.Config.getExternalCache;
import static com.hy.mipaycard.EmailActivity.joinQQGroup;
import static com.hy.mipaycard.MainUtils.getCard;
import static com.hy.mipaycard.MainUtils.getMiWalletVersion;
import static com.hy.mipaycard.MainUtils.getTsm;
import static com.hy.mipaycard.MainUtils.initOther;
import static com.hy.mipaycard.MainUtils.isInstallApp;
import static com.hy.mipaycard.MainUtils.showAboutDialog;
import static com.hy.mipaycard.MainUtils.toSelfSetting;
import static com.hy.mipaycard.WebBrowserActivity.openBrowser;
import static com.hy.mipaycard.shortcuts.OpenMiPayActivity.openMiPay;

public class MainActivity extends AppCompatActivity {
    private List<Card> cardList = new ArrayList<>();
    private CardAdapter adapter;
    private FloatingActionMenu fab_menu;
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private SwipeRefreshLayout swipeRefresh;

    private LocalBroadcast localBroadcast;

    final private int REQUEST_CODE_OPEN_DIRECTORY = 100;
    final private int NEW_SAF_CHOOSE_IMG = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        pref = getSharedPreferences("set", Context.MODE_PRIVATE);
        Toolbar toolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        initOther(MainActivity.this,pref);
        fab_menu = (FloatingActionMenu) findViewById(R.id.fab_menu);
        fab_menu.setClosedOnTouchOutside(true);        //点空白处关闭菜单
        initCardList();
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new CardAdapter(cardList);
        recyclerView.setAdapter(adapter);

        FloatingActionButton fab_button1 = (FloatingActionButton)findViewById(R.id.fab_exp1);
        fab_button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fab_menu.close(true);
                openMiPay(MainActivity.this);
            }
        });
        fab_button1.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                fab_menu.close(true);
                LauncherShortcut.addMiPay(MainActivity.this);
                return true;
            }
        });
        FloatingActionButton fab_button2 = (FloatingActionButton)findViewById(R.id.fab_exp2);
        fab_button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fab_menu.close(true);
                startActivity(new Intent(MainActivity.this, CardDefaultActivity.class));
            }
        });
        FloatingActionButton fab_button3 = (FloatingActionButton)findViewById(R.id.fab_exp3);
        fab_button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fab_menu.close(true);
                if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.Q){
                    getCard(MainActivity.this);
                } else {
                    if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 2);
                    } else {
                        getCard(MainActivity.this);
                    }
                }
            }
        });
        FloatingActionButton fab_button4 = (FloatingActionButton)findViewById(R.id.fab_exp4);
        fab_button4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fab_menu.close(true);
                startActivity(new Intent(MainActivity.this, SetMenuPermissionActivity.class));
            }
        });
        FloatingActionButton fab_button5 = (FloatingActionButton)findViewById(R.id.fab_online);
        fab_button5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fab_menu.close(true);
                openOnlineCard();
            }
        });
        fab_button5.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                fab_menu.close(true);
                LauncherShortcut.addOnlineCard(MainActivity.this);
                return true;
            }
        });

        swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
        swipeRefresh.setColorSchemeResources(R.color.colorLty);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshCards();
            }
        });
        getTsm(MainActivity.this);

        /*注册广播*/
        localBroadcast = new LocalBroadcast();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Config.localAction);
        LocalBroadcastManager.getInstance(MainActivity.this).registerReceiver(localBroadcast, intentFilter);
        //参照 https://blog.csdn.net/look_Future/article/details/79672760

        showDialogForR();
    }

    private class LocalBroadcast extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            initCardList();
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(MainActivity.this).unregisterReceiver(localBroadcast);//解除注册广播
    }

    public void onChoosePicClick(View view) {
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.Q){
            //todo new choose
            openAlbum(NEW_SAF_CHOOSE_IMG);
        } else {
            if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            } else {
                openAlbum();
            }
        }
    }

    private void initCardList(){
        cardList.clear();
        cardList.add(new Card("招行初音卡",new File(getFilesDir(),"miku.png")));
        cardList.add(new Card("天依柠檬卡",new File(getFilesDir(),"luotianyi.png")));
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.Q) {
            initItems();
        } else {
            if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 3);
                refSAFList();
            } else {
                initItems();
            }
        }
    }

    private void initItems(){
        if (!fileWork(MainActivity.this).exists()) {
            fileWork(MainActivity.this).mkdirs();
        }
        File[] userList = fileWork(MainActivity.this).listFiles();
        if (userList != null && userList.length != 0) {
            for (int i = 0; i < userList.length; i++) {
                if(userList[i].isFile())
                    if(CardListProvider.getTypeForName(userList[i].getName()).contains("image"))
                        cardList.add(new Card(userList[i]));
            }
        }

        refSAFList();
    }

    private void refSAFList(){
        if(hasSAFPermission()){
            SharedPreferences sharedPreferences = getSharedPreferences("data", Context.MODE_PRIVATE);
            String uriStr = sharedPreferences.getString("uri", "");
            DocumentFile documentFile = DocumentFile.fromTreeUri(this,Uri.parse(uriStr));
            try {
                DocumentFile[] documentFiles = documentFile.listFiles();
                if (documentFiles != null && documentFiles.length != 0) {
                    for (int i = 0; i < documentFiles.length; i++) {
                        if (documentFiles[i].isFile() && documentFiles[i].getType().contains("image"))
                            cardList.add(new Card(documentFiles[i]));
                    }
                }
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    private void  openAlbum(){
        openAlbum(0);
    }

    private void openAlbum(int requestCode){
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent,requestCode);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openAlbum();
                } else {
                    Toast.makeText(MainActivity.this, "缺少必要权限", Toast.LENGTH_SHORT).show();
                }
                break;
            case 2:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getCard(MainActivity.this);
                } else {
                    Toast.makeText(MainActivity.this, "缺少必要权限", Toast.LENGTH_SHORT).show();
                }
                break;
            case 3:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    LocalBroadcastManager.getInstance(MainActivity.this).sendBroadcast(new Intent(Config.localAction));//发送本地广播
                }
                break;
            case 4:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startActivity(new Intent(MainActivity.this, RoundImageActivity.class));
                } else {
                    Toast.makeText(MainActivity.this, "缺少必要权限", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
        }
    }

    @SuppressLint("WrongConstant")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //TODO
        if (resultCode == RESULT_OK ) {
            switch (requestCode) {
                case 0:
                    Uri uri = data.getData();
                    String path = UriAnalyser.getRealPath(this, uri);
                    int type = pref.getInt("isUseNew", defaultSet);
                    if(path != null) {
                        if (type != 2) {
                            Intent i = new Intent(MainActivity.this, BitmapCropActivity.class);
                            i.putExtra(Config.open_Crop, path);
                            startActivity(i);
                        } else {
                            Intent intent = new Intent(this, SetCardImgActivity.class);
                            intent.putExtra(Config.file_Path, path);
                            intent.putExtra(Config.is_Auto, false);
                            startActivity(intent);
                        }
                    } else {
                        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.N)
                            newSetCard(data);
                        else
                            Toast.makeText(this,"文件获取失败",Toast.LENGTH_LONG).show();
                    }

                    break;
                case REQUEST_CODE_OPEN_DIRECTORY:
                    Log.d("SAF", String.format("Open Directory result Uri : %s", data.getData()));
                    Uri uriTree = data.getData();
                    try {
                        SharedPreferences sharedPreferences = getSharedPreferences("data", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("uri", uriTree.toString());
                        editor.apply();
                        final int takeFlags = data.getFlags() & (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                        getContentResolver().takePersistableUriPermission(uriTree, takeFlags);
                        LocalBroadcastManager.getInstance(MainActivity.this).sendBroadcast(new Intent(Config.localAction));
                    } catch (Exception e){
                        e.printStackTrace();
                        Toast.makeText(this,"当前设备不支持",Toast.LENGTH_LONG).show();
                    }
                    //updateDatas(uriTree);
                    break;
                case NEW_SAF_CHOOSE_IMG:
                    //TODO
                    newSetCard(data);
                    break;
                default:
            }
        }
    }

    private void newSetCard(Intent data){
        try {
            String path2 = saveFileFromSAF(MainActivity.this, data.getData()).getPath();
            int type2 = pref.getInt("isUseNew", defaultSet);
            if(type2 != 2) {
                Intent i_saf = new Intent(MainActivity.this, BitmapCropActivity.class);
                i_saf.putExtra(Config.open_Crop, path2);
                startActivity(i_saf);
            } else {
                Intent intent = new Intent(this, SetCardImgActivity.class);
                intent.putExtra(Config.file_Path, path2);
                intent.putExtra(Config.is_Auto, false);
                startActivity(intent);
            }
        } catch (Exception e){
            e.printStackTrace();
            Toast.makeText(this,"文件获取失败",Toast.LENGTH_LONG).show();
        }
    }

    private void refreshCards() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(600);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        LocalBroadcastManager.getInstance(MainActivity.this).sendBroadcast(new Intent(Config.localAction));//发送本地广播
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }
        }).start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_toolbar,menu);
        menu.add(0, 0, 0, "提交卡面适配");
        menu.add(0, 1, 1, "提交在线卡面");
        menu.add(0, 2, 2,"关于");
        menu.add(0, 3, 3,"加群交流");
        menu.add(0, 4, 4, "缓存信息");
        menu.add(0, 5 , 5, "卡面设置方式");
        menu.add(0, 6, 6,"使用帮助");
        menu.add(0, 7, 7,"圆角图片");
        menu.add(0, 8, 8,"选择外部卡面目录");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()) {
            case 0:
                startActivity(new Intent(MainActivity.this,EmailActivity.class));
                break;
            case 1:
                startActivity(new Intent(MainActivity.this, EmailOnlineActivity.class));
                break;
            case 2:
                showAboutDialog(MainActivity.this);
                break;
            case 3:
                if(!joinQQGroup(MainActivity.this,"N-A6occcPxGl_v-PEHCr3Hi0XwyQXyii")){
                    Toast.makeText(MainActivity.this,"请安装或升级QQ",Toast.LENGTH_LONG).show();
                }
                break;
            case 4:
                String cacheText;
                try {
                    cacheText = DataCleanManager.getCacheSize(MainActivity.this);
                } catch (Exception e) {
                    e.printStackTrace();
                    cacheText = "获取失败";
                }
                new AlertDialog.Builder(this)
                        .setTitle("缓存信息")
                        .setMessage("当前已使用缓存："+cacheText+"\n你确定清空缓存吗?\n清理后在线图片需要重新加载")
                        .setPositiveButton("清除", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                DataCleanManager.cleanInternalCacheAll(MainActivity.this);
                                Toast.makeText(MainActivity.this, "清理成功", Toast.LENGTH_LONG).show();
                            }
                        })
                        .setNeutralButton("应用管理", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                toSelfSetting(MainActivity.this);
                            }
                        })
                        .setNegativeButton("取消", null)
                        .show();
                break;
            case R.id.main_menu_set:
            case 5:
                //todo dialog
                final int version = getMiWalletVersion(this);
                int choose ;
                final String[] items ;
                final boolean isShow;
                if( version >= 2){
                    items = new String[]{"默认","MiPay","新-MiPay"};
                    int t = pref.getInt("isUseNew", defaultSet);
                    isShow = false;
                    if(t>2){
                        choose = defaultSet;
                    } else {
                        choose = t;
                    }
                } else {
                    items = new String[]{"默认","MiPay","新-MiPay","新-小米钱包"};
                    choose = pref.getInt("isUseNew", defaultSet);
                    isShow = true;
                    if(choose == -1){
                        choose = items.length -1;
                    }
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(this)
                        .setTitle("选择设置方式")
                        .setSingleChoiceItems(items, choose
                                , new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                int tmp = i;
                                dialogInterface.dismiss();
                                if(i == items.length-1 && isShow){
                                    tmp = -1;
                                }
                                editor = pref.edit();
                                editor.putInt("isUseNew",tmp);
                                editor.apply();
                                Toast.makeText(MainActivity.this,"已选择："+items[i],Toast.LENGTH_LONG).show();
                            }
                        });
                        builder.setNegativeButton("说明", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    new AlertDialog.Builder(MainActivity.this)
                                            .setTitle("说明")
                                            .setMessage((version<2?".修改小米钱包卡面为实验性功能（仅支持小米钱包1.0），请仔细对着图片按需修改\n":"")+".卡面修改界面长按对应条目可恢复默认卡面\n.用户自行修改造成的设备问题与软件开发者无关")
                                            .setPositiveButton("知道了", null)
                                            .show();
                                }
                            });
                        builder.show();
                break;
            case R.id.main_menu_online_card:
                openOnlineCard();
                break;
            case 6:
                openBrowser(MainActivity.this,"https://github.com/gddhy/MiPayCard/blob/master/README.md",0xff24292d,false);
                break;
            case 7:
                if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.Q){
                    //TODO
                    //Toast.makeText(this,"未适配当前安卓版本",Toast.LENGTH_LONG).show();
                    startActivity(new Intent(MainActivity.this, RoundImageActivity.class));
                } else {
                    if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 4);
                    } else {
                        startActivity(new Intent(MainActivity.this, RoundImageActivity.class));
                    }
                }
                break;
            case 8:
                //TODO
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    try {
                        //部分官改ROM可能不支持
                        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                        startActivityForResult(intent, REQUEST_CODE_OPEN_DIRECTORY);
                    } catch (Exception e){
                        e.printStackTrace();
                        Toast.makeText(this,"当前设备不支持",Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(this,"该功能暂不支持当前安卓版本",Toast.LENGTH_LONG).show();
                }
                break;
            default:
                break;
        }
        return true;
    }

    private void openOnlineCard(){
        boolean b = pref.getBoolean("showToast",true);
        if (b){
            final CheckBox checkBox = new CheckBox(MainActivity.this);
            checkBox.setChecked(false);
            checkBox.setText("不再提醒");
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("流量提醒")
                    .setMessage("程序会直接联网加载图片原图，请注意流量消耗")
                    .setView(checkBox)
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            editor = pref.edit();
                            editor.putBoolean("showToast",!checkBox.isChecked());
                            editor.apply();
                            startActivity(new Intent(MainActivity.this, OnlineCardActivity.class));
                        }
                    })
                    .setNegativeButton("取消",null)
                    .show();
        } else {
            startActivity(new Intent(MainActivity.this, OnlineCardActivity.class));
        }
    }

    public static void ref_media(Context context,File file){
        if(Build.VERSION.SDK_INT<Build.VERSION_CODES.Q)
            context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));
    }


    @SuppressLint("WrongConstant")
    private boolean hasSAFPermission(){
        SharedPreferences sharedPreferences = getSharedPreferences("data", Context.MODE_PRIVATE);
        String uriStr = sharedPreferences.getString("uri", "");
        if (!TextUtils.isEmpty(uriStr)) {
            try {
                Uri uri = Uri.parse(uriStr);
                final int takeFlags = getIntent().getFlags() &
                        (Intent.FLAG_GRANT_READ_URI_PERMISSION |
                                Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                // Check for the freshest data.
                getContentResolver().takePersistableUriPermission(uri, takeFlags);
                //updateDatas(uri);
                return true;
            } catch (SecurityException e) {
                e.printStackTrace();
                return false;
            }
        } else {
            return false;
        }
    }

    public static File saveFileFromSAF(Context context,Uri safUri){
        DocumentFile documentFile = DocumentFile.fromSingleUri(context,safUri);
        File dir = new File(getExternalCache(),"SAF");
        if(!dir.exists()){
            dir.mkdirs();
        }
        try {
            File save = new File(dir,documentFile.getName());
            if(save.exists()){
                save.delete();
            }
            if(!save.exists()){
                save.createNewFile();
            }
            InputStream in  = context.getContentResolver().openInputStream(documentFile.getUri());
            FileOutputStream out = new FileOutputStream(save);
            int n = 0;// 每次读取的字节长度
            byte[] bb = new byte[1024];// 存储每次读取的内容
            while ((n = in.read(bb)) != -1) {
                out.write(bb, 0, n);// 将读取的内容，写入到输出流当中
            }
            in.close();
            out.close();
            return save;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Bitmap getBitmapFromUri(Context context, Uri uri) throws IOException {
        ParcelFileDescriptor parcelFileDescriptor =
                context.getContentResolver().openFileDescriptor(uri, "r");
        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
        Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
        parcelFileDescriptor.close();
        return image;
    }

    private void showDialogForR(){
        if(Build.VERSION.SDK_INT<Build.VERSION_CODES.R)
            return;
        boolean isShowDialogForR = pref.getBoolean("isShowDialogForR",false);
        if(isShowDialogForR)
            return;

        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle("Root权限提示")
                .setMessage(".您的设备已升级到Android11，Magisk的Root权限需要您多做一步设置，开发版Root直接使用即可\n"+
                        ".打开Magisk设置，找到\"挂载命名空间模式\"，选择\"全局命名空间\"即可")
                .setPositiveButton("知道了，不再提示", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        editor = pref.edit();
                        editor.putBoolean("isShowDialogForR",true);
                        editor.apply();
                    }
                });
        if(isInstallApp(this,"com.topjohnwu.magisk")){
            builder.setNegativeButton("打开Magisk", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Intent intent = getPackageManager().getLaunchIntentForPackage("com.topjohnwu.magisk");
                    try {
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                }
            });
        }
        builder.show();
    }

    int Ti = 0;
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN){
            Timer timer = null;
            if(Ti<2){
                Ti++;
                timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        Ti = 0;
                    }
                },6000);
            } else {
                if(!TestActivity.getTestActivityType(pref)){
                    TestActivity.changeTestActivityIcon(pref);
                }
                new AlertDialog.Builder(this)
                        .setIcon(R.mipmap.ic_launcher)
                        .setTitle("测试工具")
                        .setMessage("已添加测试工具桌面图标")
                        .setPositiveButton("打开", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Intent intent = new Intent(MainActivity.this,TestActivity.class);
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
                                }
                                intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                                startActivity(intent);
                            }
                        }).setNegativeButton("移除", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            TestActivity.changeTestActivityIcon(pref);
                        }
                    }).show();

                //Toast.makeText(this, "已"+(b?"添加":"移除")+"测试工具桌面图标", Toast.LENGTH_LONG).show();

            }
        }
        return super.onKeyDown(keyCode, event);
    }
}
