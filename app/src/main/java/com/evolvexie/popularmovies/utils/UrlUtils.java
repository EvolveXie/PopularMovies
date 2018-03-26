package com.evolvexie.popularmovies.utils;

/**
 * Created by hand on 2017/4/17.
 */

public class UrlUtils {
    public static final String GET_POPULAR = "https://api.themoviedb.org/3/movie/popular?api_key=API_KEY&language=zh-cn&page=PAGE";
    public static final String GET_TOP_RATED = "https://api.themoviedb.org/3/movie/top_rated?api_key=API_KEY&language=zh-cn&page=PAGE";
    public static final String IMAGE_BASE_URL = "http://image.tmdb.org/t/p/w185/";
    public static final String IMAGE_DETAIL_URL = "http://image.tmdb.org/t/p/w500/";

    /**
     * 获取电影预告片
     */
    public static final String GET_MOVIE_VIDEO = "https://api.themoviedb.org/3/movie/MOVIE_ID/videos?api_key=API_KEY&language=en_US";
    /**
     * youtobe 基本视频链接APP
     */
    public static final String BASE_YOUTOBE_VIDEO_APP = "vnd.youtube:VIDEO_KEY";
    /**
     * youtobe 基本视频链接WEB
     */
    public static final String BASE_YOUTOBE_VIDEO_WEB = "https://www.youtube.com/watch?v=VIDEO_KEY";
    /**
     * 电影详情
     */
    public static final String GET_MOVIE_DETAIL = "https://api.themoviedb.org/3/movie/MOVIE_ID?api_key=API_KEY&language=zh-cn";

}
