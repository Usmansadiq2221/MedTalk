package com.devtwist.medtalk.Adapters;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.devtwist.medtalk.Activities.ViewDocProfile;
import com.devtwist.medtalk.Models.DoctorData;
import com.devtwist.medtalk.Models.SavedDocData;
import com.devtwist.medtalk.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;

public class DoctorAdapter extends RecyclerView.Adapter<DoctorAdapter.PostViewHolder> {

    //used to store the values passed from PostActivity or SellerProfileActivity or USerProfileActivity...
    private String postUserIdString, myId, postUsername, profileUri, token;
    private DatabaseReference docRefrence;
    private Context context;
    private ArrayList<DoctorData> docList;

    //initializing the list and variables and list in constructor passed from PostActivity or SellerProfileActivity or USerProfileActivity...
    public DoctorAdapter(Context context, ArrayList<DoctorData> docList, String myId) {
        this.context = context;
        this.docList = docList;
        this.myId = myId;
    }

    // We are inflating the post_items
    // inside on Create View Holder method...
    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.doc_list_items, parent, false);
        return new PostViewHolder(view);
    }

    // Inside on bind view holder we will
    // set data to item of Recycler View...
    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        try {
            Log.i("PostAdapter","successfull");
            DoctorData model = docList.get(position);
            if (model.getProfileUrl().length()>1) {
                Picasso.get().load(model.getProfileUrl()).placeholder(R.drawable.enter_profile_icon).into(holder.docListProfile);
            }
            postUserIdString = model.getUserId();
            holder.docListUsername.setText(model.getUsername());
            holder.docListCategory.setText(model.getCategory());
            holder.docListHospital.setText(model.getHospital());
            holder.docListPatients.setText(model.getPatients()+"");
            holder.docListExp.setText(model.getExperience());
            holder.docListRating.setText(model.getRating()+"");

            holder.favIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    holder.favIcon.setVisibility(View.GONE);
                    holder.docSaveProgress.setVisibility(View.VISIBLE);
                    Date date = new Date();
                    SavedDocData savedDocData = new SavedDocData(model.getUserId(), model.getUserId(), date.getTime());
                    FirebaseDatabase.getInstance().getReference().child("SavedDoctors").child(myId)
                            .child(model.getUserId()).setValue(savedDocData).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Toast.makeText(context, "Saved Successfully", Toast.LENGTH_SHORT).show();
                                    holder.favIcon.setImageResource(R.drawable.ic_baseline_favorite_24);
                                    holder.docSaveProgress.setVisibility(View.GONE);
                                    holder.favIcon.setVisibility(View.VISIBLE);
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(context, "Network Problem\nPlease Check your connection!", Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            });


            FirebaseDatabase.getInstance().getReference().child("SavedDoctors").child(myId).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot dataSnapshot: snapshot.getChildren()){
                        SavedDocData savedDocData = dataSnapshot.getValue(SavedDocData.class);
                        if (savedDocData.getDocId().equals(model.getUserId())) {
                            holder.favIcon.setImageResource(R.drawable.ic_baseline_favorite_24);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, ViewDocProfile.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("userId", model.getUserId());
                    bundle.putString("name", model.getUsername());
                    bundle.putString("profile", model.getProfileUrl());
                    bundle.putString("rate", model.getSessionPrice());
                    bundle.putString("category", model.getCategory());
                    bundle.putString("info", model.getBiography());
                    bundle.putString("patients", model.getPatients()+"");
                    bundle.putString("experience", model.getExperience());
                    bundle.putString("rating", model.getRating()+"");
                    bundle.putString("token", model.getToken());
                    intent.putExtras(bundle);
                    context.startActivity(intent);
                }
            });

        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    // this method will return the count of our list...
    @Override
    public int getItemCount() {
        return docList.size();
    }




    class PostViewHolder extends RecyclerView.ViewHolder{

        // Adapter class for initializing
        // the views of our post_items view...
        private ImageView docListProfile, favIcon;
        private TextView docListUsername, docListCategory, docListHospital, docListPatients, docListExp, docListRating;
        private ProgressBar docSaveProgress;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);

            try {
                docListProfile = (ImageView) itemView.findViewById(R.id.docListProfile);
                favIcon = (ImageView) itemView.findViewById(R.id.favIcon);
                docListUsername = (TextView) itemView.findViewById(R.id.docListUsername);
                docListCategory = (TextView) itemView.findViewById(R.id.docListCategory);
                docListHospital = (TextView) itemView.findViewById(R.id.docListHospital);
                docListPatients = (TextView) itemView.findViewById(R.id.docListPatients);
                docListExp = (TextView) itemView.findViewById(R.id.docListExp);
                docListRating = (TextView) itemView.findViewById(R.id.docListRating);
                docSaveProgress = (ProgressBar) itemView.findViewById(R.id.docSaveProgress);

            } catch (Exception e) {
                e.printStackTrace();
                Log.i("PostViewHolder Error", e.getMessage().toString());
            }

        }
    }
}
