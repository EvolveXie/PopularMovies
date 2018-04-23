package com.evolvexie.popularmovies.fragment;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.evolvexie.popularmovies.BuildConfig;
import com.evolvexie.popularmovies.DetailActivity;
import com.evolvexie.popularmovies.MainActivity;
import com.evolvexie.popularmovies.R;
import com.evolvexie.popularmovies.SettingsActivity;
import com.evolvexie.popularmovies.adapter.MainRecyclerViewAdapter;
import com.evolvexie.popularmovies.data.CommonPreferences;
import com.evolvexie.popularmovies.data.KeyPreferences;
import com.evolvexie.popularmovies.data.MoviesContract;
import com.evolvexie.popularmovies.model.Movie;
import com.evolvexie.popularmovies.model.PopularMovie;
import com.evolvexie.popularmovies.utils.UrlUtils;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener,
        MainRecyclerViewAdapter.MainRvAdapterClickHandler,
        LoaderManager.LoaderCallbacks<List<Movie>>{

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

    private final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
    private boolean isLoadingMore = false; // 标志现在是否正在进行上划加载更多(防止多次上划手势而启动了多个后台线程)
    private int currentPage = 0;
    private String curUrl = null;   // a flag that show if sorting wat had been changed in setting
    private List<Movie> curMovies = new ArrayList<>();
    private String curSortingWay = null;
    // 当前展示的是普通电影列表还是显示的我的收藏的列表
    private int curShowing;
    private static final int SHOWING_MOVIES = 0;
    private static final int SHOWING_FAVOURITE = 1;

    // 用于标明是不是所有数据库数据都已经被查询出来了，避免不必要的查询
    private static boolean isAllBeenQuery = false;

    private MainActivity mActivity;
    private Unbinder unbinder;
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    //private OnFragmentInteractionListener mListener;

    public MainFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MainFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MainFragment newInstance(String param1, String param2) {
        MainFragment fragment = new MainFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        unbinder = ButterKnife.bind(this,view);
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mActivity = (MainActivity) getActivity();

        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorAccent, R.color.colorPrimary);
        mSwipeRefreshLayout.setOnRefreshListener(this);

        mMovieListRecyclerView.setLayoutManager(linearLayoutManager);
        mMovieListRecyclerView.setHasFixedSize(true);

        mMovieListRecyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int lastVisiableItem = linearLayoutManager.findLastVisibleItemPosition();
                int totalItemCount = linearLayoutManager.getItemCount();
                if (lastVisiableItem >= totalItemCount - 4 && dy > 0) {
                    if (!isLoadingMore && !isAllBeenQuery) {
                        isLoadingMore = true;
                        mMainRecycleViewAdapter.changeLoadMoreStatus(BuildConfig.MAIN_LOADING_MORE);
                        SharedPreferences sharedPreferences = mActivity.getSharedPreferences(CommonPreferences.SETTING_PREF_NAME, mActivity.MODE_PRIVATE);
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
    public void onStart() {
        super.onStart();
        SharedPreferences sharedPreferences = mActivity.getSharedPreferences(CommonPreferences.SETTING_PREF_NAME, mActivity.MODE_PRIVATE);
        String curSortingSetting = sharedPreferences.getString(CommonPreferences.SORTING_WAY, "popular");
        if ("popular".equals(curSortingSetting) || curSortingSetting == null){
            mActivity.getSupportActionBar().setTitle(R.string.main_activity_name_popular);
        }else {
            mActivity.getSupportActionBar().setTitle(R.string.main_activity_name_rate);
        }
        if ((curSortingWay != null && !curSortingWay.equals(curSortingSetting))  // sorting way had been changed
                || (curMovies == null || curMovies.size() == 0)) {  // current movie list is null or empty
            // 置为空，这样在onStartLoading方法内才会加载新数据
            curMovies.clear();
            isLoadingMore = false;
            mMainRecycleViewAdapter.changeLoadMoreStatus(BuildConfig.MAIN_PULLUP_LOAD_MORE);
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
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    /**
     * handle Main RecyclerView's click event
     *
     * @param movie
     */
    @Override
    public void onClick(Movie movie) {
        Intent intent = new Intent(mActivity, DetailActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        intent.putExtra("movie", movie);
        startActivity(intent);
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
            mMainRecycleViewAdapter.changeLoadMoreStatus(BuildConfig.MAIN_PULLUP_LOAD_MORE);
            fetchMovieDatas(MOVIES_INTERNET_LOADER,false);
        }
        Log.d(TAG, "onRefresh: currentPage<--" + currentPage);
    }

    @Override
    public Loader<List<Movie>> onCreateLoader(final int id, final Bundle args) {
        if (id == MOVIES_INTERNET_LOADER) {
            isLoadFromInternet = true;
        }else {
            isLoadFromInternet = false;
        }
        return new AsyncTaskLoader<List<Movie>>(mActivity) {
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
                        //mLoadingIndicator.setVisibility(View.VISIBLE);
                        mMainRecycleViewAdapter.changeLoadMoreStatus(BuildConfig.MAIN_LOADING_MORE);
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
//        if (mDrawerLayout.isDrawerOpen(mNavigationView)){
//            mDrawerLayout.closeDrawer(mNavigationView);
//        }
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
                mActivity.getContentResolver().bulkInsert(uri,contentValues);
                Log.d(TAG, "onLoadFinished: -----------bulkInsert with delete : "+movies.size());
            }else {
                mActivity.getContentResolver().bulkInsert(uri,contentValues);
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
        mMainRecycleViewAdapter.changeLoadMoreStatus(BuildConfig.MAIN_PULLUP_LOAD_MORE);
    }

    @Override
    public void onLoaderReset(Loader<List<Movie>> loader) {

    }

    public List<Movie> getMoviesFromInternet(Bundle args){
        isAllBeenQuery = false;
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
            SharedPreferences sharedPreferences = mActivity.getSharedPreferences(CommonPreferences.SETTING_PREF_NAME, mActivity.MODE_PRIVATE);
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
        Cursor cursor = mActivity.getContentResolver().query(uri,
                MAIN_MOVIE_COLUMN,
                selection,
                selectionArgs,
                sortOrder);
        if (cursor.getCount() == 0) {
            isAllBeenQuery = true;
        }
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
        SharedPreferences sharedPreferences = mActivity.getSharedPreferences(CommonPreferences.SETTING_PREF_NAME, mActivity.MODE_PRIVATE);
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
        LoaderManager loaderManager = mActivity.getSupportLoaderManager();
        Loader<List<Movie>> loader =  loaderManager.getLoader(loaderId);
        if (loader == null) {
            loaderManager.initLoader(loaderId,queryBundle,this);
        }else {
            loaderManager.restartLoader(loaderId,queryBundle,this);
        }
    }

    /**
     * MainActivity的 侧边栏选项点击处理方法
     * @param item
     */
    public boolean navigationItemSelect(@NonNull MenuItem item,DrawerLayout drawerLayout){
        drawerLayout.closeDrawer(Gravity.START);
        switch (item.getItemId()){
            case R.id.action_movies: //侧边栏选择电影列表
                isAllBeenQuery = false;
                if (curShowing != SHOWING_MOVIES){
                    currentPage = 0;
                    curShowing = SHOWING_MOVIES;
                    curMovies.clear();
                    fetchMovieDatas(MOVIES_CURSOR_LOADER,false);
                }
                break;
            case R.id.action_favourites: //选择收藏列表
                isAllBeenQuery = false;
                if (curShowing != SHOWING_FAVOURITE){
                    currentPage = 0;
                    curShowing = SHOWING_FAVOURITE;
                    curMovies.clear();
                    fetchMovieDatas(MOVIES_CURSOR_LOADER,false);
                }
                break;
            case R.id.action_drawer_setting: //选择设置页面
                Intent intent = new Intent(mActivity, SettingsActivity.class);
                startActivity(intent);
                break;
            default:
                break;
        }
        return true;
    }
}
