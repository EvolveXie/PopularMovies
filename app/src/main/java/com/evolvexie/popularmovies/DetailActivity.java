package com.evolvexie.popularmovies;

import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.evolvexie.popularmovies.adapter.ReviewRecyclerViewAdapter;
import com.evolvexie.popularmovies.data.KeyPreferences;
import com.evolvexie.popularmovies.data.MoviesContract;
import com.evolvexie.popularmovies.data.MoviesDbHelper;
import com.evolvexie.popularmovies.fragment.DetailFragment;
import com.evolvexie.popularmovies.model.Movie;
import com.evolvexie.popularmovies.model.MovieDetail;
import com.evolvexie.popularmovies.model.MovieReview;
import com.evolvexie.popularmovies.model.MovieTrailer;
import com.evolvexie.popularmovies.model.Review;
import com.evolvexie.popularmovies.model.Video;
import com.evolvexie.popularmovies.utils.NetUtils;
import com.evolvexie.popularmovies.utils.UrlUtils;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.squareup.picasso.Transformation;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DetailActivity extends AppCompatActivity {

    @BindView(R.id.detail_toolbar)
    Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent fromIntent = this.getIntent();
        if (fromIntent.hasExtra("movie")) {
            Movie movie = fromIntent.getParcelableExtra("movie");
            DetailFragment detailFragment = DetailFragment.newInstance(movie);
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.add(R.id.detail_fragment_container,detailFragment,BuildConfig.DETAIL_FRAGMENT_TAG);
            fragmentTransaction.commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //getMenuInflater().inflate(R.menu.detail,menu);
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // 返回false，让事件分发到fragment
        return false;
    }

}
