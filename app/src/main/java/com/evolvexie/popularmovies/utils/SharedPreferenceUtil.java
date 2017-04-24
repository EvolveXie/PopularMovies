package com.evolvexie.popularmovies.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by hand on 2017/4/24.
 */

public class SharedPreferenceUtil {

    private static final String FILE_NAME = "com.evolvexie.popularmovies.shared_preference_setting";
    private static SharedPreferences sharedPreferences;

    public static void saveSharedPreferenceData(Context context,String key, String value){
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key,value);
        editor.apply();
    }

}
