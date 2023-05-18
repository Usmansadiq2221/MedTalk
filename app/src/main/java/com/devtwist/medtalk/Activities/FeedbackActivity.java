package com.devtwist.medtalk.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.devtwist.medtalk.Adapters.FeedbackAdapter;
import com.devtwist.medtalk.Models.FeedbackData;
import com.devtwist.medtalk.databinding.ActivityFeedbackBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Comparator;

public class FeedbackActivity extends AppCompatActivity {

    private ActivityFeedbackBinding binding;

    private String myId;
    private ArrayList<FeedbackData> feedbackDataList;
    private FeedbackAdapter feedbackAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFeedbackBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        myId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        feedbackDataList = new ArrayList<>(); //initialize Array List
        feedbackAdapter = new FeedbackAdapter(this, feedbackDataList); //initializingAdapter

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        binding.preFeedbackView.setLayoutManager(linearLayoutManager); //setting Layout manager for recycle vier
        binding.preFeedbackView.setAdapter(feedbackAdapter);
        binding.preFeedbackView.showShimmerAdapter();

        readReviewList();//calling function to load into list for recycler view adapter

    }

    private void readReviewList() {
        try {
            FirebaseDatabase.getInstance().getReference().child("Feedback").child(myId)
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            feedbackDataList.clear();
                            for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                                FeedbackData feedbackData = dataSnapshot.getValue(FeedbackData.class);
                                feedbackDataList.add(feedbackData);
                                Log.i("ReviewStatus", "Successfull");
                            }
                            if (feedbackDataList.size()<1){
                                binding.preFeedbackView.setVisibility(RecyclerView.GONE);
                                binding.noReviewText.setVisibility(TextView.VISIBLE);
                            }

                            feedbackDataList.sort(Comparator.comparing(FeedbackData::getTimestamp));
                            binding.preFeedbackView.hideShimmerAdapter();
                            feedbackAdapter.notifyDataSetChanged();

                        }


                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
            Log.i("feedback Eror",e.getMessage().toString());
        }
    }


}