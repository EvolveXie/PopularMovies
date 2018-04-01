package com.evolvexie.popularmovies.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
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
import com.squareup.picasso.Transformation;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by orlog on 2017/4/11.
 */

public class MainRecyclerViewAdapter extends RecyclerView.Adapter<MainRecyclerViewAdapter.MovieViewHolder> {

    private static final String TAG = MainRecyclerViewAdapter.class.getSimpleName();
    private List<Movie> movies;
    private Context context;
    private int width = 0; // screen width

    private MainRvAdapterClickHandler mClickHandler;
    public interface MainRvAdapterClickHandler {
        void onClick(Movie movie);
    }

    public MainRecyclerViewAdapter() {
        movies = null;
    }

    public MainRecyclerViewAdapter(MainRvAdapterClickHandler clickHandler){
        movies = null;
        this.mClickHandler = clickHandler;
    }

    @Override
    public MovieViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        width = displayMetrics.widthPixels;

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
        //holder.setIsRecyclable(false);
        // 设置tag，在holder被回收时要取消后台图片加载
        holder.mListItemImageView.setTag(movies.get(holder.getAdapterPosition()).getPosterPath());
        holder.bind(holder.getAdapterPosition());

    }

    @Override
    public int getItemCount() {
        if (movies == null) {
            return 0;
        }
        return movies.size();
    }

    public class MovieViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ImageView mListItemImageView;
        TextView mListItemTitle;
        TextView mListItemContent;

        public MovieViewHolder(View itemView) {
            super(itemView);
            mListItemImageView = (ImageView) itemView.findViewById(R.id.list_item_movie_image);
            mListItemTitle = (TextView) itemView.findViewById(R.id.list_item_movie_title);
            mListItemContent = (TextView) itemView.findViewById(R.id.list_item_movie_content);
            itemView.setOnClickListener(this);
        }

        void bind(int listIndex) {
            String backDropPath = UrlUtils.IMAGE_BASE_URL + movies.get(listIndex).getBackdropPath();
            mListItemTitle.setText(movies.get(listIndex).getTitle());
            mListItemContent.setText(movies.get(listIndex).getOverview());
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
            Picasso.with(context).load(backDropPath)
                    .transform(transformation)
                    .placeholder(R.drawable.movie_temp_image)
                    .error(R.mipmap.ic_error)
                    .tag(movies.get(getAdapterPosition()).getBackdropPath())
                    .into(mListItemImageView);
        }

        @Override
        public void onClick(View v) {
            mClickHandler.onClick(movies.get(getAdapterPosition()));
        }
    }

    @Override
    public void onViewRecycled(MovieViewHolder holder) {
        super.onViewRecycled(holder);
        Picasso.with(context).cancelTag(holder.mListItemImageView.getTag());
    }

    public void setMovies(List<Movie> movies) {
        this.movies = movies;
        notifyDataSetChanged();
    }

    public void addMovies(List<Movie> movies) {
        if (this.movies == null) {
            this.movies = movies;
            notifyDataSetChanged();
        } else {
            this.movies.addAll(movies);
            notifyDataSetChanged();
        }
        Log.d(TAG, "addMovies: #####################"+movies.size());
        Log.d(TAG, "addMovies: #####################"+this.movies.size());
    }
}
