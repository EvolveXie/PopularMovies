package com.evolvexie.popularmovies.data;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by hand on 2018/3/22.
 */

public class MoviesContract {

    public static final String CONTENT_AUTHORITY = "com.evolvexie.popularmovies";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String MOVIES_PATH = "movies";
    public static final String MOVIES_PAGE_PATH = "moviePage";
    public static final int PAGE_SIZE = 20;

    public MoviesContract(){

    }

    public static final class MoviesEntry implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().
                appendPath(MOVIES_PATH).
                build();

        public static final String TABLE_NAME = "popular_movie";

        public static final String COLUMN_MOVIE_ID = "movie_id";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_RELEASE_DATE = "release_date";
        public static final String COLUMN_POSTER_PATH = "poster_path";
        public static final String COLUMN_BACKDROP_PATH = "backdrop_path";
        public static final String COLUMN_OVERVIEW = "overview";
        public static final String COLUMN_ADULT = "adult";
        public static final String COLUMN_POPULARITY = "popularity";
        public static final String COLUMN_VOTE_COUNT = "vote_count";
        public static final String COLUMN_VOTE_AVERAGE = "vote_average";
        public static final String COLUMN_ORIGINAL_TITLE = "original_title";
        // this row data's create time, we need it to sort data
        public static final String COLUMN_CREATE_TIME = "create_time";
        // 查询的时候根据这个值获取是按热度排序的电影还是按评分高低排序的电影；本地数据尽量和网络访问一致
        public static final String COLUMN_SORTING_WAR = "sorting_way";
        // 是否收藏(Y/N)
        public static final String COLUMN_IS_FAVOURITE = "is_favourite";
    }
}
