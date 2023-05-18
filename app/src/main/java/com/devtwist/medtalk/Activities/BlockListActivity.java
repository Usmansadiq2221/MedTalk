package com.devtwist.medtalk.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.os.Bundle;
import android.util.Log;

import com.devtwist.medtalk.Adapters.BlockedListAdapter;
import com.devtwist.medtalk.Adapters.FeedbackAdapter;
import com.devtwist.medtalk.Models.BlockListData;
import com.devtwist.medtalk.Models.FeedbackData;
import com.devtwist.medtalk.R;
import com.devtwist.medtalk.databinding.ActivityBlockListBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class BlockListActivity extends AppCompatActivity {

    private ActivityBlockListBinding binding;

    private String myId;
    private ArrayList<BlockListData> blockedUsersList;

    private BlockedListAdapter blockedListAdapter;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBlockListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        myId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        blockedUsersList = new ArrayList<>(); //initialize Array List
        blockedListAdapter = new BlockedListAdapter(this, blockedUsersList); //initializingAdapter

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        binding.preBlockedUView.setLayoutManager(linearLayoutManager); //setting Layout manager for recycle vier
        binding.preBlockedUView.setAdapter(blockedListAdapter);
        binding.preBlockedUView.showShimmerAdapter();

        readBlockList();//calling function to load into list for recycler view adapter


    }

    private void readBlockList() {
        try {
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            database.getReference().child("BlockList").child(myId).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    blockedUsersList.clear();
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        BlockListData blockListData = dataSnapshot.getValue(BlockListData.class);
                        blockedUsersList.add(blockListData);
                    }

                    //hiding the loading animation after blocked users retrieved data retrieved...
                    binding.preBlockedUView.hideShimmerAdapter();

                    //notify the blockedListAdapter if any changes occurs in the calls data in realtime database...
                    blockedListAdapter.notifyDataSetChanged();

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        } catch (Exception e) {
            Log.i("Call Error", e.getMessage().toString());
        }

    }
}