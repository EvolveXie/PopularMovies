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

import com.evolvexie.popularmovies.BuildConfig;
import com.evolvexie.popularmovies.R;
import com.evolvexie.popularmovies.model.Movie;
import com.evolvexie.popularmovies.utils.UrlUtils;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import java.util.List;

/**
 * Created by orlog on 2017/4/11.
 */

public class MainRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = MainRecyclerViewAdapter.class.getSimpleName();
    private List<Movie> movies;
    private Context context;
    private int width = 0; // screen width
    private int load_more_status = BuildConfig.MAIN_PULLUP_LOAD_MORE;

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
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        width = displayMetrics.widthPixels;
        boolean shouldAttachToParentImmediately = false;
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        if (viewType == BuildConfig.MAIN_ITEM_TYPE) {
            int layoutIdForListItem = R.layout.layout_movie_list_item;
            View view = layoutInflater.inflate(layoutIdForListItem, parent, shouldAttachToParentImmediately);
            MovieViewHolder movieViewHolder = new MovieViewHolder(view);
            return movieViewHolder;
        } else if (viewType == BuildConfig.MAIN_FOOTER_TYPE) {
            int layoutIdForFooter = R.layout.layout_movie_list_footer;
            View view = layoutInflater.inflate(layoutIdForFooter,parent,shouldAttachToParentImmediately);
            FooterViewHolder footerViewHolder = new FooterViewHolder(view);
            return footerViewHolder;
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder: " + position);
        if (holder instanceof MovieViewHolder){
            MovieViewHolder movieViewHolder = (MovieViewHolder) holder;
            //holder.setIsRecyclable(false);
            // 设置tag，在holder被回收时要取消后台图片加载
            movieViewHolder.mListItemImageView.setTag(movies.get(holder.getAdapterPosition()).getPosterPath());
            movieViewHolder.bind(holder.getAdapterPosition());
        } else if (holder instanceof FooterViewHolder) {
            FooterViewHolder footerViewHolder = (FooterViewHolder) holder;
            if (load_more_status == BuildConfig.MAIN_PULLUP_LOAD_MORE) {
                footerViewHolder.mFooterTitle.setText(context.getResources().getString(R.string.loading_more_tip));
            } else if (load_more_status == BuildConfig.MAIN_LOADING_MORE) {
                footerViewHolder.mFooterTitle.setText(context.getResources().getString(R.string.loading_more));
            }
        }
    }

    @Override
    public int getItemCount() {
        if (movies == null) {
            return 0;
        }
        return movies.size()+1;
    }

    public class FooterViewHolder extends RecyclerView.ViewHolder {
        TextView mFooterTitle;
        public FooterViewHolder(View itemView) {
            super(itemView);
            mFooterTitle = itemView.findViewById(R.id.tv_footer_title);
        }
    }

    public class MovieViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ImageView mListItemImageView;
        TextView mListItemTitle;
        TextView mListItemContent;

        public MovieViewHolder(View itemView) {
            super(itemView);
            mListItemImageView = itemView.findViewById(R.id.list_item_movie_image);
            mListItemTitle = itemView.findViewById(R.id.list_item_movie_title);
            mListItemContent = itemView.findViewById(R.id.list_item_movie_content);
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
            if (mClickHandler != null) {
                mClickHandler.onClick(movies.get(getAdapterPosition()));
            }
        }
    }

    @Override
    public void onViewRecycled(RecyclerView.ViewHolder holder) {
        super.onViewRecycled(holder);
        if (holder instanceof MovieViewHolder) {
            MovieViewHolder movieViewHolder = (MovieViewHolder) holder;
            Picasso.with(context).cancelTag(movieViewHolder.mListItemImageView.getTag());
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position == getItemCount()-1) {
            return BuildConfig.MAIN_FOOTER_TYPE;
        }else {
            return BuildConfig.MAIN_ITEM_TYPE;
        }
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

    public void changeLoadMoreStatus(int status){
        load_more_status = status;
        notifyDataSetChanged();
    }
}
