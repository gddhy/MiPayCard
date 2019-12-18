package com.hy.mipaycard;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Toast;

import com.by_syk.lib.uri.UriAnalyser;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.hy.mipaycard.Utils.DataCleanManager;
import com.hy.mipaycard.online_card.EmailOnlineActivity;
import com.hy.mipaycard.online_card.OnlineCardActivity;
import com.hy.mipaycard.shortcuts.CardDefaultActivity;
import com.hy.mipaycard.shortcuts.SetMenuPermissionActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.hy.mipaycard.Config.fileWork;
import static com.hy.mipaycard.EmailActivity.joinQQGroup;
import static com.hy.mipaycard.MainUtils.getCard;
import static com.hy.mipaycard.MainUtils.getTsm;
import static com.hy.mipaycard.MainUtils.initOther;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        pref = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        Toolbar toolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        initOther(MainActivity.this);
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
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{ Manifest.permission. WRITE_EXTERNAL_STORAGE }, 2);
                } else {
                    getCard(MainActivity.this);
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
        //参照https://blog.csdn.net/look_Future/article/details/79672760
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
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{ Manifest.permission. WRITE_EXTERNAL_STORAGE }, 1);
        } else {
            openAlbum();
        }
    }

    private void initCardList(){
        cardList.clear();
        cardList.add(new Card("招行初音卡",new File(getFilesDir(),"miku.png")));
        cardList.add(new Card("天依柠檬卡",new File(getFilesDir(),"luotianyi.png")));
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{ Manifest.permission. WRITE_EXTERNAL_STORAGE }, 3);
        } else {
            if (!fileWork.exists()) {
                fileWork.mkdirs();
            }
            String[] userList = fileWork.list();
            if (userList != null && userList.length != 0) {
                for (int i = 0; i < userList.length; i++) {
                    cardList.add(new Card(new File(fileWork, userList[i])));
                }
            }
        }
    }

    private void openAlbum(){
        Intent intent = new Intent("android.intent.action.GET_CONTENT");
        intent.setType("image/*");
        startActivityForResult(intent,0);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK ) {
            switch (requestCode) {
                case 0:
                    Uri uri = data.getData();
                    Intent i = new Intent(MainActivity.this, BitmapCropActivity.class);
                    i.putExtra(Config.open_Crop, UriAnalyser.getRealPath(this, uri));
                    startActivity(i);
                    break;
                default:
            }
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
                final String[] items = {"默认","新-MiPay","新-小米钱包"};
                new AlertDialog.Builder(this)
                        .setTitle("选择设置方式")
                        .setItems(items, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                editor = pref.edit();
                                editor.putInt("isUseNew",i);
                                editor.apply();
                                Toast.makeText(MainActivity.this,"已选择："+items[i],Toast.LENGTH_LONG).show();
                            }
                        })
                        .setPositiveButton("说明", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                new AlertDialog.Builder(MainActivity.this)
                                        .setTitle("说明")
                                        .setMessage(".修改小米钱包卡面为实验性功能（仅支持小米钱包1.0），请仔细对着图片按需修改\n.卡面修改界面长按对应条目可恢复默认卡面\n.用户自行修改造成的设备问题与软件开发者无关")
                                        .setPositiveButton("知道了",null)
                                        .show();
                            }
                        })
                        .show();
                break;
            case R.id.main_menu_online_card:
                openOnlineCard();
                break;
            case 6:
                openBrowser(MainActivity.this,"https://github.com/gddhy/MiPayCard/blob/master/README.md",0xff24292d,false);
                break;
            case 7:
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{ Manifest.permission. WRITE_EXTERNAL_STORAGE }, 4);
                } else {
                    startActivity(new Intent(MainActivity.this, RoundImageActivity.class));
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
        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));
    }
}
