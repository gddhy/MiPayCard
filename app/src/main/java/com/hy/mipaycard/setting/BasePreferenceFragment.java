package com.hy.mipaycard.setting;

import android.annotation.SuppressLint;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceGroup;
import androidx.preference.PreferenceGroupAdapter;
import androidx.preference.PreferenceScreen;
import androidx.recyclerview.widget.RecyclerView;

/**
 * 隐藏 PreferenceFragment 无图标的空白
 * 方法来自 https://blog.csdn.net/csdn0lan/article/details/88760088
 */

public abstract class BasePreferenceFragment extends PreferenceFragmentCompat {


    private void setAllPreferencesToAvoidHavingExtraSpace(Preference preference) {
        preference.setIconSpaceReserved(false);
        if (preference instanceof PreferenceGroup)
            for(int i=0;i<((PreferenceGroup) preference).getPreferenceCount();i++){
                setAllPreferencesToAvoidHavingExtraSpace(((PreferenceGroup) preference).getPreference(i));
            }
    }

    @Override
    public void  setPreferenceScreen(PreferenceScreen preferenceScreen) {
        if (preferenceScreen != null)
            setAllPreferencesToAvoidHavingExtraSpace(preferenceScreen);
        super.setPreferenceScreen(preferenceScreen);

    }

    @SuppressLint("RestrictedApi")
    @Override
    protected RecyclerView.Adapter onCreateAdapter(PreferenceScreen preferenceScreen) {
        return new PreferenceGroupAdapter(preferenceScreen){
            @SuppressLint("RestrictedApi")
            @Override
            public void onPreferenceHierarchyChange(Preference preference) {
                if(null!=preference){
                    setAllPreferencesToAvoidHavingExtraSpace(preference);
                }
                super.onPreferenceHierarchyChange(preference);
            }
        };
    }
}
