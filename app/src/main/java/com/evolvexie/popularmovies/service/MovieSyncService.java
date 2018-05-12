package com.evolvexie.popularmovies.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.evolvexie.popularmovies.adapter.MovieSyncAdapter;

/**
 * 该Service 返回一个IBinder 对象给 SyncAdapter类，
 * 以让框架调用 onPerformSync() 方法并将数据传递给它
 */
public class MovieSyncService extends Service {

    private static final String TAG = MovieSyncService.class.getSimpleName();
    private static MovieSyncAdapter mMovieSyncAdapter = null;
    private static final Object sSyncAdapterLock = new Object();

    @Override
    public void onCreate() {
        Log.i(TAG, "onCreate: $$$$$$$$$$$$$$$$MovieSyncService created");
        synchronized (sSyncAdapterLock){
            if (mMovieSyncAdapter == null) {
                mMovieSyncAdapter = new MovieSyncAdapter(getApplicationContext(),true);
            }
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mMovieSyncAdapter.getSyncAdapterBinder();
    }
}
