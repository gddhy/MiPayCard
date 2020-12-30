package com.hy.mipaycard;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.hy.mipaycard.Utils.HttpUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static com.hy.mipaycard.BaseApplication.getContext;
import static com.hy.mipaycard.Utils.cmdUtil.runRootShell;

public class TestActivity extends AppCompatActivity {
    private static final int ProgressBar_Default = 0;
    private static final int ProgressBar_Gone = -1;
    private static final int ProgressBar_Show = 1;
    private static String Action_add = "ADD_TEXT";
    private static String Action_type = "ProgressBar_TYPE";

    private LocalBroadcast localBroadcast;
    private static String LocalAction = "local_Test";
    TextView textView ;
    SharedPreferences pref;
    //弃用ProgressBar，改为SwipeRefreshLayout
    SwipeRefreshLayout swipeRefresh;
    RadioGroup radioGroup;
    ToggleButton cardButton;
    ToggleButton onlineButton;

    //有效性测试进度条
    LinearLayout test_LinearLayout;
    ProgressBar test_ProgressBar;
    TextView test_TextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        textView=findViewById(R.id.test_text_view);
        textView.setMovementMethod(ScrollingMovementMethod.getInstance());
        //progressBar = findViewById(R.id.progress_bar);
        swipeRefresh = findViewById(R.id.test_refresh);
        swipeRefresh.setEnabled(false);
        radioGroup = findViewById(R.id.test_radio_group);
        cardButton = findViewById(R.id.test_card);
        onlineButton = findViewById(R.id.test_online);
        test_LinearLayout = findViewById(R.id.test_online_card_test_view);
        test_ProgressBar = findViewById(R.id.test_online_card_test);
        test_TextView = findViewById(R.id.test_online_card_test_text);
        pref = PreferenceManager.getDefaultSharedPreferences(this);
        localBroadcast = new LocalBroadcast();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(LocalAction);
        LocalBroadcastManager.getInstance(this).registerReceiver(localBroadcast, intentFilter);
        invalidateOptionsMenu();//通知系统刷新Menu
        cardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cardButton.setChecked(true);
                onlineButton.setChecked(false);
                if(swipeRefresh.isRefreshing()){
                    Toast.makeText(TestActivity.this,"请等待当前任务执行完再操作",Toast.LENGTH_LONG).show();
                } else {
                    getData(getLink());
                }
            }
        });
        onlineButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cardButton.setChecked(false);
                onlineButton.setChecked(true);
                if(swipeRefresh.isRefreshing()){
                    Toast.makeText(TestActivity.this,"请等待当前任务执行完再操作",Toast.LENGTH_LONG).show();
                } else {
                    getData(getLink());
                }
            }
        });
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if(swipeRefresh.isRefreshing()){
                    Toast.makeText(TestActivity.this,"请等待当前任务执行完再操作",Toast.LENGTH_LONG).show();
                } else {
                    getData(getLink());
                }
            }
        });
    }

    private void addTextView(String text) {
        textView.append(text);
        textView.append("\n");
        int offset = textView.getLineCount() * textView.getLineHeight();
        if (offset > textView.getHeight()) {
            textView.scrollTo(0, offset - textView.getHeight());
        }
    }

    public static void addText(String text,int progressBarType){
        Intent intent = new Intent(LocalAction);
        intent.putExtra(Action_add,text);
        intent.putExtra(Action_type,progressBarType);
        LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);
    }

    public static void addText(String text){
        Intent intent = new Intent(LocalAction);
        intent.putExtra(Action_add,text);
        LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);
    }

    public void onTestRoot(View view) {
        addText("打开/data目录：\n"+runRootShell(new String[]{"cd /data","ls"}));
    }

    public void onTestRootData(View view) {
        addText("打开/data/data目录：\n"+runRootShell(new String[]{"cd /data/data","ls"}));
    }

    String getLink(){
        String link = "";
        switch (getCheckedId()){
            case R.id.test_link:
                link = Config.WEBSITE + "MiPayCard/";
                break;
            case R.id.test_raw:
                link = "https://raw.githubusercontent.com/gddhy/MiPayCard/master/";
                break;
            case R.id.test_cdn:
                link = "https://cdn.jsdelivr.net/gh/gddhy/MiPayCard@master/";
                break;
            default:
        }

        link = link + (onlineButton.isChecked() ? "online_card.json" : "card_list.json");

        return link;
    }

    int getCheckedId(){
        for(int i = 0;i<radioGroup.getChildCount();i++){
            RadioButton rb = (RadioButton) radioGroup.getChildAt(i);
            if(rb.isChecked()){
                return rb.getId();
            }
        }
        return 0;
    }

    public void onTestOnline(View view) {
        if(swipeRefresh.isRefreshing()){
            Toast.makeText(TestActivity.this,"请等待当前任务执行完再操作",Toast.LENGTH_LONG).show();
        } else {
            testOnlineCard();
        }
    }

    private class LocalBroadcast extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            //todo
            String text = intent.getStringExtra(Action_add);
            if(!TextUtils.isEmpty(text)&&text.length()!=0){
                addTextView(text);
            }
            int type = intent.getIntExtra(Action_type,ProgressBar_Default);
            switch (type){
                case ProgressBar_Show:
                    //progressBar.setVisibility(View.VISIBLE);
                    swipeRefresh.setRefreshing(true);
                    break;
                case ProgressBar_Gone:
                    //progressBar.setVisibility(View.GONE);
                    swipeRefresh.setRefreshing(false);
                    break;
                case ProgressBar_Default:
                default:
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(localBroadcast);
    }

    private void getData(final String url){
        addText("",ProgressBar_Show);
        final long start = System.currentTimeMillis();
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpUtil.sendOkHttpRequest(url, new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        addText(e.toString(),ProgressBar_Gone);
                        long end = System.currentTimeMillis();
                        addText("耗时："+(end - start) +"ms");
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        String data = response.body().string();
                        File file = saveJson(data);
                        addText("数据储存在："+file.getPath(),ProgressBar_Gone);
                        long end = System.currentTimeMillis();
                        addText("耗时："+(end - start) +"ms");
                        addText("正在使用mt管理器打开");
                        openFile(file);
                    }
                });

            }
        }).start();
    }

    //写入
    public static File saveJson( String data) {
        BufferedWriter writer = null;
        File file = new File(Config.getExternalCache(),"local.json");
        //如果文件不存在，则新建一个
        if(!file.exists()){
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //写入
        try {
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file,false), StandardCharsets.UTF_8));
            writer.write(data);
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

        return file;
    }

    //读取
    private static String readJson() {
        File file = new File(Config.getExternalCache(),"local.json");
        if(!file.exists()){
            return null;
        }
        BufferedReader reader = null;
        StringBuilder laststr = new StringBuilder();
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, StandardCharsets.UTF_8);
            reader = new BufferedReader(inputStreamReader);
            String tempString = null;
            while ((tempString = reader.readLine()) != null) {
                laststr.append(tempString);
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return laststr.toString();
    }

    //打开json查看
    private void openFile(File file){
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(Intent.ACTION_VIEW);
        Uri uri;
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.N){
            uri = FileProvider.getUriForFile(this,getPackageName()+".FileProvider",file);
        } else {
            uri = Uri.fromFile(file);
        }
        intent.setData(uri);
        intent.setClassName("bin.mt.plus","bin.mt.edit.TextEditor");
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);//允许临时的读
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION| Intent.FLAG_GRANT_WRITE_URI_PERMISSION);//允许临时的读和写
        try {
            startActivity(intent);
        } catch (Exception e){
            addText(e.toString());
        }
    }

    public static boolean getTestActivityType(SharedPreferences pref){
        return pref.getBoolean("isShowTestIcon",false);
    }

    public static boolean changeTestActivityIcon(SharedPreferences pref){
        boolean type = !pref.getBoolean("isShowTestIcon",false);

        getContext().getPackageManager().setComponentEnabledSetting(new ComponentName(getContext(),getContext().getPackageName()+".TestActivity"),
                type ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED : PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);

        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean("isShowTestIcon",type);
        editor.apply();

        return type;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_test,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()) {
            case R.id.menu_set_test_icon:
                changeTestActivityIcon(pref);
            default:
        }
        return true;
    }

    //刷新menu
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.menu_set_test_icon).setChecked(pref.getBoolean("isShowTestIcon",false));
        return true;
    }

    private void testOnlineCard(){
        addText("开始读取文件",ProgressBar_Show);
        test_LinearLayout.setVisibility(View.VISIBLE);
        new Thread(new Runnable() {
            @Override
            public void run() {
                String data = readJson();
                try {
                    JSONArray jsonArray = new JSONArray("["+data+"]");
                    int fail = 0;
                    test_ProgressBar.setMax(jsonArray.length());
                    for (int i = 0; i < jsonArray.length();i++){
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        final String cardName = jsonObject.getString("cardName");
                        String link = jsonObject.getString("link");
                        final String userName = jsonObject.getString("userName");
                        String about = jsonObject.getString("about");
                        String email = jsonObject.getString("email");
                        final int tmp = i;
                        runOnUiThread(new Runnable() {
                            @SuppressLint("SetTextI18n")
                            @Override
                            public void run() {
                                test_ProgressBar.setProgress(tmp);
                                test_TextView.setText(cardName + " - " + userName);
                            }
                        });

                        boolean b = isValid(link);
                        //addText(cardName+" - "+userName + " : "+(b?"有效":"失效"));
                        if(!b){
                            fail++;
                            addText(cardName + " - " + userName + " : 失效");
                        }

                    }
                    addText("请求完成，共计"+fail+"张卡面图片失效",ProgressBar_Gone);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            test_LinearLayout.setVisibility(View.GONE);
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                    addText("json解析失败\n请在本页面成功获取一次在线卡面后重试",ProgressBar_Gone);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            test_LinearLayout.setVisibility(View.GONE);
                        }
                    });
                }
            }
        }).start();
    }

    /**
     * 判断链接是否有效
     * 输入链接
     * 返回true或者false
     * https://blog.csdn.net/zheng12tian/article/details/40617075
     */
    public static boolean isValid(String strLink) {
        URL url;
        try {
            url = new URL(strLink);
            HttpURLConnection connt = (HttpURLConnection)url.openConnection();
            connt.setRequestMethod("HEAD");
            String strMessage = connt.getResponseMessage();
            if (strMessage.compareTo("Not Found") == 0) {
                return false;
            }
            connt.disconnect();
        } catch (Exception e) {
            return false;
        }
        return true;
    }

}
