package com.evolvexie.popularmovies.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.evolvexie.popularmovies.R;
import com.evolvexie.popularmovies.model.Review;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * Created by hand on 2018/3/27.
 */

public class ReviewRecyclerViewAdapter extends RecyclerView.Adapter<ReviewRecyclerViewAdapter.ReviewViewHolder> {

    private List<Review> reviews;
    private Context mContext;
    private int width = 0; // screen width

    public ReviewRecyclerViewAdapter(List<Review> reviews){
        this.reviews = reviews;
    }

    @Override
    public ReviewViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.layout_movie_review_list_item,parent,false);
        ReviewViewHolder holder = new ReviewViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(ReviewViewHolder holder, int position) {
        holder.bind(position);
    }

    @Override
    public int getItemCount() {
        return this.reviews.size();
    }

    class ReviewViewHolder extends RecyclerView.ViewHolder {

        TextView mReviewContent;
        TextView mReviewAuthor;
        TextView mReviewContentOpen;

        public ReviewViewHolder(View itemView) {
            super(itemView);
            mReviewContent = (TextView) itemView.findViewById(R.id.rv_item_review_content);
            mReviewAuthor = (TextView) itemView.findViewById(R.id.rv_item_review_author);
            mReviewContentOpen = (TextView) itemView.findViewById(R.id.tv_open_review_content);
            mReviewContentOpen.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if ("open".equals(mReviewContentOpen.getTag())) { // 如果是展开状态，则收起
                        mReviewContentOpen.setText(mContext.getResources().getString(R.string.open_review_content));
                        mReviewContentOpen.setTag("close");
                        mReviewContent.setMaxLines(3);
                        mReviewContent.setEllipsize(TextUtils.TruncateAt.END);
                    }else {
                        mReviewContentOpen.setText(mContext.getResources().getString(R.string.close_review_content));
                        mReviewContentOpen.setTag("open");
                        mReviewContent.setMaxLines(Integer.MAX_VALUE);
                    }
                }
            });
        }

        public void bind(int listPosition){
            Review review = reviews.get(listPosition);
            mReviewContent.setText(review.getContent());
            mReviewAuthor.setText(review.getAuthor());
        }
    }

    public void setDatas(List<Review> datas){
        this.reviews = datas;
        notifyDataSetChanged();
    }
}
