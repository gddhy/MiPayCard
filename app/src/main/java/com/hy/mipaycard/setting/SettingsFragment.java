package com.hy.mipaycard.setting;

import static com.hy.mipaycard.MainUtils.getMiWalletVersion;
import static com.hy.mipaycard.MainUtils.toSelfSetting;
import static com.hy.mipaycard.Utils.DataCleanManager.cleanOnlineCache;
import static com.hy.mipaycard.Utils.DataCleanManager.cleanOtherCache;
import static com.hy.mipaycard.Utils.DataCleanManager.getExternalCacheSize;
import static com.hy.mipaycard.Utils.DataCleanManager.getOnlineCacheSize;
import static com.hy.mipaycard.WebBrowserActivity.openBrowser;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.preference.ListPreference;
import androidx.preference.Preference;

import com.hy.mipaycard.R;
import com.hy.mipaycard.shortcuts.CardDefaultActivity;
import com.hy.mipaycard.shortcuts.LauncherShortcut;
import com.hy.mipaycard.shortcuts.SetMenuPermissionActivity;

public class SettingsFragment extends BasePreferenceFragment {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        getPreferenceManager().setSharedPreferencesName("set");
        setPreferencesFromResource(R.xml.root_preferences, rootKey);
        new PrefListener("online_type_key");
        new PrefListener("set_type_key");
        new PrefListener("set_type_pay_key");
        findPreference("PreventReplacement").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(getContext(), SetMenuPermissionActivity.class);
                startActivity(intent);
                return true;
            }
        });
        findPreference("CardReset").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(getContext(), CardDefaultActivity.class);
                startActivity(intent);
                return true;
            }
        });

        int version = getMiWalletVersion(getContext());
        if(version<2){
            findPreference("set_type_key").setVisible(false);
            findPreference("set_type_pay_key").setVisible(true);
        }

        findPreference("shortcuts_mipay").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                LauncherShortcut.addMiPay(getContext());
                return true;
            }
        });
        findPreference("shortcut_online").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                LauncherShortcut.addOnlineCard(getContext());
                return true;
            }
        });
        findPreference("ShowAbout").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                new AlertDialog.Builder(getContext())
                        .setTitle("说明")
                        .setMessage((version<2?".修改小米钱包卡面为实验性功能（仅支持小米钱包1.0），请仔细对着图片按需修改\n":"")+".卡面修改界面长按对应条目可恢复默认卡面\n.用户自行修改造成的设备问题与软件开发者无关")
                        .setPositiveButton("知道了", null)
                        .show();
                return true;
            }
        });

        Preference cacheOnline = findPreference("cacheOnline");
        Preference cacheOther = findPreference("cacheOther");
        cacheOnline.setSummary("已使用："+getOnlineCacheSize(getContext()));
        cacheOther.setSummary("已使用："+getExternalCacheSize(getContext()));
        cacheOnline.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                new AlertDialog.Builder(getContext())
                        .setTitle("清理缓存")
                        .setMessage("在线卡面会缓存原始图片，清理后需要重新联网加载，确定清理吗？")
                        .setPositiveButton("清理", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                cleanOnlineCache(getContext());
                                cacheOnline.setSummary("已使用："+getOnlineCacheSize(getContext()));
                            }
                        })
                        .setNegativeButton("取消",null)
                        .show();
                return true;
            }
        });
        cacheOther.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                new AlertDialog.Builder(getContext())
                        .setTitle("清理缓存")
                        .setMessage("确定清理其他缓存吗？")
                        .setPositiveButton("清理", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                cleanOtherCache(getContext());
                                cacheOther.setSummary("已使用："+getExternalCacheSize(getContext()));
                            }
                        })
                        .setNegativeButton("取消",null)
                        .show();
                return true;
            }
        });
        findPreference("systemSettings").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                toSelfSetting(getContext());
                return true;
            }
        });
        findPreference("yhxy").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                openBrowser(getContext(),"https://gddhy.net/mipaycard/license",0xff24292d,true);
                return true;
            }
        });
        findPreference("yszc").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                openBrowser(getContext(),"https://gddhy.net/mipaycard/privacy",0xff24292d,true);
                return true;
            }
        });

        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.R){
            findPreference("tips11").setVisible(true);
        }
    }

    public class PrefListener implements Preference.OnPreferenceChangeListener {
        private String key = null;

        public PrefListener(String key) {
            super();
            Preference preference =  findPreference(key);
            this.key = key ;

            if (ListPreference.class.isInstance(preference)) {
                ListPreference listPreference = (ListPreference) preference;
                onPreferenceChange(preference, listPreference.getValue());
            }
            preference.setOnPreferenceChangeListener(this);
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            String[] keySummary = new String[0];
            int keyValue = Integer.parseInt(newValue.toString());
            SharedPreferences pref = getContext().getSharedPreferences("set", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = pref.edit();
            if(key.equals("online_type_key")){
                keySummary = getResources().getStringArray(R.array.online_data);
                editor.putInt("onlineCardType",keyValue);
            } else if(key.contains("set_type_")) {
                keySummary = getResources().getStringArray(key.equals("set_type_key")?R.array.card_set:R.array.card_set_pay);
                editor.putInt("isUseNew",keyValue);
                if(keyValue == -1){
                    keyValue = 4;
                }
            }
            editor.apply();
            preference.setSummary(keySummary[keyValue]);
            return true;
        }
    }

}
