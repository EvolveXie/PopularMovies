package com.evolvexie.popularmovies;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.widget.ImageView;
import android.widget.TextView;

import com.evolvexie.popularmovies.data.KeyPreferences;
import com.evolvexie.popularmovies.model.Movie;
import com.evolvexie.popularmovies.model.MovieDetail;
import com.evolvexie.popularmovies.utils.NetUtils;
import com.evolvexie.popularmovies.utils.UrlUtils;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DetailActivity extends AppCompatActivity {

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

    private int width; // screen width
    private int movieId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        ButterKnife.bind(this);
        Intent fromIntent = getIntent();
        DisplayMetrics displayMetrics = this.getResources().getDisplayMetrics();
        width = displayMetrics.widthPixels;

        initData(fromIntent);
        // 加载电影详情数据
        new LoadDetailTask().execute(getDetailUrl(movieId));
    }

    private void initData(Intent fromIntent){
        if (fromIntent.hasExtra("movie")){
            Movie movie = fromIntent.getParcelableExtra("movie");
            if (movie == null) {
                return;
            }
            movieId = movie.getId();
            Transformation transformation = new Transformation() {
                @Override
                public Bitmap transform(Bitmap source) {
                    if (source.getWidth() == 0) {
                        return source;
                    }
                    int imgWidth = source.getWidth();
                    int imgHeight = source.getHeight()* width/imgWidth;
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
                    return "transformation";
                }
            };
            String backdropPath = UrlUtils.IMAGE_DETAIL_URL + movie.getBackdropPath();
            Picasso.with(this)
                    .load(backdropPath).transform(transformation)
                    .placeholder(R.mipmap.loading)
                    .error(R.mipmap.ic_error)
                    .into(mBackdropImageView);
            mTitleTextView.setText(movie.getTitle());
            String posterPath = UrlUtils.IMAGE_DETAIL_URL + movie.getPosterPath();
            Picasso.with(this)
                    .load(posterPath).transform(transformation)
                    .placeholder(R.mipmap.loading)
                    .error(R.mipmap.ic_error)
                    .into(mPosterImageView);
            mOverviewTextView.setText(movie.getOverview());
            mVoteAverageTextView.setText(movie.getVoteAverage());
            mReleaseDateTextView.setText(getResources().getString(R.string.label_release_date)+movie.getReleaseDate());
        }

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
            if (movieDetail != null) {
                mMovieRuntimeTextView.setText(getResources().getString(R.string.label_runtime)
                        +String.valueOf(movieDetail.getRuntime())
                        +getResources().getString(R.string.label_runtime_unit));
                mMovieGenreTextView.setText(getResources().getString(R.string.label_genre)
                        +movieDetail.getGenresStr());
            }
        }
    }


}
