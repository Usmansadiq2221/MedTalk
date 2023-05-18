package com.devtwist.medtalk.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.devtwist.medtalk.Models.StudentData;
import com.devtwist.medtalk.databinding.ActivityViewUserDetailsBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class ViewUserDetails extends AppCompatActivity {

    private ActivityViewUserDetailsBinding binding;
    private String accType = "", myId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityViewUserDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        SharedPreferences preferences = getSharedPreferences("MedTalkData", MODE_PRIVATE);
        accType = preferences.getString("accType", "");

        myId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseDatabase.getInstance().getReference().child("Users").child(accType)
                .child(myId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //to avoid code length i used StudentData class for retrieving data of all type of users because this class contains the attributes of all of the users...
                        StudentData userdata = snapshot.getValue(StudentData.class);
                        if (userdata.getProfileUrl().length()>1){
                            Picasso.get().load(userdata.getProfileUrl()).into(binding.profileimage);
                        }
                        binding.username.setText(userdata.getUsername());
                        binding.email.setText(userdata.getEmail());
                        binding.phone.setText(userdata.getPhone());
                        binding.city.setText(userdata.getCity());
                        binding.address.setText(userdata.getAddress());
                        if (!accType.equals("Patient")){
                            binding.education.setText(userdata.getEducation());
                            binding.docCategory.setText(userdata.getCategory());
                            binding.workPlace.setText(userdata.getHospital());
                            binding.bio.setText(userdata.getBiography());
                            binding.extradetails.setVisibility(View.VISIBLE);
                        }
                        binding.progressBar.setVisibility(View.GONE);
                        binding.userDetails.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(ViewUserDetails.this, "Network Error\nCheck your internet connection!", Toast.LENGTH_SHORT).show();
                    }
                });

        binding.backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });


        binding.editProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (accType.equals("Patient")){
                    startActivity(new Intent(ViewUserDetails.this, EditPatientProfile.class));
                }
                else if (accType.equals("Student")) {
                    startActivity(new Intent(ViewUserDetails.this, EditStudentProfile.class));
                } else if (accType.equals("Doctor")) {
                    startActivity(new Intent(ViewUserDetails.this, EditProfileActivity.class));
                }
            }
        });


    }
}