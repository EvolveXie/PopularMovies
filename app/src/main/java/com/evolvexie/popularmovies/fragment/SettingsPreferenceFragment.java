package com.evolvexie.popularmovies.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceGroup;

import com.evolvexie.popularmovies.BuildConfig;
import com.evolvexie.popularmovies.R;
import com.evolvexie.popularmovies.adapter.MovieSyncAdapter;
import com.evolvexie.popularmovies.data.CommonPreferences;

public class SettingsPreferenceFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener{
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.pref_settings);
        for (int i=0;i<getPreferenceScreen().getPreferenceCount();i++){
            Preference preference = getPreferenceScreen().getPreference(i);
            if (preference instanceof PreferenceGroup) {
                PreferenceGroup preferenceGroup = (PreferenceGroup) preference;
                for (int j = 0; j < preferenceGroup.getPreferenceCount(); ++j) {
                    Preference singlePref = preferenceGroup.getPreference(j);
                    updatePrefSummary(singlePref);
                }
            } else {
                updatePrefSummary(preference);
            }

        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Preference preference = findPreference(key);
        updatePrefSummary(preference);
        if (getResources().getString(R.string.pref_key_sync_frequency).equals(key)){ // 更新同步频率
            int syncFrequency = CommonPreferences.getDefaultSharedPreferenceIntValue(getContext(),
                    getResources().getString(R.string.pref_key_sync_frequency),
                    getResources().getString(R.string.pref_default_sync_frequency));
            MovieSyncAdapter.updateSyncFrequency(getContext(),syncFrequency);
        }
    }

    private void updatePrefSummary(Preference p) {
        if (p instanceof ListPreference) {
            ListPreference listPref = (ListPreference) p;
            p.setSummary(listPref.getEntry());
        }
        if (BuildConfig.LAST_TIME_SYNC_KEY.equals(p.getKey())){ // 上次同步时间
            String lastSyncTime = CommonPreferences.getSettingPreferenceDataMutiMode(getContext(),
                    BuildConfig.LAST_TIME_SYNC_KEY,"");
            String tipText = getResources().getString(R.string.pref_text_last_sync_time);
            String summary = String.format(tipText,lastSyncTime);
            p.setSummary(summary);
        }
    }
}
