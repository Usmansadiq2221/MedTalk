package com.devtwist.medtalk.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.devtwist.medtalk.Activities.SearchDoctorActivity;
import com.devtwist.medtalk.R;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    //for storing context of the activity passed from PostActivity...
    private Context mContext;

    //for storing the list of image list data...
    private int[] categoryImageList;

    //for storing the list of text list data...
    private String[] categoryTextList;


    //initializing the variables and list in constructor passed from PostActivity...
    public CategoryAdapter(Context mContext, int[] categoryImageList, String[] categoryTextList) {
        this.mContext = mContext;
        this.categoryImageList = categoryImageList;
        this.categoryTextList = categoryTextList;
    }

    // We are inflating the category_items
    // inside on Create View Holder method...
    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.category_items, parent, false);
        return new CategoryViewHolder(view);
    }

    // Inside on bind view holder we will
    // set data to item of Recycler View...

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, @SuppressLint("RecyclerView") int position) {
        holder._categoryImage.setImageResource(categoryImageList[position]);
        holder._categoryTextItem.setText(categoryTextList[position]);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, SearchDoctorActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("text", categoryTextList[position]);
                bundle.putInt("ImageId", categoryImageList[position]);
                intent.putExtra("searchType", "category");
                intent.putExtras(bundle);
                mContext.startActivity(intent);
            }
        });

    }

    // this method will return the count of our list...
    @Override
    public int getItemCount() {
        return categoryImageList.length;
    }

    public class CategoryViewHolder extends RecyclerView.ViewHolder {

        // Adapter class for initializing
        // the views of our category_items view...

        private ImageView _categoryImage;
        private TextView _categoryTextItem;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);

            _categoryImage = (ImageView) itemView.findViewById(R.id._categoryImage);
            _categoryTextItem = (TextView) itemView.findViewById(R.id._categoryTextItem);
        }
    }
}
