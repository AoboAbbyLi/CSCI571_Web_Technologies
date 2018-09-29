package com.example.liaobo.myapplication;

import android.content.Context;
import android.content.SharedPreferences;
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
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.liaobo.myapplication.databinding.ItemResultBinding;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URL;

import static android.content.Context.MODE_PRIVATE;
import static com.example.liaobo.myapplication.BR.place;

public class ResultsAdapter extends RecyclerView.Adapter<ResultsAdapter.ItemViewHolder> {
//    private String[] mDataset;
    private JSONArray mDataset;
    SharedPreferences prefs;
    SharedPreferences.Editor editor;
    public static final String  MY_PREFS_NAME = "favorites";
    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public ImageView iconImageView;
        public TextView nameTextView;
        public TextView addressTextView;
        public ImageButton favImageButton;
        private ItemResultBinding mBinding;
        public ItemViewHolder(View v) {
            super(v);
            iconImageView = (ImageView) v.findViewById(R.id.icon_num);
            nameTextView = (TextView) v.findViewById(R.id.name_num);
            addressTextView = (TextView) v.findViewById(R.id.address_num);
            favImageButton = (ImageButton) v.findViewById(R.id.fav_num);
            int SDK_INT = android.os.Build.VERSION.SDK_INT;
            if (SDK_INT > 8)
            {
                StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                        .permitAll().build();
                StrictMode.setThreadPolicy(policy);
                //your codes here

            }
            mBinding = DataBindingUtil.bind(v);
        }
        public ItemViewHolder setBinding(Place place){
            mBinding.setVariable(BR.place , place);
            mBinding.executePendingBindings();
            return this;
        }
    }

    public interface OnItemClickLitener
    {
        void onItemClick(View view, int position);
    }
    public interface OnFavClickLitener
    {
        void onFavClick(View view, int position);
    }

    private OnItemClickLitener mOnItemClickLitener;

    public void setOnItemClickLitener(OnItemClickLitener mOnItemClickLitener)
    {
        this.mOnItemClickLitener = mOnItemClickLitener;
    }
    private OnFavClickLitener mOnFavClickLitener;

    public void setOnFavClickLitener(OnFavClickLitener mOnFavClickLitener)
    {
        this.mOnFavClickLitener = mOnFavClickLitener;
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public ResultsAdapter(Context context, JSONArray myDataset) {
        mDataset = myDataset;
        prefs = context.getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        editor = prefs.edit();
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ResultsAdapter.ItemViewHolder onCreateViewHolder(ViewGroup parent,
                                                        int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_result, parent, false);
        // set the view's size, margins, paddings and layout parameters
        // ...
        ItemViewHolder vh = new ItemViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final ItemViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        try {
            String id = mDataset.getJSONObject(position).getString("place_id");
            String icon = mDataset.getJSONObject(position).getString("icon");
            String name = mDataset.getJSONObject(position).getString("name");
            String address = mDataset.getJSONObject(position).getString("vicinity");
            Place place = new Place(id, icon, name, address, false);

            URL newurl = new URL(mDataset.getJSONObject(position).getString("icon"));
            Bitmap bitmap = BitmapFactory.decodeStream(newurl.openConnection().getInputStream());
            holder.iconImageView.setImageBitmap(bitmap);
//            holder.nameTextView.setText(mDataset.getJSONObject(position).getString("name"));
//            holder.addressTextView.setText(mDataset.getJSONObject(position).getString("vicinity"));
            if (prefs.contains(id)) {
                holder.favImageButton.setImageResource(R.drawable.heart_fill_red);
            }
            else {
                holder.favImageButton.setImageResource(R.drawable.heart_outline_black);
            }
            holder.setBinding(place);
            holder.itemView.setTag(position);

        }
        catch (Exception e) {
            Log.i("ResultsAdapter","JSONException");
        }

        if (mOnItemClickLitener != null) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = holder.getLayoutPosition();
                    mOnItemClickLitener.onItemClick(holder.itemView, pos);
                }
            });
            holder.favImageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = holder.getLayoutPosition();
                    mOnFavClickLitener.onFavClick(holder.itemView, pos);
                }
            });
        }

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.length();
    }
}


