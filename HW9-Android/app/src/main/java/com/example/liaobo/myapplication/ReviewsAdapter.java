package com.example.liaobo.myapplication;

import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.StrictMode;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import org.json.JSONArray;

import java.net.URL;
import java.util.ArrayList;

public class ReviewsAdapter extends RecyclerView.Adapter<ReviewsAdapter.ItemViewHolder> {
    private ArrayList<Review> mDataset;
    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public ImageView profileImageView;
        public TextView nameTextView;
        public RatingBar ratingBar;
        public TextView dateTextView;
        public TextView contentTextView;
        public ItemViewHolder(View v) {
            super(v);
            profileImageView = (ImageView) v.findViewById(R.id.profile_num);
            nameTextView = (TextView) v.findViewById(R.id.name_num);
            ratingBar = (RatingBar) v.findViewById(R.id.reviewRatingBar);
            dateTextView = (TextView) v.findViewById(R.id.date_num);
            contentTextView = (TextView) v.findViewById(R.id.content_num);
            int SDK_INT = android.os.Build.VERSION.SDK_INT;
            if (SDK_INT > 8)
            {
                StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                        .permitAll().build();
                StrictMode.setThreadPolicy(policy);
                //your codes here
            }
        }
    }

    public interface OnItemClickListener
    {
        void onItemClick(View view, int position);
    }

    private ReviewsAdapter.OnItemClickListener mOnItemClickLitener;

    public void setOnItemClickListener(ReviewsAdapter.OnItemClickListener mOnItemClickLitener)
    {
        this.mOnItemClickLitener = mOnItemClickLitener;
    }


    // Provide a suitable constructor (depends on the kind of dataset)
    public ReviewsAdapter(ArrayList<Review> myDataset) {
        mDataset = myDataset;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ReviewsAdapter.ItemViewHolder onCreateViewHolder(ViewGroup parent,
                                                            int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_review, parent, false);
        // set the view's size, margins, paddings and layout parameters
        // ...
        ReviewsAdapter.ItemViewHolder vh = new ReviewsAdapter.ItemViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final ReviewsAdapter.ItemViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        try {
            String name = mDataset.get(position).name;

            if (!mDataset.get(position).img.equals("null")) {
                URL imgUrl = new URL(mDataset.get(position).img);
                Bitmap bitmap = BitmapFactory.decodeStream(imgUrl.openConnection().getInputStream());
                holder.profileImageView.setImageBitmap(bitmap);
            }
            holder.nameTextView.setText(mDataset.get(position).name);
//            Log.i("date",mDataset.get(position).date);
//            Log.i("content",mDataset.get(position).content);
            holder.dateTextView.setText(mDataset.get(position).date);
            holder.contentTextView.setText(mDataset.get(position).content);
            holder.ratingBar.setRating(mDataset.get(position).rating);
            holder.itemView.setTag(position);

        }
        catch (Exception e) {
            Log.i("ReviewsAdapter","JSONException", e);
        }

        if (mOnItemClickLitener != null) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = holder.getLayoutPosition();
                    mOnItemClickLitener.onItemClick(holder.itemView, pos);
                }
            });
        }

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}
