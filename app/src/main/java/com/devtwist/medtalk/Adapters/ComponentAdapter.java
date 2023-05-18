package com.devtwist.medtalk.Adapters;

import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.devtwist.medtalk.Activities.BlockListActivity;
import com.devtwist.medtalk.Activities.EditPatientProfile;
import com.devtwist.medtalk.Activities.EditProfileActivity;
import com.devtwist.medtalk.Activities.EditStudentProfile;
import com.devtwist.medtalk.Activities.FeedbackActivity;
import com.devtwist.medtalk.Activities.LoginOptionActivity;
import com.devtwist.medtalk.Activities.SearchDoctorActivity;
import com.devtwist.medtalk.Activities.SettingsActivity;
import com.devtwist.medtalk.Activities.ViewHistoryActivity;
import com.devtwist.medtalk.Activities.ViewUserDetails;
import com.devtwist.medtalk.R;
import com.google.firebase.auth.FirebaseAuth;

public class ComponentAdapter extends RecyclerView.Adapter<ComponentAdapter.ComponentViewHolder> {

    //for storing context of the activity passed from PostActivity...
    private Context mContext;

    //for storing the list of image list data...
    private int[] componentImageList;

    //for storing the list of text list data...
    private String[] componentNameList;


    //initializing the variables and list in constructor passed from PostActivity...
    public ComponentAdapter(Context mContext, int[] componentImageList, String[] componentNameList) {
        this.mContext = mContext;
        this.componentImageList = componentImageList;
        this.componentNameList = componentNameList;
    }

    // We are inflating the category_items
    // inside on Create View Holder method...
    @NonNull
    @Override
    public ComponentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.component_items, parent, false);
        return new ComponentViewHolder(view);
    }

    // Inside on bind view holder we will
    // set data to item of Recycler View...

    @Override
    public void onBindViewHolder(@NonNull ComponentViewHolder holder, @SuppressLint("RecyclerView") int position) {

        SharedPreferences accPreferences = mContext.getSharedPreferences("MedTalkData", MODE_PRIVATE);
        String accType = accPreferences.getString("accType", "");

        if (position==3 && !accType.equals("Doctor")){
            holder._componentTextItem.setText("Blocklist");
            holder._componmnentImage.setImageResource(R.drawable.blocklist_icon);
        }else {
            holder._componmnentImage.setImageResource(componentImageList[position]);
            holder._componentTextItem.setText(componentNameList[position]);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //if personal detail item...
                if (position==0){
                    mContext.startActivity(new Intent(mContext, ViewUserDetails.class));
                }

                //if edit personal detail item...
                else if (position==1) {

                    if (accType.equals("Doctor")) {
                        mContext.startActivity(new Intent(mContext, EditProfileActivity.class));
                    }
                    else if (accType.equals("Student")){
                        mContext.startActivity(new Intent(mContext, EditStudentProfile.class));
                    }
                    else if (accType.equals("Patient")){
                        mContext.startActivity(new Intent(mContext, EditPatientProfile.class));
                    }
                }

                //if history item...
                else if (position == 2) {
                    mContext.startActivity(new Intent(mContext, ViewHistoryActivity.class));
                }

                //if feedback item...
                else if (position == 3) {
                    if (accType.equals("Doctor")){
                        mContext.startActivity(new Intent(mContext, FeedbackActivity.class));
                    }
                    else {
                        mContext.startActivity(new Intent(mContext, BlockListActivity.class));
                    }

                }

                //if setting item...
                else if (position == 4) {
                    mContext.startActivity(new Intent(mContext, SettingsActivity.class));
                }

                //if log out item...
                else if (position == 5) {
                    SharedPreferences preferences = mContext.getSharedPreferences("MedTalkData", MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("isLogedIn", false);
                    editor.putBoolean("isProfileCreated", false);
                    editor.putString("loginType", "");
                    editor.putString("accType", "");
                    editor.commit();
                    FirebaseAuth.getInstance().signOut();
                    mContext.startActivity(new Intent(mContext, LoginOptionActivity.class));
                    ((Activity)view.getContext()).finish();
                }
            }
        });

    }

    // this method will return the count of our list...
    @Override
    public int getItemCount() {
        return componentImageList.length;
    }

    public class ComponentViewHolder extends RecyclerView.ViewHolder {

        // Adapter class for initializing
        // the views of our category_items view...

        private ImageView _componmnentImage;
        private TextView _componentTextItem;

        public ComponentViewHolder(@NonNull View itemView) {
            super(itemView);

            _componmnentImage = (ImageView) itemView.findViewById(R.id._componmnentImage);
            _componentTextItem = (TextView) itemView.findViewById(R.id._componentTextItem);
        }
    }
}
