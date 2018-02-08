package com.evolvexie.popularmovies;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.evolvexie.popularmovies.model.Movie;
import com.evolvexie.popularmovies.utils.UrlUtils;
import com.squareup.picasso.Picasso;

public class DetailActivity extends AppCompatActivity {

    private TextView mTitleTextView;
    private TextView mReleaseDateTextView;
    private TextView mOverviewTextView;
    private TextView mVoteAverageTextView;
    private ImageView mPosterImageView;
    private ImageView mBackdropImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Intent fromIntent = getIntent();


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
            String backdropPath = UrlUtils.IMAGE_BASE_URL + movie.getBackdropPath();
            Picasso.with(this)
                    .load(backdropPath)
                    .into(mBackdropImageView);
            mTitleTextView.setText(movie.getTitle());
            String posterPath = UrlUtils.IMAGE_BASE_URL + movie.getPosterPath();
            Picasso.with(this)
                    .load(posterPath)
                    .into(mPosterImageView);
            mOverviewTextView.setText(fromIntent.getStringExtra("overview"));
            mVoteAverageTextView.setText(fromIntent.getStringExtra("vote_average"));
            mReleaseDateTextView.setText(fromIntent.getStringExtra("release_date"));
        }

    }
}
