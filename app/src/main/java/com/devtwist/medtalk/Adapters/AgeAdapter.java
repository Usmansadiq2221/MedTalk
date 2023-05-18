package com.devtwist.medtalk.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
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

public class AgeAdapter extends RecyclerView.Adapter<AgeAdapter.AgeViewHolder> {

    //for storing context of the activity passed from PostActivity...
    private Context mContext;

    //for storing the list of text list data...
    private String[] ageTextList;


    //initializing the variables and list in constructor passed from PostActivity...
    public AgeAdapter(Context mContext, String[] ageTextList) {
        this.mContext = mContext;
        this.ageTextList = ageTextList;
    }

    // We are inflating the category_items
    // inside on Create View Holder method...
    @NonNull
    @Override
    public AgeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.age_item, parent, false);
        return new AgeViewHolder(view);
    }

    // Inside on bind view holder we will
    // set data to item of Recycler View...

    @Override
    public void onBindViewHolder(@NonNull AgeViewHolder holder, @SuppressLint("RecyclerView") int position) {
        holder.ageText.setText(ageTextList[position]);
        boolean agisSelected = false;
        holder.ageText.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("Range")
            @Override
            public void onClick(View view) {
                if (!agisSelected) {
                    SharedPreferences preferences = mContext.getSharedPreferences("MedTalkData", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("age", holder.ageText.getText().toString());
                    editor.commit();
                    holder.ageText.setBackgroundResource(R.drawable.orange_round_square_fill);
                    holder.ageText.setTextColor(Color.parseColor("#ffffff"));
                }
                else {
                    holder.ageText.setBackgroundResource(R.drawable.orange_round_square_rest);
                    holder.ageText.setTextColor(Color.parseColor("#FE8314"));
                }
            }
        });

    }

    // this method will return the count of our list...
    @Override
    public int getItemCount() {
        return ageTextList.length;
    }

    public class AgeViewHolder extends RecyclerView.ViewHolder {

        // Adapter class for initializing
        // the views of our category_items view...

        private TextView ageText;

        public AgeViewHolder(@NonNull View itemView) {
            super(itemView);

            ageText = (TextView) itemView.findViewById(R.id.ageText);
        }
    }
}
