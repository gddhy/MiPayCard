package com.hy.mipaycard.online_card;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.bumptech.glide.disklrucache.DiskLruCache;
import com.bumptech.glide.load.engine.cache.DiskCache;
import com.bumptech.glide.load.engine.cache.SafeKeyGenerator;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.signature.EmptySignature;
import com.hy.mipaycard.Config;
import com.hy.mipaycard.R;
import com.hy.mipaycard.Utils.CardList;
import com.hy.mipaycard.Utils.HttpUtil;
import com.hy.mipaycard.shortcuts.LauncherShortcut;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static com.hy.mipaycard.Config.getOnlineGitLink;
import static com.hy.mipaycard.Utils.CardList.getJsonLine;
import static com.hy.mipaycard.online_card.EmailOnlineActivity.showAbout;
import static com.hy.mipaycard.online_card.online_utils.readJsonFromFile;
import static com.hy.mipaycard.online_card.online_utils.saveJsonData;

public class OnlineCardActivity extends AppCompatActivity {
    private List<Card2> card2List = new ArrayList<>();
    private Card2Adapter adapter;
    private static String jsonData;
    private LocalBroadcast2 localBroadcast;
    private SwipeRefreshLayout swipeRefresh;
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private int onlineCardType;
    private SearchView searchView;
    private static String SearchAction = "SearchAction";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_online_card);
        pref = getSharedPreferences("set", Context.MODE_PRIVATE);
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view_online);
        Toolbar toolbar = findViewById(R.id.online_toolbar);
        setSupportActionBar(toolbar);
        onlineCardType = pref.getInt("onlineCardType",0);

        jsonData = readJsonFromFile(OnlineCardActivity.this);
        if(jsonData == null){
            jsonData = "{\"cardName\":\"miku&天依\",\"link\":\"https://s2.ax1x.com/2019/09/24/uE8Vzj.md.png\",\"userName\":\"回忆\",\"about\":\"图源pixiv，id75581429，仅裁切圆角\",\"email\":\"gddhy@foxmail.com\"}";
        }
        initCardList(jsonData);
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new Card2Adapter(card2List);
        recyclerView.setAdapter(adapter);

        /*注册广播*/
        localBroadcast = new LocalBroadcast2();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Config.localAction_online);
        LocalBroadcastManager.getInstance(OnlineCardActivity.this).registerReceiver(localBroadcast, intentFilter);

        swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_online);
        swipeRefresh.setColorSchemeColors(0xff66ccff);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getOnlineCard(true);
            }
        });

        getOnlineCard(false);
    }

    private class LocalBroadcast2 extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
                String searchText = intent.getStringExtra(SearchAction);
                try {
                    if (searchText.length()!=0) {
                        initCardList(jsonData,searchText);
                    } else {
                        initCardList(jsonData);
                    }
                } catch (Exception e){
                    initCardList(jsonData);
                }
                adapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(OnlineCardActivity.this).unregisterReceiver(localBroadcast);
    }

    private void initCardList(String jsonData){
        initCardList(jsonData,"");
    }

    private void initCardList(String jsonData,final String searchText){
        card2List.clear();
        try {
            JSONArray jsonArray = new JSONArray("["+jsonData+"]");
            for (int i = 0; i < jsonArray.length();i++){
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String cardName = jsonObject.getString("cardName");
                String link = jsonObject.getString("link");
                String userName = jsonObject.getString("userName");
                String about = jsonObject.getString("about");
                String email = jsonObject.getString("email");
                if(onlineCardType != 0){
                    String type = "";
                    String tmp = link.substring(link.lastIndexOf("/")+1);
                    if(tmp.contains(".")){
                        type = tmp.substring(tmp.lastIndexOf("."));
                    }
                    String name = cardName + " _ " +userName + type;
                    name = name.replaceAll(" ","%20");
                    try {
                        name = URLEncoder.encode(name,"UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                        continue;
                    }
                    name = name.replaceAll("%2520","%20");
                    link = getOnlineGitLink(onlineCardType == 1) + name;
                }
                if(searchText.length()==0) {
                    card2List.add(new Card2(cardName, link, userName, about, email));
                } else {
                    //不区分大小写比较
                    if(cardName.toLowerCase().contains(searchText.toLowerCase())||userName.toLowerCase().contains(searchText.toLowerCase())){
                        card2List.add(new Card2(cardName, link, userName, about, email));
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void getOnlineCard(final boolean isRef){
        final String online_link = Config.getApiLink(true,!pref.getBoolean("ApiStatus",true));
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpUtil.sendOkHttpRequest(online_link, new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(OnlineCardActivity.this, "获取失败", Toast.LENGTH_LONG).show();
                                if (isRef) {
                                    swipeRefresh.setRefreshing(false);
                                    //下拉刷新后隐藏搜索
                                    searchView.clearFocus();
                                    searchView.onActionViewCollapsed();
                                }
                                SharedPreferences.Editor editor = pref.edit();
                                editor.putBoolean("ApiStatus",false);
                                editor.apply();
                                getOnlineCard(isRef);
                            }
                        });
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        String data = response.body().string();
                        int local = getJsonLine("[" + jsonData + "]");
                        int net = getJsonLine("[" + data + "]");
                        if (net > local)
                            saveJsonData(OnlineCardActivity.this, data);
                        jsonData = data;
                        LocalBroadcastManager.getInstance(OnlineCardActivity.this).sendBroadcast(new Intent(Config.localAction_online).putExtra(SearchAction,""));//发送本地广播
                        if (isRef) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    swipeRefresh.setRefreshing(false);
                                    //下拉刷新后隐藏搜索
                                    searchView.clearFocus();
                                    searchView.onActionViewCollapsed();
                                }
                            });
                        }
                        SharedPreferences.Editor editor = pref.edit();
                        editor.putBoolean("ApiStatus",true);
                        editor.apply();
                    }
                });
            }
        }).run();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.online_toolbar,menu);
        menu.add(0, 0, 0, "在线图片说明");
        menu.add(0, 1, 1, "提交在线卡面");
        menu.add(0, 4, 4,"添加快捷方式");
        if(pref.getBoolean("showOnlineCardType",false)){
            menu.add( 0, 2, 2,"在线卡面来源");
            if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.Q || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                menu.add( 0, 3, 3, "保存在线卡面");
            }
        }
        MenuItem searchItem = menu.findItem(R.id.online_menu_search);
        searchView = (SearchView) searchItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                LocalBroadcastManager.getInstance(OnlineCardActivity.this).sendBroadcast(new Intent(Config.localAction_online).putExtra(SearchAction,query));
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                LocalBroadcastManager.getInstance(OnlineCardActivity.this).sendBroadcast(new Intent(Config.localAction_online).putExtra(SearchAction,newText));
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()) {
            case 0:
                showAbout(OnlineCardActivity.this);
                break;
            case 1:
                startActivity(new Intent(OnlineCardActivity.this, EmailOnlineActivity.class));
                break;
            case 2:
                setOnlineCardType();
                break;
            case 3:
                new AlertDialog.Builder(this)
                        .setTitle("确定保存？")
                        .setMessage("将在线卡面储存到"+new File(Config.fileWork(OnlineCardActivity.this).getParentFile(),"OnlineCard").getPath()+"\n(测试功能)")
                        .setPositiveButton("保存", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                saveOnline();
                            }
                        })
                        .setNegativeButton("取消",null)
                        .show();
                break;
            case 4:
                LauncherShortcut.addOnlineCard(this);
                break;
            default:
                break;
        }
        return true;
    }

    private void saveOnline(){
        swipeRefresh.setRefreshing(true);
        Toast.makeText(this,"保存中",Toast.LENGTH_LONG).show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                for(int i = 0;i<card2List.size();i++){
                    String type = ".png";
                    String link = card2List.get(i).getLink();
                    String cardName = card2List.get(i).getCardName();
                    String userName = card2List.get(i).getUserName();
                    String tmp = link.substring(link.lastIndexOf("/")+1);
                    if(tmp.contains(".")){
                        type = tmp.substring(tmp.lastIndexOf("."));
                    }
                    final String name = cardName + " _ " +userName + type;
                    File file = new File(Config.fileWork(OnlineCardActivity.this).getParentFile(),"OnlineCard");
                    if(!file.exists()){
                        file.mkdirs();
                    }
                    File saveFile = new File(file, name);
                    if(saveFile.exists()){
                        for(int j = 1;saveFile.exists();j++){
                            if(saveFile.getName().contains(".")){
                                saveFile = new File(saveFile.getPath().substring(0,saveFile.getPath().lastIndexOf("."))+"-"+j+type);
                            }
                        }
                    }
                    try {
                        CardList.copyFile(getGlideCacheFile(OnlineCardActivity.this, link).getPath(), saveFile.getPath());
                    } catch (Exception e){
                        e.printStackTrace();
                        Log.d("获取失败：", "link: "+link);
                    }
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(OnlineCardActivity.this,"保存完成",Toast.LENGTH_LONG).show();
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }
        }).run();
    }

    //https://github.com/HuangShengHuan/GlideCache
    public static File getGlideCacheFile(Context context ,String id) {
        DataCacheKey dataCacheKey = new DataCacheKey(new GlideUrl(id), EmptySignature.obtain());
        SafeKeyGenerator safeKeyGenerator = new SafeKeyGenerator();
        String safeKey = safeKeyGenerator.getSafeKey(dataCacheKey);
        try {
            int cacheSize = 100 * 1000 * 1000;
            DiskLruCache diskLruCache = DiskLruCache.open(new File(context.getCacheDir(), DiskCache.Factory.DEFAULT_DISK_CACHE_DIR), 1, 1, cacheSize);
            DiskLruCache.Value value = diskLruCache.get(safeKey);
            if (value != null) {
                return value.getFile(0);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
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
                setOnlineCardType();
                if(!pref.getBoolean("showOnlineCardType",false)) {
                    editor = pref.edit();
                    editor.putBoolean("showOnlineCardType", true);
                    editor.apply();
                    Toast.makeText(this, "设置入口已添加到菜单\n重新打开当前页面即可看到", Toast.LENGTH_LONG).show();
                }
            }
        }
        return super.onKeyDown(keyCode, event);
    }


    private void setOnlineCardType(){
        int choose = pref.getInt("onlineCardType",0);
        String[] items = {"图床  默认","Github CDN"};//,"直链"};
        new AlertDialog.Builder(this)
                .setTitle("选择在线卡面数据来源")
                .setSingleChoiceItems(items,choose,new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        onlineCardType = i;
                        editor = pref.edit();
                        editor.putInt("onlineCardType",i);
                        editor.apply();
                        swipeRefresh.setRefreshing(true);
                        getOnlineCard(true);
                    }
                })
                .setPositiveButton("取消",null)
                .show();
    }
}
