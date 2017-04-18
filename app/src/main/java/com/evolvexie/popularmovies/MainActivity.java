package com.evolvexie.popularmovies;

import android.os.AsyncTask;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.evolvexie.popularmovies.adapter.MainRecyclerViewAdapter;
import com.evolvexie.popularmovies.data.KeyPreferences;
import com.evolvexie.popularmovies.model.Movie;
import com.evolvexie.popularmovies.model.PopularMovie;
import com.evolvexie.popularmovies.utils.UrlUtils;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    private RecyclerView mMovieListRecyclerView;

    private ProgressBar mLoadingIndicator;
    private TextView mErrorMessageDisplay;

    private MainRecyclerViewAdapter mMainRecycleViewAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    private final GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
    private boolean isLoadingMore = false;
    private int currentPage = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mLoadingIndicator = (ProgressBar) findViewById(R.id.pb_loading_indicator);
        mErrorMessageDisplay = (TextView) findViewById(R.id.tv_error_message_display);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.srl_refresh);

        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorAccent, R.color.colorPrimary);
        mSwipeRefreshLayout.setOnRefreshListener(this);

        mMovieListRecyclerView = (RecyclerView) findViewById(R.id.rv_display_movies);
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
                        String url = UrlUtils.GET_POPULAR.replace("API_KEY", KeyPreferences.API_KEY)
                                .replace("PAGE", String.valueOf(++currentPage));
                        new FetchMoviesTask().execute(url,"true");
                    }
                }
            }
        });

        mMainRecycleViewAdapter = new MainRecyclerViewAdapter();
        mMovieListRecyclerView.setAdapter(mMainRecycleViewAdapter);

        showMoviesDataView();
        String url = UrlUtils.GET_POPULAR.replace("API_KEY", KeyPreferences.API_KEY)
                .replace("PAGE", String.valueOf(++currentPage));
        new FetchMoviesTask().execute(url);
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
        mMainRecycleViewAdapter = new MainRecyclerViewAdapter();
        mMovieListRecyclerView.setAdapter(mMainRecycleViewAdapter);

        showMoviesDataView();
        String url = UrlUtils.GET_POPULAR.replace("API_KEY", KeyPreferences.API_KEY)
                .replace("PAGE", String.valueOf(++currentPage));
        new FetchMoviesTask().execute(url);
    }

    public class FetchMoviesTask extends AsyncTask<String, Void, List<Movie>> {

        OkHttpClient okHttpClient = new OkHttpClient();
        MediaType mediaType = MediaType.parse("application/octet-stream");
        RequestBody requestBody = RequestBody.create(mediaType, "{}");
        Gson gson = new Gson();
        boolean isLoadMore = false;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (!mSwipeRefreshLayout.isRefreshing()) {
                mLoadingIndicator.setVisibility(View.VISIBLE);
            }
        }

        @Override
        protected List<Movie> doInBackground(String... params) {
            if (params.length == 0) {
                return null;
            }
            List<Movie> results = new ArrayList<>();
            String url = params[0];
            if (params.length > 1){
                isLoadMore = Boolean.parseBoolean(params[1]); //是否是加载更多数据
            }else{
                isLoadMore = false;
            }
            Request request = new Request.Builder()
                    .url(url)
                    .get()
                    .build();
            try {
                Response response = okHttpClient.newCall(request).execute();
                if (response.message().equals("OK")) {
                    String jsonStr = response.body().string();
                    PopularMovie popularMovie = gson.fromJson(jsonStr, PopularMovie.class);
                    currentPage = popularMovie.getPage();
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

        @Override
        protected void onPostExecute(List<Movie> movies) {
            super.onPostExecute(movies);
            if (!isLoadMore){
                mLoadingIndicator.setVisibility(View.INVISIBLE);
                mSwipeRefreshLayout.setRefreshing(false);
                if (movies != null) {
                    showMoviesDataView();
                    mMainRecycleViewAdapter.setMovies(movies);
                } else {
                    showErrorMessage();
                }
            }else{
                mLoadingIndicator.setVisibility(View.INVISIBLE);
                mSwipeRefreshLayout.setRefreshing(false);
                if (movies != null) {
                    showMoviesDataView();
                    mMainRecycleViewAdapter.addMovies(movies);
                } else {
                    showErrorMessage();
                }
            }

        }
    }
}
