package com.evolvexie.popularmovies.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

/**
 * Created by hand on 2018/3/22.
 */

public class MoviesProvider extends ContentProvider {

    private static final int MOVIES = 100;
    private static final int MOVIES_WITH_ID = 101;
    private static final int MOVIES_WITH_PAGE = 102;

    private static final UriMatcher sUriMatcher = buildUriMatch();
    private Context mContext;
    private MoviesDbHelper mMoviesDbHelper;

    private static final UriMatcher buildUriMatch() {
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(MoviesContract.CONTENT_AUTHORITY, MoviesContract.MOVIES_PATH, MOVIES);
        uriMatcher.addURI(MoviesContract.CONTENT_AUTHORITY, MoviesContract.MOVIES_PATH + "/#", MOVIES_WITH_ID);
        uriMatcher.addURI(MoviesContract.CONTENT_AUTHORITY, MoviesContract.MOVIES_PATH + "/" + MoviesContract.MOVIES_PAGE_PATH + "/#", MOVIES_WITH_PAGE);
        return uriMatcher;
    }

    @Override
    public boolean onCreate() {
        mContext = getContext();
        mMoviesDbHelper = new MoviesDbHelper(mContext);
        return true;
    }

    @Override
    public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] values) {
        final SQLiteDatabase db = mMoviesDbHelper.getWritableDatabase();
        switch (sUriMatcher.match(uri)) {
            case MOVIES:
                db.beginTransaction();
                int insertCount = 0;
                try {
                    for (ContentValues value : values) {
                        Uri queryUri = MoviesContract.MoviesEntry.CONTENT_URI.buildUpon()
                                .appendPath(value.getAsString(MoviesContract.MoviesEntry.COLUMN_MOVIE_ID))
                                .build();
                        Cursor cursor = query(queryUri,
                                new String[]{MoviesContract.MoviesEntry.COLUMN_IS_FAVOURITE},
                                null,
                                null,
                                null);
                        if (cursor.moveToNext()){
                            // 上面只查询了是否收藏一个字段，所以这里index为0
                            value.put(MoviesContract.MoviesEntry.COLUMN_IS_FAVOURITE,cursor.getString(0));
                        }
                        long rowId = db.insert(MoviesContract.MoviesEntry.TABLE_NAME,
                                null,
                                value);
                        if (rowId != -1) {
                            insertCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                if (insertCount > 0) {
                    mContext.getContentResolver().notifyChange(uri, null);
                }
                return insertCount;
            default:
                return super.bulkInsert(uri, values);
        }

    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        Cursor cursor = null;
        switch (sUriMatcher.match(uri)) {
            case MOVIES:
                cursor = mMoviesDbHelper.getWritableDatabase().query(MoviesContract.MoviesEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            case MOVIES_WITH_ID:
                String movieIdStr = uri.getLastPathSegment();
                cursor = mMoviesDbHelper.getWritableDatabase().query(MoviesContract.MoviesEntry.TABLE_NAME,
                        projection,
                        MoviesContract.MoviesEntry.COLUMN_MOVIE_ID + " = ?",
                        new String[]{movieIdStr},
                        null,
                        null,
                        sortOrder);
                break;
            case MOVIES_WITH_PAGE:
                String page = uri.getLastPathSegment();
                if (!TextUtils.isDigitsOnly(page)) {
                    page = "0";
                }
                int offset = MoviesContract.PAGE_SIZE * (Integer.parseInt(page) - 1);
                String limit = offset + "," + MoviesContract.PAGE_SIZE;
                cursor = mMoviesDbHelper.getWritableDatabase().query(MoviesContract.MoviesEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder,
                        limit);
                break;
            default:
                throw new UnsupportedOperationException("un supported uri : " + uri);
        }
        cursor.setNotificationUri(mContext.getContentResolver(), uri);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(CommonPreferences.SETTING_PREF_NAME, Context.MODE_PRIVATE);
        String sortingWay = sharedPreferences.getString(CommonPreferences.SORTING_WAY, "popular");
        switch (sUriMatcher.match(uri)) {
            case MOVIES:
                int deleteCount = mMoviesDbHelper.getWritableDatabase().delete(MoviesContract.MoviesEntry.TABLE_NAME,
                        MoviesContract.MoviesEntry.COLUMN_SORTING_WAR + " = ?",
                        new String[]{sortingWay});
                mContext.getContentResolver().notifyChange(uri, null);
                return deleteCount;
            default:
                throw new UnsupportedOperationException("Unknown uri:" + uri);
        }
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        switch (sUriMatcher.match(uri)) {
            case MOVIES_WITH_ID:
                int updateCount = mMoviesDbHelper.getWritableDatabase().update(MoviesContract.MoviesEntry.TABLE_NAME,
                        values,
                        MoviesContract.MoviesEntry.COLUMN_MOVIE_ID + " = ?",
                        new String[]{values.getAsString(MoviesContract.MoviesEntry.COLUMN_MOVIE_ID)});
                mContext.getContentResolver().notifyChange(uri, null);
                return updateCount;
            default:
                throw new UnsupportedOperationException("Unknown uri:" + uri);
        }
    }
}
