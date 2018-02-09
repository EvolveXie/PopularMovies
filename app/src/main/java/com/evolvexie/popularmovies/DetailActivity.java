package com.evolvexie.popularmovies;

import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.widget.ImageView;
import android.widget.TextView;

import com.evolvexie.popularmovies.model.Movie;
import com.evolvexie.popularmovies.utils.UrlUtils;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

public class DetailActivity extends AppCompatActivity {

    private TextView mTitleTextView;
    private TextView mReleaseDateTextView;
    private TextView mOverviewTextView;
    private TextView mVoteAverageTextView;
    private ImageView mPosterImageView;
    private ImageView mBackdropImageView;

    private int width; // screen width

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        Intent fromIntent = getIntent();
        DisplayMetrics displayMetrics = this.getResources().getDisplayMetrics();
        width = displayMetrics.widthPixels;

        mTitleTextView = (TextView) findViewById(R.id.tv_movie_title);
        mReleaseDateTextView = (TextView) findViewById(R.id.tv_movie_release_date);
        mOverviewTextView = (TextView) findViewById(R.id.tv_movie_overview);
        mVoteAverageTextView = (TextView) findViewById(R.id.tv_movie_vote_average);
        mPosterImageView = (ImageView) findViewById(R.id.iv_poster);
        mBackdropImageView = (ImageView) findViewById(R.id.iv_backdrop);

        initData(fromIntent);
    }

    private void initData(Intent fromIntent){
        if (fromIntent.hasExtra("movie")){
            Movie movie = fromIntent.getParcelableExtra("movie");
            if (movie == null) {
                return;
            }
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
            mReleaseDateTextView.setText(movie.getReleaseDate());
        }

    }
}
