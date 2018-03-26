package com.evolvexie.popularmovies.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by hand on 2018/3/22.
 */

public class MoviesDbHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 2;

    public static final String DATABASE_NAME = "popular_movies.db";

    public MoviesDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String CREATE_POPULAR_MIVIES_TABLE =
                "CREATE TABLE " + MoviesContract.MoviesEntry.TABLE_NAME + " ("
                + MoviesContract.MoviesEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + MoviesContract.MoviesEntry.COLUMN_MOVIE_ID + " INTEGER NOT NULL, "
                + MoviesContract.MoviesEntry.COLUMN_TITLE + " TEXT NOT NULL, "
                + MoviesContract.MoviesEntry.COLUMN_OVERVIEW + " TEXT NOT NULL, "
                + MoviesContract.MoviesEntry.COLUMN_POSTER_PATH + " TEXT NOT NULL, "
                + MoviesContract.MoviesEntry.COLUMN_VOTE_AVERAGE + " TEXT NOT NULL, "
                + MoviesContract.MoviesEntry.COLUMN_VOTE_COUNT + " TEXT, "
                + MoviesContract.MoviesEntry.COLUMN_ADULT + " TEXT, "
                + MoviesContract.MoviesEntry.COLUMN_BACKDROP_PATH + " TEXT NOT NULL, "
                + MoviesContract.MoviesEntry.COLUMN_ORIGINAL_TITLE + " TEXT, "
                + MoviesContract.MoviesEntry.COLUMN_POPULARITY + " REAL, "
                + MoviesContract.MoviesEntry.COLUMN_RELEASE_DATE + " REAL, "
                + MoviesContract.MoviesEntry.COLUMN_CREATE_TIME + " REAL, "
                + MoviesContract.MoviesEntry.COLUMN_SORTING_WAR + " TEXT,"
                + " UNIQUE ("+ MoviesContract.MoviesEntry.COLUMN_MOVIE_ID+") ON CONFLICT REPLACE);";

        db.execSQL(CREATE_POPULAR_MIVIES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + MoviesContract.MoviesEntry.TABLE_NAME);
        onCreate(db);
    }
}
