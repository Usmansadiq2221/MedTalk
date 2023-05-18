package com.devtwist.medtalk.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.os.Bundle;
import android.view.View;

import com.devtwist.medtalk.Adapters.AppointmentAdapter;
import com.devtwist.medtalk.Models.AppointmentListData;
import com.devtwist.medtalk.databinding.ActivityViewHistoryBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Comparator;

public class ViewHistoryActivity extends AppCompatActivity {

    private ActivityViewHistoryBinding binding;

    private ArrayList<AppointmentListData> appointmentList;
    private AppointmentAdapter appointmentAdapter;

    private String myId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityViewHistoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        myId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        appointmentList = new ArrayList<>();

        // initializing AppointmentAdapter class and also pass the values to constructor to load data in _preNotifyView(Recyclerview)...
        appointmentAdapter = new AppointmentAdapter(this, appointmentList, myId);

        binding.backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        //set up layout manager for _preNotifyView(Recycler View)...
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        binding.preAppointView.setLayoutManager(linearLayoutManager);
        binding.preAppointView.setAdapter(appointmentAdapter);

        // show the animation while notification data retrieving from realtime database...
        binding.preAppointView.showShimmerAdapter();

        //this function retrieve the data of Appointments from realtime database
        readAppointHistoryList();



    }

    private void readAppointHistoryList() {
        FirebaseDatabase.getInstance().getReference().child("AppointList").child(myId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        appointmentList.clear();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                            AppointmentListData appointmentListData = dataSnapshot.getValue(AppointmentListData.class);
                            if (appointmentListData.getAppointStatus().equals("Completed")) {
                                appointmentList.add(appointmentListData);
                            }
                        }
                        appointmentList.sort(Comparator.comparing(AppointmentListData::getTimestamp));
                        binding.preAppointView.hideShimmerAdapter();
                        binding.errorText.setVisibility(View.GONE);
                        binding.preAppointView.setVisibility(View.VISIBLE);
                        appointmentAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        binding.errorText.setVisibility(View.VISIBLE);
                    }
                });

    }


}