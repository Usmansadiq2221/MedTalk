package com.devtwist.medtalk.Fragments;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.devtwist.medtalk.Activities.NotificationActivity;
import com.devtwist.medtalk.Adapters.AppointmentAdapter;
import com.devtwist.medtalk.Adapters.NotificationAdapter;
import com.devtwist.medtalk.Models.AppointmentData;
import com.devtwist.medtalk.Models.AppointmentListData;
import com.devtwist.medtalk.Models.NotificationData;
import com.devtwist.medtalk.Models.PatientData;
import com.devtwist.medtalk.R;
import com.devtwist.medtalk.databinding.FragmentAppointmentBinding;
import com.devtwist.medtalk.databinding.FragmentHomeBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;


public class AppointmentFragment extends Fragment {


    private FragmentAppointmentBinding binding;
    private Context context;
    private ArrayList<AppointmentListData> appointmentList;
    private AppointmentAdapter appointmentAdapter;

    private DatabaseReference reference;

    private String myId, accType;



    public AppointmentFragment() {
        // Required empty public constructor
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentAppointmentBinding.inflate(inflater,container,false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        context = view.getContext();
        myId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        appointmentList = new ArrayList<>();

        SharedPreferences preferences = context.getSharedPreferences("MedTalkData", MODE_PRIVATE);
        accType = preferences.getString("accType", "");

        if (accType.equals("Doctor")) {
            Date date = new Date();   // given date
            Calendar calendar = GregorianCalendar.getInstance(); // creates a new calendar instance
            calendar.setTime(date);   // assigns calendar to given date
            int hour = calendar.get(Calendar.HOUR_OF_DAY); // gets hour in 24h format

            if (hour < 12 && hour > 5) {
                binding.wish.setText("Good Morning");
            }
            else if (hour > 12 && hour < 17) {
                binding.wish.setText("Good Afternoon");
            }
            else if (hour > 17 && hour < 21) {
                binding.wish.setText("Good Evening");
            }
            else {
                binding.wish.setText("Good Night");
            }


            reference = FirebaseDatabase.getInstance().getReference().child("Users");

            reference.child(accType).child(myId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    PatientData patientData = snapshot.getValue(PatientData.class);
                    binding.username.setText(patientData.getUsername());
                    if (patientData.getProfileUrl().length()>1) {
                        Picasso.get().load(patientData.getProfileUrl()).placeholder(R.drawable.enter_profile_icon).into(binding.profileimage);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

            binding.nameLayout.setVisibility(View.VISIBLE);

            binding.notifications.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    context.startActivity(new Intent(context, NotificationActivity.class));
                }
            });


        }


        // initializing AppointmentAdapter class and also pass the values to constructor to load data in _preNotifyView(Recyclerview)...
        appointmentAdapter = new AppointmentAdapter(context, appointmentList, myId);

        //set up layout manager for _preNotifyView(Recycler View)...
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        binding.preAppointView.setLayoutManager(linearLayoutManager);
        binding.preAppointView.setAdapter(appointmentAdapter);

        // show the animation while notification data retrieving from realtime database...
        binding.preAppointView.showShimmerAdapter();

        //this function retrieve the data of Appointments from realtime database
        readAppointmentList();

    }

    private void readAppointmentList() {
        FirebaseDatabase.getInstance().getReference().child("AppointList").child(myId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        appointmentList.clear();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                            AppointmentListData appointmentListData = dataSnapshot.getValue(AppointmentListData.class);
                            if (!appointmentListData.getAppointStatus().equals("Completed")) {
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