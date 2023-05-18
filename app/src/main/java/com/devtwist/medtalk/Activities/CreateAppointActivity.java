package com.devtwist.medtalk.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;

import com.devtwist.medtalk.Adapters.AgeAdapter;
import com.devtwist.medtalk.Models.AppointmentData;
import com.devtwist.medtalk.Models.AppointmentListData;
import com.devtwist.medtalk.R;
import com.devtwist.medtalk.databinding.ActivityCreateAppointBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CreateAppointActivity extends AppCompatActivity {

    private ActivityCreateAppointBinding binding;
    private Bundle bundle;
    private String appointTime, appointDate, docId, myId, mDate, mTime;
    private AgeAdapter ageAdapter;

    private String[] ageTextList = {"1-9", "10+", "20+", "30+", "40+", "50+", "60+", "70+", "80+", "90+", "100+"};

    private ArrayAdapter<CharSequence> genderAdapter;
    private String patientName, patientAge, patientGender, patientProblem, appointId, accType;

    private FirebaseDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCreateAppointBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        appointDate = appointTime = docId = myId = "";
        bundle = getIntent().getExtras();


        SharedPreferences preferences = getSharedPreferences("MedTalkData", MODE_PRIVATE);
        accType = preferences.getString("accType", "");

        genderAdapter = ArrayAdapter.createFromResource(this,
                R.array.gender_array, R.layout.spinner_style_dark);
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.patientGender.setAdapter(genderAdapter);

        appointTime = bundle.getString("time");
        appointDate = bundle.getString("date");
        docId = bundle.getString("docId");
        myId = bundle.getString("useId");
        patientName = patientGender = patientProblem = patientAge = "";
        database = FirebaseDatabase.getInstance();

        //set up layout manager for _categoryView(RecyclerView to load category data...
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(RecyclerView.HORIZONTAL);
        binding.ageView.setLayoutManager(linearLayoutManager);

        //initializing Category Adapter class and also pass parameters to its constructor to load lists in _categoryView...
        ageAdapter = new AgeAdapter(this, ageTextList);
        binding.ageView.setAdapter(ageAdapter);

        binding.backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences preferences = getSharedPreferences("MedTalkData", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("age", "");
                editor.commit();
                finish();
            }
        });

        binding.submitDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences preferences = getSharedPreferences("MedTalkData", MODE_PRIVATE);
                patientAge = preferences.getString("age", "");
                patientName = binding.patientName.getText().toString().trim();
                patientProblem = binding.problemDetail.getText().toString().trim();
                patientGender = binding.patientGender.getSelectedItem().toString();
                checkRequirements();
            }
        });

    }

    private void checkRequirements() {
        if (patientAge.length()>0 && patientName.length()>0 && patientProblem.length()>0 && patientGender.length()>0){
            submitDetails();
        }
        else {
            if (patientName.length()<1){
                binding.nameError.setVisibility(View.VISIBLE);
            }
            else {
                binding.nameError.setVisibility(View.GONE);
            }

            if (patientAge.length()<1){
                binding.ageError.setVisibility(View.VISIBLE);
            }
            else {
                binding.ageError.setVisibility(View.GONE);
            }

            if (patientGender.length()<1){
                binding.genderError.setVisibility(View.VISIBLE);
            }
            else {
                binding.genderError.setVisibility(View.GONE);
            }

            if (patientProblem.length()<1){
                binding.problemError.setVisibility(View.VISIBLE);
            }
            else {
                binding.problemError.setVisibility(View.GONE);
            }
        }
    }

    private void submitDetails() {
        binding.submitDetails.setVisibility(View.GONE);
        binding.progressBar.setVisibility(View.VISIBLE);

        Date date = new Date();
        mDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
        mTime = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
        appointId = database.getReference().push().getKey();

        AppointmentData appointmentData = new AppointmentData(appointId, myId, docId, appointTime, appointDate,
                patientName, patientAge, patientGender, patientProblem, mDate, mTime, date.getTime());

        database.getReference().child("Appointments").child(appointId)
                .setValue(appointmentData).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        AppointmentListData docAppoint = new AppointmentListData(myId, appointId, accType, "Pending", date.getTime());
                        database.getReference().child("AppointList").child(docId)
                                .child(appointId).setValue(docAppoint).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        AppointmentListData clientAppoint = new AppointmentListData(docId, appointId, "Doctor", "Pending", date.getTime());
                                        database.getReference().child("AppointList").child(myId).child(appointId).setValue(clientAppoint)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void unused) {
                                                        SharedPreferences preferences = getSharedPreferences("MedTalkData", Context.MODE_PRIVATE);
                                                        SharedPreferences.Editor editor = preferences.edit();
                                                        editor.putString("age", "");
                                                        editor.commit();
                                                        binding.progressBar.setVisibility(View.GONE);
                                                        binding.networkError.setVisibility(View.GONE);
                                                        binding.submitDetails.setVisibility(View.VISIBLE);
                                                        startActivity(new Intent(CreateAppointActivity.this, ThanksScreenActivity.class));
                                                        finish();
                                                    }
                                                }).addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        networkError();
                                                    }
                                                });

                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        networkError();
                                    }
                                });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        networkError();
                    }
                });

    }

    private void networkError() {
        binding.progressBar.setVisibility(View.GONE);
        binding.networkError.setVisibility(View.VISIBLE);
        binding.submitDetails.setVisibility(View.VISIBLE);
    }


}