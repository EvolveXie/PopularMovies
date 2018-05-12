package com.evolvexie.popularmovies.utils;

import android.util.Log;

import com.evolvexie.popularmovies.model.Movie;
import com.evolvexie.popularmovies.model.PopularMovie;

import java.io.IOException;
import java.net.URL;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static android.content.ContentValues.TAG;

/**
 * Created by hand on 2018/3/26.
 */

public class NetUtils {

    public static String get(String url){
        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();
        Response response = null;
        try {
            response = okHttpClient.newCall(request).execute();
            if (response.message().equals("OK")) {
                String jsonStr = response.body().string();
                Log.d(TAG, "get Json: successful");
                return jsonStr;
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "get method ERROR: ",e);
            return null;
        }
        return null;
    }
}
