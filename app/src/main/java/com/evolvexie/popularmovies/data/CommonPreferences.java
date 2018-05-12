package com.evolvexie.popularmovies.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by hand on 2017/4/24.
 */

public class CommonPreferences {
    public static final String SETTING_PREF_NAME = "com.evolvexie.popularmovies.setting_data";
    public static final String SORTING_WAY = "sorting_way_setting";
    // 判断用户是否是第一次进入这个app
    public static final String IS_FIRST_LOADING = "is_first_loading";
    // popular 电影榜单第一名电影id，sharedPreference的key
    public static final String BEST_POPULAR_MOVIE = "best_popular_movie";
    // rated 电影榜单第一名电影id，sharedPreference的key
    public static final String BEST_RATED_MOVIE = "best_rated_movie";

    public static String getDefaultSharedPreferenceValue(Context context,String key,String defaultValue){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getString(key,defaultValue);
    }

    public static int getDefaultSharedPreferenceIntValue(Context context,String key,String defaultValue){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String value = sharedPreferences.getString(key,defaultValue);
        return Integer.parseInt(value);
    }

    public static boolean getDefaultSharedPreferenceValue(Context context,String key,boolean defaultValue){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getBoolean(key,defaultValue);
    }

    public static void setDefaultSharedPreferenceValue(Context context,String key,String value){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPreferences.edit().putString(key,value);
    }

}
