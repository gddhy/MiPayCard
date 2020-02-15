package com.hy.mipaycard.online_card;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
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
import com.hy.mipaycard.Utils.HttpUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static com.hy.mipaycard.Config.git_url;
import static com.hy.mipaycard.Utils.CardList.getJsonLine;
import static com.hy.mipaycard.online_card.EmailOnlineActivity.showAbout;
import static com.hy.mipaycard.online_card.online_utils.readJsonFromFile;
import static com.hy.mipaycard.online_card.online_utils.saveJsonData;

public class OnlineCardActivity extends AppCompatActivity {
    private List<Card2> card2List = new ArrayList<>();
    private Card2Adapter adapter;
    private String jsonDara;
    private LocalBroadcast2 localBroadcast;
    private SwipeRefreshLayout swipeRefresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_online_card);
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view_online);
        Toolbar toolbar = findViewById(R.id.online_toolbar);
        setSupportActionBar(toolbar);

        jsonDara = readJsonFromFile(OnlineCardActivity.this);
        if(jsonDara == null){
            jsonDara = "{\"cardName\":\"miku&天依\",\"link\":\"https://s2.ax1x.com/2019/09/24/uE8Vzj.md.png\",\"userName\":\"回忆\",\"about\":\"图源pixiv，id75581429，仅裁切圆角\",\"email\":\"gddhy@foxmail.com\"}";
        }
        initCardList(jsonDara);
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
                initCardList(jsonDara);
                adapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(OnlineCardActivity.this).unregisterReceiver(localBroadcast);
    }

    private void initCardList(String jsonData){
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
                card2List.add(new Card2(cardName,link,userName,about,email));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void getOnlineCard(final boolean isRef){
        final String online_link = git_url+"online_card.json";
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpUtil.sendOkHttpRequest(online_link, new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(OnlineCardActivity.this,"获取失败",Toast.LENGTH_LONG).show();
                                if (isRef)
                                    swipeRefresh.setRefreshing(false);
                            }
                        });
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        String data = response.body().string();
                        int local = getJsonLine("["+jsonDara+"]");
                        int net = getJsonLine("[" + data + "]");
                        if (net > local)
                            saveJsonData(OnlineCardActivity.this, data);
                        jsonDara = data;
                        LocalBroadcastManager.getInstance(OnlineCardActivity.this).sendBroadcast(new Intent(Config.localAction_online));//发送本地广播
                        if (isRef) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    swipeRefresh.setRefreshing(false);
                                }
                            });
                        }
                    }
                });
            }
        }).run();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 0, 0, "在线图片说明");
        menu.add(0, 1, 1, "提交在线卡面");
        return true;
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
            default:
                break;
        }
        return true;
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
}
