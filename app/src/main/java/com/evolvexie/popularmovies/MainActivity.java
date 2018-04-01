package com.evolvexie.popularmovies;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.evolvexie.popularmovies.adapter.MainRecyclerViewAdapter;
import com.evolvexie.popularmovies.data.CommonPreferences;
import com.evolvexie.popularmovies.data.KeyPreferences;
import com.evolvexie.popularmovies.data.MoviesContract;
import com.evolvexie.popularmovies.model.Movie;
import com.evolvexie.popularmovies.model.PopularMovie;
import com.evolvexie.popularmovies.utils.SharedPreferenceUtil;
import com.evolvexie.popularmovies.utils.UrlUtils;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener,
        MainRecyclerViewAdapter.MainRvAdapterClickHandler,
        LoaderManager.LoaderCallbacks<List<Movie>>,
        NavigationView.OnNavigationItemSelectedListener{

    private static final String TAG = MainActivity.class.getSimpleName();
    // AsyncTaskLoader to loading movies data from internet
    private static final int MOVIES_INTERNET_LOADER = 22;
    // CursorLoader to loading movies data from database
    private static final int MOVIES_CURSOR_LOADER = 33;

    private static final String MOVIES_URL_KEY = "moviesUrl";
    private static final String MOVIES_IS_LOADING_MORE_KEY = "isLoadingMore";
    private boolean isLoadFromInternet = false;
    /*
     * 当前加载模式(网络还是本地)
     * 0 : 本地数据库
     * 1 ：网络数据
     */
    private int curLoadingWay = 0;

    public static final String[] MAIN_MOVIE_COLUMN = {
            MoviesContract.MoviesEntry.COLUMN_MOVIE_ID,
            MoviesContract.MoviesEntry.COLUMN_POSTER_PATH,
            MoviesContract.MoviesEntry.COLUMN_RELEASE_DATE,
            MoviesContract.MoviesEntry.COLUMN_POPULARITY,
            MoviesContract.MoviesEntry.COLUMN_TITLE,
            MoviesContract.MoviesEntry.COLUMN_ORIGINAL_TITLE,
            MoviesContract.MoviesEntry.COLUMN_BACKDROP_PATH,
            MoviesContract.MoviesEntry.COLUMN_VOTE_AVERAGE,
            MoviesContract.MoviesEntry.COLUMN_VOTE_COUNT,
            MoviesContract.MoviesEntry.COLUMN_OVERVIEW,
            MoviesContract.MoviesEntry.COLUMN_IS_FAVOURITE,
    };
    public static final int INDEX_COLUMN_MOVIE_ID = 0;
    public static final int INDEX_COLUMN_POSTER_PATH = 1;
    public static final int INDEX_COLUMN_RELEASE_DATE = 2;
    public static final int INDEX_COLUMN_POPULARITY = 3;
    public static final int INDEX_COLUMN_TITLE = 4;
    public static final int INDEX_COLUMN_ORIGINAL_TITLE = 5;
    public static final int INDEX_COLUMN_BACKDROP_PATH = 6;
    public static final int INDEX_COLUMN_VOTE_AVERAGE = 7;
    public static final int INDEX_COLUMN_VOTE_COUNT = 8;
    public static final int INDEX_COLUMN_COLUMN_OVERVIEW = 9;
    public static final int INDEX_COLUMN_IS_FAVOURITE = 10;

    // TODO 快速下划显示前面图片的时候，由于占位的图片比较小，recycleView的位置已经到第一了，
    // 因而图片加载出来后迅速显示了第一条数据，用户感觉是跳跃过去，并且看起来屏幕闪烁。
    // 解决方案，1、设定图片的高度为固定某个dp值       2、使用大小相似的占位图片

    // TODO 增加一个刷新按钮

    @BindView(R.id.rv_display_movies)
    RecyclerView mMovieListRecyclerView;

    @BindView(R.id.pb_loading_indicator)
    ProgressBar mLoadingIndicator;
    @BindView(R.id.tv_error_message_display)
    TextView mErrorMessageDisplay;

    private MainRecyclerViewAdapter mMainRecycleViewAdapter;
    @BindView(R.id.srl_refresh)
    SwipeRefreshLayout mSwipeRefreshLayout;
    @BindView(R.id.tv_show_refresh_tip)
    TextView mRefreshTipDisplay;
    @BindView(R.id.main_toolbar)
    Toolbar mToolbar;
    @BindView(R.id.main_drawer)
    DrawerLayout mDrawerLayout;
    @BindView(R.id.nav_drawer)
    NavigationView mNavigationView;

    private ActionBarDrawerToggle drawerToggle;
    private final GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
    private boolean isLoadingMore = false; // 标志现在是否正在进行上划加载更多(防止多次上划手势而启动了多个后台线程)
    private int currentPage = 0;
    private String curUrl = null;   // a flag that show if sorting wat had been changed in setting
    private List<Movie> curMovies = new ArrayList<>();
    private String curSortingWay = null;
    // 当前展示的是普通电影列表还是显示的我的收藏的列表
    private int curShowing;
    private static final int SHOWING_MOVIES = 0;
    private static final int SHOWING_FAVOURITE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        drawerToggle = new ActionBarDrawerToggle(this,
                mDrawerLayout,mToolbar,R.string.drawer_open,R.string.drawer_close);
        mDrawerLayout.setDrawerListener(drawerToggle);

        mNavigationView.setNavigationItemSelectedListener(this);

        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorAccent, R.color.colorPrimary);
        mSwipeRefreshLayout.setOnRefreshListener(this);

        mMovieListRecyclerView.setLayoutManager(gridLayoutManager);
        mMovieListRecyclerView.setHasFixedSize(true);

        mMovieListRecyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int lastVisiableItem = gridLayoutManager.findLastVisibleItemPosition();
                int totalItemCount = gridLayoutManager.getItemCount();
                if (lastVisiableItem >= totalItemCount - 4 && dy > 0) {
                    if (!isLoadingMore) {
                        isLoadingMore = true;
                        SharedPreferences sharedPreferences = getSharedPreferences(CommonPreferences.SETTING_PREF_NAME, MODE_PRIVATE);
                        boolean isFirstTime = sharedPreferences.getBoolean(CommonPreferences.IS_FIRST_LOADING, true);
                        if (isFirstTime) {
                            fetchMovieDatas(MOVIES_INTERNET_LOADER, isLoadingMore);
                            sharedPreferences.edit().putBoolean(CommonPreferences.IS_FIRST_LOADING, false).apply();
                        } else {
                            if (curLoadingWay == 0) {
                                fetchMovieDatas(MOVIES_INTERNET_LOADER, isLoadingMore);
                            }else {
                                fetchMovieDatas(MOVIES_CURSOR_LOADER, isLoadingMore);
                            }
                        }
                    }
                }
            }
        });

        mMainRecycleViewAdapter = new MainRecyclerViewAdapter(this);
        mMovieListRecyclerView.setAdapter(mMainRecycleViewAdapter);

        curShowing = SHOWING_MOVIES;
        showMoviesDataView();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId) {
            case R.id.action_setting:
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
                break;
            default:
                break;
        }
        return true;
    }

    public void showErrorMessage() {
        mMovieListRecyclerView.setVisibility(View.INVISIBLE);
        mErrorMessageDisplay.setVisibility(View.VISIBLE);
    }

    public void showMoviesDataView() {
        mMovieListRecyclerView.setVisibility(View.VISIBLE);
        mErrorMessageDisplay.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onRefresh() {
        currentPage = 0;
        mMainRecycleViewAdapter = new MainRecyclerViewAdapter(this);
        mMovieListRecyclerView.setAdapter(mMainRecycleViewAdapter);

        showMoviesDataView();
        Log.d(TAG, "onRefresh: currentPage-->" + currentPage);
        if (curShowing == SHOWING_FAVOURITE){
            currentPage = 0;
            curMovies.clear();
            fetchMovieDatas(MOVIES_CURSOR_LOADER,false);
        }else {
            isLoadingMore = false;
            fetchMovieDatas(MOVIES_INTERNET_LOADER,false);
        }
        Log.d(TAG, "onRefresh: currentPage<--" + currentPage);
    }

    /**
     * handle Main RecyclerView's click event
     *
     * @param movie
     */
    @Override
    public void onClick(Movie movie) {
        Intent intent = new Intent(MainActivity.this, DetailActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        intent.putExtra("movie", movie);
        startActivity(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        //String url = getMoviesUrl();
        SharedPreferences sharedPreferences = getSharedPreferences(CommonPreferences.SETTING_PREF_NAME, MODE_PRIVATE);
        String curSortingSetting = sharedPreferences.getString(CommonPreferences.SORTING_WAY, "popular");
        if ((curSortingWay != null && !curSortingWay.equals(curSortingSetting))  // sorting way had been changed
                || (curMovies == null || curMovies.size() == 0)) {  // current movie list is null or empty
            // 置为空，这样在onStartLoading方法内才会加载新数据
            curMovies.clear();
            isLoadingMore = false;
            currentPage = 0;
            curSortingWay = null; // when curSortingWay is null,it would change to new sortingWar in getMoviesUrl() method
            mMainRecycleViewAdapter = new MainRecyclerViewAdapter(this);
            mMovieListRecyclerView.setAdapter(mMainRecycleViewAdapter);

            showMoviesDataView();
            boolean isFirstTime = sharedPreferences.getBoolean(CommonPreferences.IS_FIRST_LOADING, true);
            if (isFirstTime){
                fetchMovieDatas(MOVIES_INTERNET_LOADER,false);
                sharedPreferences.edit().putBoolean(CommonPreferences.IS_FIRST_LOADING,false).apply();
            }else {
                fetchMovieDatas(MOVIES_CURSOR_LOADER,false);
            }
        }
    }

    @Override
    public Loader<List<Movie>> onCreateLoader(final int id, final Bundle args) {
        if (id == MOVIES_INTERNET_LOADER) {
            isLoadFromInternet = true;
        }else {
            isLoadFromInternet = false;
        }
        return new AsyncTaskLoader<List<Movie>>(this) {
            @Override
            public List<Movie> loadInBackground() {
                Log.d(TAG, "------------loadInBackground---------------");
                switch (id) {
                    case MOVIES_INTERNET_LOADER:
                        return getMoviesFromInternet(args);
                    case MOVIES_CURSOR_LOADER:
                        return getMoviesFromDatabase(args);
                    default:
                        throw new RuntimeException("Un implement loader id");
                }

            }

            @Override
            protected void onStartLoading() {
                super.onStartLoading();
                /**
                 * 进入其它activity时，点击back按钮，会导致MainActivity调用onStart方法，
                 * 而源码中这个方法会调用Loader的onStartLoading方法，造成不必要的数据loading。所以这里需要加一个判断
                 * mSwipeRefreshLayout.isRefreshing()表示正在进行下拉刷型，需要调用后台loading
                 * isLoadingMore表示正在上划加载更多
                 */
                if (curMovies == null || curMovies.size() == 0 || mSwipeRefreshLayout.isRefreshing() || isLoadingMore) {
                    if (!mSwipeRefreshLayout.isRefreshing()) {
                        mLoadingIndicator.setVisibility(View.VISIBLE);
                    }
                    forceLoad();
                }
            }
        };

    }



    @Override
    public void onLoadFinished(Loader<List<Movie>> loader, List<Movie> movies) {
        Log.d(TAG, "------------onLoadFinished---------------");
        if (!isLoadingMore) {
            mLoadingIndicator.setVisibility(View.INVISIBLE);
            mSwipeRefreshLayout.setRefreshing(false);
            if (movies != null) {
                showMoviesDataView();
                mMainRecycleViewAdapter.setMovies(movies);
                curMovies.addAll(movies);
            } else {
                showErrorMessage();
            }
        } else {
            mLoadingIndicator.setVisibility(View.INVISIBLE);
            mSwipeRefreshLayout.setRefreshing(false);
            if (movies != null) {
                showMoviesDataView();
                mMainRecycleViewAdapter.addMovies(movies);
                curMovies.addAll(movies);
            } else {
                showErrorMessage();
            }
        }
        if (mDrawerLayout.isDrawerOpen(mNavigationView)){
            mDrawerLayout.closeDrawer(mNavigationView);
        }
        Log.d(TAG, "onLoadFinished: ####################curMovies-Size:"+curMovies.size());
        if (isLoadFromInternet && movies != null) {
            ContentValues[] contentValues = new ContentValues[movies.size()];
            int i = 0;
            for (Movie movie : movies) {
                ContentValues values = new ContentValues();
                values.put(MoviesContract.MoviesEntry.COLUMN_MOVIE_ID,movie.getId());
                values.put(MoviesContract.MoviesEntry.COLUMN_TITLE,movie.getTitle());
                values.put(MoviesContract.MoviesEntry.COLUMN_BACKDROP_PATH,movie.getBackdropPath());
                values.put(MoviesContract.MoviesEntry.COLUMN_ORIGINAL_TITLE,movie.getOriginalTitle());
                values.put(MoviesContract.MoviesEntry.COLUMN_OVERVIEW,movie.getOverview());
                values.put(MoviesContract.MoviesEntry.COLUMN_POPULARITY,movie.getPopularity());
                values.put(MoviesContract.MoviesEntry.COLUMN_POSTER_PATH,movie.getPosterPath());
                values.put(MoviesContract.MoviesEntry.COLUMN_RELEASE_DATE,movie.getReleaseDate());
                values.put(MoviesContract.MoviesEntry.COLUMN_VOTE_AVERAGE,movie.getVoteAverage());
                values.put(MoviesContract.MoviesEntry.COLUMN_VOTE_COUNT,movie.getVoteCount());
                values.put(MoviesContract.MoviesEntry.COLUMN_CREATE_TIME, String.valueOf(new Date()));
                values.put(MoviesContract.MoviesEntry.COLUMN_SORTING_WAR,curSortingWay);
                contentValues[i] = values;
                i++;
            }
            Uri uri = MoviesContract.MoviesEntry.CONTENT_URI;
            if (!isLoadingMore){ //不是上划加载更多，则需要删除数据后全量插入
                //getContentResolver().delete(uri,null,null);
                getContentResolver().bulkInsert(uri,contentValues);
                Log.d(TAG, "onLoadFinished: -----------bulkInsert with delete : "+movies.size());
            }else {
                getContentResolver().bulkInsert(uri,contentValues);
                Log.d(TAG, "onLoadFinished: -----------bulkInsert : "+movies.size());
            }
        }
        if (movies == null || movies.size() == 0) { // 没有加载出数据时，提示用户下拉刷新
            if (curMovies == null || curMovies.size() == 0) {
                mRefreshTipDisplay.setVisibility(View.VISIBLE);
            }
        }else{
            mRefreshTipDisplay.setVisibility(View.INVISIBLE);
        }
        isLoadingMore = false;
    }

    @Override
    public void onLoaderReset(Loader<List<Movie>> loader) {

    }

    public List<Movie> getMoviesFromInternet(Bundle args){
        OkHttpClient okHttpClient = new OkHttpClient();
        Gson gson = new Gson();
        String url = args.getString(MOVIES_URL_KEY);
        Log.d(TAG, "getMoviesFromInternet: URL--->"+url);
        List<Movie> results = new ArrayList<>();
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();
        try {
            Response response = okHttpClient.newCall(request).execute();
            if (response.message().equals("OK")) {
                String jsonStr = response.body().string();
                PopularMovie popularMovie = gson.fromJson(jsonStr, PopularMovie.class);
                //currentPage = popularMovie.getPage();
                for (Movie movie : popularMovie.getResults()) {
                    results.add(movie);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return results;
    }

    public List<Movie> getMoviesFromDatabase(Bundle args){
        Uri uri = MoviesContract.MoviesEntry.CONTENT_URI
                .buildUpon()
                .appendPath(MoviesContract.MOVIES_PAGE_PATH)
                .appendPath(String.valueOf(++currentPage))
                .build();
        Log.d(TAG, "getMoviesFromDatabase: RUI--->"+uri);
        String sortOrder = null;
        String selection = null;
        String[] selectionArgs = null;
        if (curSortingWay == null) {
            SharedPreferences sharedPreferences = getSharedPreferences(CommonPreferences.SETTING_PREF_NAME, MODE_PRIVATE);
            curSortingWay = sharedPreferences.getString(CommonPreferences.SORTING_WAY, "popular");
        }
        if (curSortingWay.equals("popular")){ // 取出按热度查询的数据，并且按热度降序排序
            sortOrder = MoviesContract.MoviesEntry.COLUMN_POPULARITY + " DESC";
        }else if(curSortingWay.equals("rated")) { // 取出按评分查询的数据，并且按评分降序排序
            sortOrder = MoviesContract.MoviesEntry.COLUMN_VOTE_AVERAGE + " DESC";
        }
        if (curShowing == SHOWING_FAVOURITE) {
            selection = MoviesContract.MoviesEntry.COLUMN_SORTING_WAR + " = ? AND "
                    + MoviesContract.MoviesEntry.COLUMN_IS_FAVOURITE + " = ?";
            selectionArgs = new String[]{curSortingWay,"Y"};
        }else {
            selection = MoviesContract.MoviesEntry.COLUMN_SORTING_WAR + " = ?";
            selectionArgs = new String[]{curSortingWay};
        }
        Cursor cursor = getContentResolver().query(uri,
                MAIN_MOVIE_COLUMN,
                selection,
                selectionArgs,
                sortOrder);
        List<Movie> results = new ArrayList<>();
        while (cursor.moveToNext()){
            Movie movie = new Movie();
            movie.setId(cursor.getInt(INDEX_COLUMN_MOVIE_ID));
            movie.setTitle(cursor.getString(INDEX_COLUMN_TITLE));
            movie.setBackdropPath(cursor.getString(INDEX_COLUMN_BACKDROP_PATH));
            movie.setOriginalTitle(cursor.getString(INDEX_COLUMN_ORIGINAL_TITLE));
            movie.setOverview(cursor.getString(INDEX_COLUMN_COLUMN_OVERVIEW));
            movie.setPopularity(cursor.getDouble(INDEX_COLUMN_POPULARITY));
            movie.setVoteAverage(cursor.getString(INDEX_COLUMN_VOTE_AVERAGE));
            movie.setVoteCount(cursor.getInt(INDEX_COLUMN_VOTE_COUNT));
            movie.setPosterPath(cursor.getString(INDEX_COLUMN_POSTER_PATH));
            movie.setReleaseDate(cursor.getString(INDEX_COLUMN_RELEASE_DATE));
            movie.setIsFavourite(cursor.getString(INDEX_COLUMN_IS_FAVOURITE));
            results.add(movie);
        }
        return results;
    }

    public String getMoviesUrl() {
        SharedPreferences sharedPreferences = getSharedPreferences(CommonPreferences.SETTING_PREF_NAME, MODE_PRIVATE);
        String curSortingSetting = sharedPreferences.getString(CommonPreferences.SORTING_WAY, "popular");
        if (curSortingWay == null) {
            curSortingWay = curSortingSetting;
        }
        String url = null;
        if (curSortingSetting.equals("popular")) {
            url = UrlUtils.GET_POPULAR.replace("API_KEY", KeyPreferences.API_KEY)
                    .replace("PAGE", String.valueOf(++currentPage));
        } else if (curSortingSetting.equals("rated")) {
            url = UrlUtils.GET_TOP_RATED.replace("API_KEY", KeyPreferences.API_KEY)
                    .replace("PAGE", String.valueOf(++currentPage));
        }
        return url;
    }

    public void fetchMovieDatas(int loaderId,boolean isLoadingMore){
        Bundle queryBundle = new Bundle();
        if (loaderId == MOVIES_INTERNET_LOADER) {
            queryBundle.putString(MOVIES_URL_KEY,getMoviesUrl());
            queryBundle.putBoolean(MOVIES_IS_LOADING_MORE_KEY,isLoadingMore);
            curLoadingWay = 0; //切换数据加载来源为网络
        }else {
            curLoadingWay = 1;//切换数据加载来源为数据库
        }
        LoaderManager loaderManager = getSupportLoaderManager();
        Loader<List<Movie>> loader =  loaderManager.getLoader(loaderId);
        if (loader == null) {
            loaderManager.initLoader(loaderId,queryBundle,this);
        }else {
            loaderManager.restartLoader(loaderId,queryBundle,this);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        mDrawerLayout.closeDrawer(Gravity.START);
        switch (item.getItemId()){
            case R.id.action_movies: //侧边栏选择电影列表
                if (curShowing != SHOWING_MOVIES){
                    currentPage = 0;
                    curShowing = SHOWING_MOVIES;
                    curMovies.clear();
                    fetchMovieDatas(MOVIES_CURSOR_LOADER,false);
                }
                break;
            case R.id.action_favourites: //选择收藏列表
                if (curShowing != SHOWING_FAVOURITE){
                    currentPage = 0;
                    curShowing = SHOWING_FAVOURITE;
                    curMovies.clear();
                    fetchMovieDatas(MOVIES_CURSOR_LOADER,false);
                }
                break;
            case R.id.action_drawer_setting: //选择设置页面
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
                break;
            default:
                break;
        }
        return true;
    }
}
