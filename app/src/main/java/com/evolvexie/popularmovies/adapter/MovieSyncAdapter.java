package com.evolvexie.popularmovies.adapter;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SyncResult;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.evolvexie.popularmovies.BuildConfig;
import com.evolvexie.popularmovies.R;
import com.evolvexie.popularmovies.data.CommonPreferences;
import com.evolvexie.popularmovies.data.KeyPreferences;
import com.evolvexie.popularmovies.data.MoviesContract;
import com.evolvexie.popularmovies.model.Movie;
import com.evolvexie.popularmovies.model.PopularMovie;
import com.evolvexie.popularmovies.utils.NetUtils;
import com.evolvexie.popularmovies.utils.NotificationUtils;
import com.evolvexie.popularmovies.utils.UrlUtils;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static android.content.Context.ACCOUNT_SERVICE;

public class MovieSyncAdapter extends AbstractThreadedSyncAdapter {

    private static final String TAG = MovieSyncAdapter.class.getSimpleName();

    // Content provider authority
    public static final String AUTHORITY = "com.evolvexie.popularmovies";
    // Sync interval constants
    public static final long SECONDS_PER_MINUTE = 60L;
    public static final long SYNC_INTERVAL_IN_MINUTES = 60L;
    public static final long SYNC_INTERVAL_IN_HOURS = 1L;
    public static final long SYNC_INTERVAL =
            SYNC_INTERVAL_IN_HOURS *
                    SYNC_INTERVAL_IN_MINUTES *
                    SECONDS_PER_MINUTE;

    private ContentResolver mContentResolver;
    private Context mContext;

    public MovieSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        mContext = context;
        mContentResolver = context.getContentResolver();
    }

    public MovieSyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
        mContext = context;
        mContentResolver = context.getContentResolver();
    }

    /**
     * 数据传输实现
     * @param account 服务器端所需要的账户对象，这里我们不需要
     * @param extras 一个 Bundle 对象，它包含了一些标识，这些标识由触发 Sync Adapter 的事件所发送。
     * @param authority 应用中的 Content Provider 的 Authority
     * @param provider 由 Authority 参数所指向的Content Provider
     * @param syncResult 一个 SyncResult 对象，我们可以使用它将信息发送给 Sync Adapter 框架。
     */
    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        // 保存同步时间
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
        String nowTime = format.format(date);
        CommonPreferences.saveSettingPreferenceDataMutiMode(mContext, BuildConfig.LAST_TIME_SYNC_KEY,nowTime);
        new SyncPopularThread().start();
        new SyncRatedThread().start();
    }

    /**
     * 执行SyncAdapter
     * @param context 上下文
     * @param account 虚拟账户(getAccount方法获取)
     */
    public static void performSync(Context context,Account account){
        Bundle b = new Bundle();
        b.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        b.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        ContentResolver.requestSync(account,AUTHORITY,b);
    }

    /**
     * 获取虚拟账号方法，第一次创建账户时，返回null
     * @param context
     * @return
     */
    public static Account getAccount(Context context) {
        // Create the account type and default account
        String accountName = context.getResources().getString(R.string.account_name);
        String accountType = context.getResources().getString(R.string.account_type);
        Account newAccount = new Account(
                accountName, accountType);
        // Get an instance of the Android account manager
        AccountManager accountManager =
                (AccountManager) context.getSystemService(
                        ACCOUNT_SERVICE);
        /*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */
        if (accountManager.addAccountExplicitly(newAccount, null, null)) {
            ContentResolver.setIsSyncable(newAccount,AUTHORITY,1);
            ContentResolver.setSyncAutomatically(newAccount,AUTHORITY,true);
            int syncFrequency = CommonPreferences.getDefaultSharedPreferenceIntValue(context,
                    context.getResources().getString(R.string.pref_key_sync_frequency),
                    context.getResources().getString(R.string.pref_default_sync_frequency));
            ContentResolver.addPeriodicSync(newAccount,
                    AUTHORITY,
                    new Bundle(),
                    SYNC_INTERVAL * syncFrequency);

            performSync(context,newAccount);
            // 第一次创建账户时会自动执行一次onPerformSync，返回null，
            // MainActivity初则根据返回值不为null，调用performSync
            return null;
        }
        return newAccount;
    }

    /**
     * 更新同步频率
     * @param context
     * @param syncFrequency 同步频率（syncFrequency/小时）
     */
    public static void updateSyncFrequency(Context context,int syncFrequency){
        String accountName = context.getResources().getString(R.string.account_name);
        String accountType = context.getResources().getString(R.string.account_type);
        Account account = new Account(
                accountName, accountType);

        ContentResolver.addPeriodicSync(account,
                AUTHORITY,
                new Bundle(),
                SYNC_INTERVAL * syncFrequency);
    }

    public void syncData(String sortingWay){
        int currentPage = 0;
        String baseUrl = "";
        String bestMovieKey = "";
        String notificationContent = "";
        int intentId = NotificationUtils.FIRST_POPULAR_CHANGE_NOTIFY_INTENT_ID;
        if (sortingWay.equals("popular")){
            baseUrl = UrlUtils.GET_POPULAR.replace("API_KEY", KeyPreferences.API_KEY);
            bestMovieKey = CommonPreferences.BEST_POPULAR_MOVIE;
            intentId = NotificationUtils.FIRST_POPULAR_CHANGE_NOTIFY_INTENT_ID;
            notificationContent = mContext.getResources().getString(R.string.notification_content_popular);
        }else if (sortingWay.equals("rated")){
            baseUrl = UrlUtils.GET_TOP_RATED.replace("API_KEY", KeyPreferences.API_KEY);
            bestMovieKey = CommonPreferences.BEST_RATED_MOVIE;
            intentId = NotificationUtils.FIRST_RATED_CHANGE_NOTIFY_INTENT_ID;
            notificationContent = mContext.getResources().getString(R.string.notification_content_rated);
        }
        for (int i = 0; i < BuildConfig.SYNC_MAX_PAGE; i++) {
            String url = baseUrl.replace("PAGE", String.valueOf(++currentPage));
            String jsonResult = NetUtils.get(url);
            PopularMovie popularMovie = new Gson().fromJson(jsonResult, PopularMovie.class);
            List<Movie> movies = popularMovie != null ? popularMovie.getResults() : null;
            if (movies != null) {
                ContentValues[] contentValues = new ContentValues[movies.size()];
                int j = 0;
                for (Movie movie : movies) {
                    String movieId = String.valueOf(movie.getId());
                    if (currentPage == 1 && j == 0) {
                        String bestMovieId = CommonPreferences.getSettingPreferenceDataMutiMode(getContext(),
                                bestMovieKey,
                                "");
                        if ("".equals(bestMovieId) || !bestMovieId.equals(movieId)){ // 第一次需要写入，或者第一名变更
                            CommonPreferences.saveSettingPreferenceDataMutiMode(getContext(),
                                    bestMovieKey,
                                    String.valueOf(movieId));
                            if (!bestMovieId.equals(movieId) && !"".equals(bestMovieId)) { // 发送通知
                                if (NotificationUtils.isNotify(getContext())){
                                    String content = mContext.getResources().getString(R.string.notification_content_first_change);
                                    content = String.format(content,
                                            notificationContent,
                                            movie.getTitle());
                                    NotificationUtils.createAndSendFirstChangeNotification(getContext(),content,intentId);
                                }
                            }
                        }
                    }

                    ContentValues values = new ContentValues();
                    values.put(MoviesContract.MoviesEntry.COLUMN_MOVIE_ID, movie.getId());
                    values.put(MoviesContract.MoviesEntry.COLUMN_TITLE, movie.getTitle());
                    values.put(MoviesContract.MoviesEntry.COLUMN_BACKDROP_PATH, movie.getBackdropPath());
                    values.put(MoviesContract.MoviesEntry.COLUMN_ORIGINAL_TITLE, movie.getOriginalTitle());
                    values.put(MoviesContract.MoviesEntry.COLUMN_OVERVIEW, movie.getOverview());
                    values.put(MoviesContract.MoviesEntry.COLUMN_POPULARITY, movie.getPopularity());
                    values.put(MoviesContract.MoviesEntry.COLUMN_POSTER_PATH, movie.getPosterPath());
                    values.put(MoviesContract.MoviesEntry.COLUMN_RELEASE_DATE, movie.getReleaseDate());
                    values.put(MoviesContract.MoviesEntry.COLUMN_VOTE_AVERAGE, movie.getVoteAverage());
                    values.put(MoviesContract.MoviesEntry.COLUMN_VOTE_COUNT, movie.getVoteCount());
                    values.put(MoviesContract.MoviesEntry.COLUMN_CREATE_TIME, String.valueOf(new Date()));
                    values.put(MoviesContract.MoviesEntry.COLUMN_SORTING_WAR, sortingWay);
                    contentValues[j] = values;
                    j++;
                }
                Uri uri = MoviesContract.MoviesEntry.CONTENT_URI;
                getContext().getContentResolver().bulkInsert(uri, contentValues);
                Log.d(TAG, "Sync "+sortingWay+" movie with page " + currentPage + " successful");
            }else {
                Log.d(TAG, "Sync "+sortingWay+" movie with page " + currentPage + " failed");
            }
        }
        Log.i(TAG, "Sync "+sortingWay+" movie complete");
    }

    private class SyncPopularThread extends Thread {

        @Override
        public void run() {
            syncData("popular");
        }
    }


    private class SyncRatedThread extends Thread {

        @Override
        public void run() {
            syncData("rated");
        }
    }


}
