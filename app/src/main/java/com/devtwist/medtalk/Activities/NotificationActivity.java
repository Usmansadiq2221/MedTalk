package com.devtwist.medtalk.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;

import com.cooltechworks.views.shimmer.ShimmerRecyclerView;
import com.devtwist.medtalk.Adapters.NotificationAdapter;
import com.devtwist.medtalk.Models.NotificationData;
import com.devtwist.medtalk.databinding.ActivityNotificationBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Comparator;

public class NotificationActivity extends AppCompatActivity {

    ActivityNotificationBinding binding;
    // store the user id of the current user...
    private String myId, accType;
    private Intent intent;

    // store the list of previous notification...
    private ArrayList<NotificationData> notificationList;

    // used to load the data in _preNotifyView(RecyclerView) from notificationList;
    private NotificationAdapter notificationAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNotificationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        myId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        SharedPreferences preferences = getSharedPreferences("MedTalkData", MODE_PRIVATE);
        accType = preferences.getString("accType", "");

        try {
            notificationList = new ArrayList<>();

            // initializing NotificationAdapter class and also pass the values to constructor to load data in _preNotifyView(Recyclerview)...
            notificationAdapter = new NotificationAdapter(this, notificationList, myId);

            //set up layout manager for _preNotifyView(Recycler View)...
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
            linearLayoutManager.setReverseLayout(true);
            linearLayoutManager.setStackFromEnd(true);
            binding.preNotifyView.setLayoutManager(linearLayoutManager);
            binding.preNotifyView.setAdapter(notificationAdapter);

            // show the animation while notification data retrieving from realtime database...
            binding.preNotifyView.showShimmerAdapter();

            //retrieve the list of notification on the basis of search when any changes occurs in _searchNotification(EditText)...
            binding.searchNotification.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    readNotificationList();
                }

                @Override
                public void afterTextChanged(Editable editable) {

                }
            });

            // this function will retrieve the list of notification
            readNotificationList();

        }
        catch (Exception e) {
            e.printStackTrace();
            Log.i("Block List Error", e.getMessage().toString());
        }

        binding.backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });


    }


    private void readNotificationList() {
        FirebaseDatabase.getInstance().getReference().child("Notifications").child(myId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        // retrieve the list of all notifications of the users if user didn't enter any value in _searchNotification...
                        if (binding.searchNotification.getText().length()<1) {
                            notificationList.clear();
                            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                NotificationData notificationData = dataSnapshot.getValue(NotificationData.class);
                                notificationList.add(notificationData);
                            }
                            notificationList.sort(Comparator.comparing(NotificationData::getTimestamp));
                            binding.preNotifyView.hideShimmerAdapter();
                            notificationAdapter.notifyDataSetChanged();
                        }
                        else{
                            // retrieve the list of only those notifications of the users which matches the entered value in _searchNotification...
                            notificationList.clear();
                            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                NotificationData notificationData = dataSnapshot.getValue(NotificationData.class);
                                if (notificationData.getNotificationTitle().toLowerCase()
                                        .contains(binding.searchNotification.getText().toString().toLowerCase())) {
                                    notificationList.add(notificationData);
                                }
                            }
                            //sort the list of notification data on the basis of time...
                            notificationList.sort(Comparator.comparing(NotificationData::getTimestamp));

                            // hide the loading animation...
                            binding.preNotifyView.hideShimmerAdapter();

                            //notify the Adapter if any changes occurs in the data of the notification in firebase...
                            notificationAdapter.notifyDataSetChanged();
                        }



                    }


                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }


}