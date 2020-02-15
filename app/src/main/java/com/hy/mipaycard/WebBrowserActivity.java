package com.hy.mipaycard;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

public class WebBrowserActivity extends AppCompatActivity {
    WebView webView;
    ProgressBar progressBar;
    String url;
    boolean isOpenInWebView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_browser);
        Intent intent = getIntent();
        int color = intent.getIntExtra("StatusBarColor",R.color.colorPrimaryDark);
        url = intent.getStringExtra("URL");
        isOpenInWebView = intent.getBooleanExtra("OpenInWebView",false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(color);
        }
        initView();
        webView.loadUrl(url);
    }

    private void initView() {
        webView = findViewById(R.id.webview);
        progressBar = findViewById(R.id.progressBar);

        WebSettings webSettings = webView.getSettings();
        //如果访问的页面中要与Javascript交互，则webview必须设置支持Javascript
        webSettings.setJavaScriptEnabled(true);
        //网页在app内打开
        if(isOpenInWebView) {
            webView.setWebViewClient(new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    view.loadUrl(url);
                    return true;
                }
            });
        }
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress == 100) {
                    progressBar.setVisibility(View.GONE);//加载完网页进度条消失
                } else {
                    progressBar.setVisibility(View.VISIBLE);//开始加载网页时显示进度条
                    progressBar.setProgress(newProgress);//设置进度值
                }
                super.onProgressChanged(view, newProgress);
            }
        });
    }//https://blog.csdn.net/qq_23491413/article/details/80017201


    /**
     * 实现按下源生返回键，返回到上一个网页的方法，直接复制即可，
     * 此方法为监听返回按键时的处理
     **/
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //监听到返回键被按下，并且当前网页可被返回
        if (KeyEvent.KEYCODE_BACK == keyCode && webView.canGoBack()) {
            //获取当前的网址，与初始网址界面是否相同
            if (webView.getUrl().equals(url)) {
                //相同表示为第一次进入的网址，上一级为源生
                finish();
            } else {
                //返回到网页的上一级
                webView.goBack();
                //返回true，交于系统处理
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }//https://blog.csdn.net/qq_34517710/article/details/70239934

    public static void openBrowser(Context context, String url, int statusBarColor, boolean isOpenInWebView){
        Intent intent = new Intent(context, WebBrowserActivity.class);
        intent.putExtra("URL", url);
        intent.putExtra("StatusBarColor",statusBarColor);
        intent.putExtra("OpenInWebView", isOpenInWebView);
        context.startActivity(intent);
    }
}
