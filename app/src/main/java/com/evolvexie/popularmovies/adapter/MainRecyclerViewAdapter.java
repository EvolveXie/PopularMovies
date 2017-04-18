package com.evolvexie.popularmovies.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.evolvexie.popularmovies.R;
import com.evolvexie.popularmovies.model.Movie;
import com.evolvexie.popularmovies.utils.UrlUtils;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by orlog on 2017/4/11.
 */

public class MainRecyclerViewAdapter extends RecyclerView.Adapter<MainRecyclerViewAdapter.MovieViewHolder> {

    private static final String TAG = MainRecyclerViewAdapter.class.getSimpleName();
    private List<Movie> movies;
    private Context context;

    public MainRecyclerViewAdapter() {

    }

    @Override
    public MovieViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        int layoutIdForListItem = R.layout.layout_movie_list_item;
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;
        View view = layoutInflater.inflate(layoutIdForListItem, parent, shouldAttachToParentImmediately);
        MovieViewHolder movieViewHolder = new MovieViewHolder(view);
        return movieViewHolder;
    }

    @Override
    public void onBindViewHolder(MovieViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder: " + position);
        holder.bind(position);
    }

    @Override
    public int getItemCount() {
        if (movies == null) {
            return 0;
        }
        return movies.size();
    }

    public class MovieViewHolder extends RecyclerView.ViewHolder {

        ImageView mListItemImageView;
        //TextView mListItemTextView;

        public MovieViewHolder(View itemView) {
            super(itemView);
            mListItemImageView = (ImageView) itemView.findViewById(R.id.list_item_movie_image);
            //mListItemTextView = (TextView) itemView.findViewById(R.id.tv_movie_title_display);
        }

        void bind(int listIndex) {
            String posterPath = UrlUtils.IMAGE_BASE_URL + movies.get(listIndex).getPosterPath();
            // mListItemTextView.setText(movies.get(listIndex).getTitle());
            Picasso.with(context).load(posterPath).into(mListItemImageView);

        }
    }

    public void setMovies(List<Movie> movies) {
        this.movies = movies;
        notifyDataSetChanged();
    }

    public void addMovies(List<Movie> movies) {
        this.movies.addAll(movies);
        notifyDataSetChanged();
    }
}
