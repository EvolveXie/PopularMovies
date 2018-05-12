package com.evolvexie.popularmovies.fragment;


import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.evolvexie.popularmovies.DetailActivity;
import com.evolvexie.popularmovies.R;
import com.evolvexie.popularmovies.adapter.ReviewRecyclerViewAdapter;
import com.evolvexie.popularmovies.data.KeyPreferences;
import com.evolvexie.popularmovies.data.MoviesContract;
import com.evolvexie.popularmovies.model.Movie;
import com.evolvexie.popularmovies.model.MovieDetail;
import com.evolvexie.popularmovies.model.MovieReview;
import com.evolvexie.popularmovies.model.MovieTrailer;
import com.evolvexie.popularmovies.model.Review;
import com.evolvexie.popularmovies.model.Video;
import com.evolvexie.popularmovies.utils.NetUtils;
import com.evolvexie.popularmovies.utils.UrlUtils;
import com.google.gson.Gson;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import static android.content.ContentValues.TAG;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link DetailFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DetailFragment extends Fragment {

    @BindView(R.id.tv_movie_title)
    TextView mTitleTextView;
    @BindView(R.id.tv_movie_release_date)
    TextView mReleaseDateTextView;
    @BindView(R.id.tv_movie_overview)
    TextView mOverviewTextView;
    @BindView(R.id.tv_movie_vote_average)
    TextView mVoteAverageTextView;
    @BindView(R.id.iv_poster)
    ImageView mPosterImageView;
    @BindView(R.id.iv_backdrop)
    ImageView mBackdropImageView;

    @BindView(R.id.tv_movie_runtime)
    TextView mMovieRuntimeTextView;
    @BindView(R.id.tv_movie_genre)
    TextView mMovieGenreTextView;
    @BindView(R.id.pb_loading_youtobe)
    ProgressBar mLoadingYoutobe;
    @BindView(R.id.lv_trailer_list)
    ListView mTrailerList;
    @BindView(R.id.sv_detail_scroll)
    ScrollView mScrollView;
    @BindView(R.id.tv_vote_count_display)
    TextView mVoteCount;
    @BindView(R.id.rv_detail_review_list)
    RecyclerView mReviewList;

    private int width; // screen width
    private int movieId;
    private Movie curMovie;
    private static Context mContext;
    private int curReviewsPage = 0;
    private final RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
    private List<Review> reviewDatas;
    //是否在loading 影评数据
    private static boolean isLoadingReviews = true;
    private static Menu menu;

    private AppCompatActivity mActivity;
    private Unbinder unbinder;
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_MOVIE = "movie";

    // TODO: Rename and change types of parameters
    private Movie mMovie;

    private LoadDetailTask mLoadDetailTask;
    private LoadReviewsTask mLoadReviewTask;
    private LoadTrailerTask mLoadTrailerTask;
    private UpdateMovieTask mUpdateMovieTask;


    public DetailFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment DetailFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static DetailFragment newInstance(Movie movie) {
        DetailFragment fragment = new DetailFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_MOVIE, movie);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        if (getArguments() != null) {
            mMovie = getArguments().getParcelable(ARG_MOVIE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_detail, container, false);
        unbinder = ButterKnife.bind(this,view);
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mActivity = (AppCompatActivity) getActivity();

        Intent fromIntent = mActivity.getIntent();
        DisplayMetrics displayMetrics = this.getResources().getDisplayMetrics();
        width = displayMetrics.widthPixels;
        mContext = mActivity;
        initData(fromIntent);

        mReviewList.setLayoutManager(layoutManager);
        mReviewList.setHasFixedSize(true);
        mReviewList.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (isLoadingReviews) { //正在loading则不用新建后台线程loading
                    return;
                }
                isLoadingReviews = true;
                LinearLayoutManager linearLayoutManager = (LinearLayoutManager) layoutManager;
                int lastVisiableItem = linearLayoutManager.findLastVisibleItemPosition();
                int totalItemCount = linearLayoutManager.getItemCount();
                if (lastVisiableItem >= totalItemCount - 4 && dy > 0) {
                    new LoadReviewsTask().execute(getReviewsUrl());
                }
            }
        });
        mReviewList.setNestedScrollingEnabled(false);
        // 加载电影详情数据
        mLoadDetailTask = new LoadDetailTask();
        mLoadDetailTask.execute(getDetailUrl(movieId));
        mLoadTrailerTask =  new LoadTrailerTask();
        mLoadTrailerTask.execute(getTrailerUrl());
        mLoadReviewTask =  new LoadReviewsTask();
        mLoadReviewTask.execute(getReviewsUrl());
    }

    private void initData(Intent fromIntent){
        if (fromIntent.hasExtra("movie") || mMovie != null){
            Movie movie = fromIntent.getParcelableExtra("movie");
            if (mMovie != null) {
                movie = mMovie;
            }
            if (movie == null) {
                return;
            }
            curMovie = movie;
            movieId = movie.getId();
            Transformation transformation = new Transformation() {
                @Override
                public Bitmap transform(Bitmap source) {
                    if (source.getWidth() == 0) {
                        return source;
                    }
                    int imgWidth = source.getWidth();
                    Log.i(TAG, "detail------------transform: imgWidth" + imgWidth);
                    int imgHeight = source.getHeight()* width/imgWidth;
                    Log.i(TAG, "detail------------transform: imgHeight" + imgHeight);
                    Bitmap result = source.createScaledBitmap(source, width, imgHeight,false);
                    if (source != result){
                        source.recycle();
                    }else {
                        return source;
                    }
                    return result;
                }

                @Override
                public String key() {
                    return "detailPosterTrans";
                }
            };
            String backdropPath = UrlUtils.IMAGE_DETAIL_URL + movie.getBackdropPath();
            Picasso.with(mActivity)
                    .load(backdropPath).transform(transformation)
                    .placeholder(R.drawable.movie_temp_image)
                    .error(R.mipmap.ic_error)
                    .into(mBackdropImageView);
            mTitleTextView.setText(movie.getTitle());
            String posterPath = UrlUtils.IMAGE_DETAIL_URL + movie.getPosterPath();
            Picasso.with(mActivity)
                    .load(posterPath).transform(transformation)
                    .memoryPolicy(MemoryPolicy.NO_CACHE)
                    .placeholder(R.drawable.movie_temp_image)
                    .error(R.mipmap.ic_error)
                    .into(mPosterImageView);
            mOverviewTextView.setText(movie.getOverview());
            mVoteAverageTextView.setText(movie.getVoteAverage());
            mReleaseDateTextView.setText(getResources().getString(R.string.label_release_date)
                    +" "+movie.getReleaseDate());
            mVoteCount.setText(movie.getVoteCount()+getResources().getString(R.string.vote_count_unit));

        }

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.detail,menu);
        this.menu = menu;
//        if ("Y".equals(curMovie.getIsFavourite())){
//            MenuItem collectItem = menu.findItem(R.id.action_collect);
//            collectItem.setIcon(R.mipmap.favourite_orange);
//            collectItem.setTitle(getResources().getString(R.string.cancel_favourite));
//        }else if (TextUtils.isEmpty(curMovie.getIsFavourite())) {
            new LoadDetailFromDbTask().execute(String.valueOf(curMovie.getId()));
//        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_collect:
                if (item.getTitle().equals(getResources().getString(R.string.favourite))){
                    curMovie.setIsFavourite("Y");
                    item.setIcon(R.mipmap.favourite_orange);
                    item.setTitle(getResources().getString(R.string.cancel_favourite));
                }else {
                    curMovie.setIsFavourite("N");
                    item.setIcon(R.mipmap.favourite_white);
                    item.setTitle(getResources().getString(R.string.favourite));
                }
                mUpdateMovieTask = new UpdateMovieTask();
                mUpdateMovieTask.execute(curMovie);
                break;
            case android.R.id.home: //toolBar箭头
                mActivity.finish();
            default:
                break;
        }
        return true;
    }

    /**
     * open YouToBe to play video or open a browser
     * @param context
     * @param id
     */
    public static void watchYoutubeVideo(Context context, String id){
        Intent appIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(UrlUtils.BASE_YOUTOBE_VIDEO_APP.replace("VIDEO_KEY",id)));
        Intent webIntent = new Intent(Intent.ACTION_VIEW,
                Uri.parse(UrlUtils.BASE_YOUTOBE_VIDEO_WEB.replace("VIDEO_KEY",id)));
        try {
            context.startActivity(appIntent);
        } catch (ActivityNotFoundException ex) {
            context.startActivity(webIntent);
        }
    }

    public String getDetailUrl(int movieId){
        return UrlUtils.GET_MOVIE_DETAIL.replace("API_KEY", KeyPreferences.API_KEY)
                .replace("MOVIE_ID",String.valueOf(movieId));
    }

    public String getTrailerUrl(){
        return UrlUtils.GET_MOVIE_VIDEO.replace("API_KEY",KeyPreferences.API_KEY)
                .replace("MOVIE_ID",String.valueOf(movieId));
    }

    public String getReviewsUrl(){
        curReviewsPage++;
        return UrlUtils.GET_REVIEW_LIST.replace("API_KEY",KeyPreferences.API_KEY)
                .replace("MOVIE_ID",String.valueOf(movieId))
                .replace("PAGE",String.valueOf(curReviewsPage));
    }

    private class LoadDetailTask extends AsyncTask<String,Integer,MovieDetail> {

        @Override
        protected MovieDetail doInBackground(String... params) {
            String url = params[0];
            String jsonStr = NetUtils.get(url);
            Gson gson= new Gson();
            MovieDetail movieDetail = gson.fromJson(jsonStr,MovieDetail.class);
            return movieDetail;
        }

        @Override
        protected void onPostExecute(MovieDetail movieDetail) {
            if (movieDetail != null && isAdded()) {
                mMovieRuntimeTextView.setText(getResources().getString(R.string.label_runtime)+" "
                        +String.valueOf(movieDetail.getRuntime())
                        +getResources().getString(R.string.label_runtime_unit));
                mMovieGenreTextView.setText(getResources().getString(R.string.label_genre)+" "
                        +movieDetail.getGenresStr());
            }
        }
    }

    private class LoadTrailerTask extends AsyncTask<String,Void,MovieTrailer>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected MovieTrailer doInBackground(String... params) {
            String url = params[0];
            String jsonStr = NetUtils.get(url);
            Gson gson= new Gson();
            MovieTrailer movieTrailer = gson.fromJson(jsonStr,MovieTrailer.class);
            return movieTrailer;
        }

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        protected void onPostExecute(MovieTrailer movieTrailer) {
            if (movieTrailer == null || movieTrailer.getVideos().size() == 0) {
                //Toast.makeText(mContext,"该影片暂未有预告片",Toast.LENGTH_SHORT).show();
                //mTrailerList.setVisibility(View.INVISIBLE);
                return;
            }else {
                List<Map<String,Object>> dataList = new ArrayList<>();
                for (Video video:movieTrailer.getVideos()) {
                    Map<String,Object> data = new HashMap<>();
                    data.put("trailerName",video.getName());
                    data.put("trailerKey",video.getKey());
                    dataList.add(data);
                }
                mTrailerList.setAdapter(new SimpleAdapter(
                        mContext,
                        dataList,
                        R.layout.layout_movie_trailer_list_item,
                        new String[]{"trailerName","trailerKey"},
                        new int[]{R.id.tv_list_item_trailer_name,R.id.tv_list_item_trailer_key}));
                mTrailerList.setVisibility(View.VISIBLE);
                mTrailerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        TextView trailerKeyTextView = (TextView) view.findViewById(R.id.tv_list_item_trailer_key);
                        String key = trailerKeyTextView.getText().toString();
                        watchYoutubeVideo(mContext,key);
                    }
                });
                // 解决滑动事件冲突
                mTrailerList.setNestedScrollingEnabled(true);
//                mTrailerList.setOnTouchListener(new View.OnTouchListener() {
//                    @Override
//                    public boolean onTouch(View v, MotionEvent event) {
//                        mScrollView.requestDisallowInterceptTouchEvent(true);
//                        return false;
//                    }
//                });
            }
        }
    }

    private class LoadReviewsTask extends AsyncTask<String,Void,List<Review>>{

        @Override
        protected List<Review> doInBackground(String... params) {
            String url = params[0];
            String jsonStr = NetUtils.get(url);
            if (jsonStr == null) {
                return new ArrayList<>();
            }
            Gson gson= new Gson();
            MovieReview movieReview = gson.fromJson(jsonStr,MovieReview.class);
            return movieReview.getReviews();
        }

        @Override
        protected void onPostExecute(List<Review> reviews) {
            super.onPostExecute(reviews);
            mReviewList.setVisibility(View.VISIBLE);
            if (reviewDatas == null) { //首次加载
                mReviewList.setAdapter(new ReviewRecyclerViewAdapter(reviews));
                reviewDatas = new ArrayList<>();
                reviewDatas.addAll(reviews);
            }else {
                ReviewRecyclerViewAdapter rvAdapter = (ReviewRecyclerViewAdapter) mReviewList.getAdapter();
                reviewDatas.addAll(reviews);
                rvAdapter.setDatas(reviewDatas);
            }
            isLoadingReviews = false;
        }
    }

    private class UpdateMovieTask extends AsyncTask<Movie,Void,Integer>{
        Movie mMovie = null;
        @Override
        protected Integer doInBackground(Movie... movies) {
            mMovie = movies[0];
            String movieId = String.valueOf(movies[0].getId());
            Uri uri = MoviesContract.MoviesEntry.CONTENT_URI;
            uri = uri.buildUpon().appendPath(movieId).build();
            int updateCount = mContext.getContentResolver().update(uri,getContentValuesByMovie(movies[0]),null,null);
            return updateCount;
        }

        @Override
        protected void onPostExecute(Integer count) {
            if (count > 0) {
                Snackbar.make(mScrollView, getResources().getString(R.string.favourite_add_tip), Snackbar.LENGTH_SHORT).show();
            }
        }
    }

    private class LoadDetailFromDbTask extends AsyncTask<String,Void,Movie>{

        @Override
        protected Movie doInBackground(String... params) {
            String movieId = params[0];
            Uri uri = MoviesContract.MoviesEntry.CONTENT_URI.buildUpon()
                    .appendPath(movieId).build();
            Cursor cursor = mContext.getContentResolver().query(uri,
                    new String[]{MoviesContract.MoviesEntry.COLUMN_IS_FAVOURITE},
                    MoviesContract.MoviesEntry.COLUMN_MOVIE_ID + " = ?",
                    new String[]{movieId},
                    null);
            if (cursor.moveToNext()) {
                Movie movie = new Movie();
                movie.setIsFavourite(cursor.getString(0));
                cursor.close();
                return movie;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Movie movie) {
            if (movie != null && isAdded()) {
                MenuItem favourite = menu.findItem(R.id.action_collect);
                if ("Y".equals(movie.getIsFavourite())){
                    favourite.setIcon(R.mipmap.favourite_orange);
                    favourite.setTitle(getResources().getString(R.string.cancel_favourite));
                }
            }
        }
    }

    private ContentValues getContentValuesByMovie(Movie movie){
        ContentValues values = new ContentValues();
        values.put(MoviesContract.MoviesEntry.COLUMN_MOVIE_ID,movie.getId());
        values.put(MoviesContract.MoviesEntry.COLUMN_IS_FAVOURITE,movie.getIsFavourite());
        return values;
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mLoadDetailTask != null) {
            mLoadDetailTask.cancel(true);
            Log.i(TAG, "onStop: mLoadDetailTask" + "had been stopped");
        }
        if (mLoadTrailerTask != null) {
            mLoadTrailerTask.cancel(true);
            Log.i(TAG, "onStop: mLoadTrailerTask" + "had been stopped");
        }
        if (mLoadReviewTask != null) {
            mLoadReviewTask.cancel(true);
            Log.i(TAG, "onStop: mLoadReviewTask" + "had been stopped");
        }
        if (mUpdateMovieTask != null) {
            mUpdateMovieTask.cancel(true);
            Log.i(TAG, "onStop: mUpdateMovieTask" + "had been stopped");
        }
    }
}
