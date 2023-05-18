package com.devtwist.medtalk.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.devtwist.medtalk.databinding.ActivityLoginOptionBinding;

public class LoginOptionActivity extends AppCompatActivity {

    private ActivityLoginOptionBinding binding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginOptionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        binding.studentImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nextActivity("Student");
            }
        });

        binding.doctorImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nextActivity("Doctor");
            }
        });

        binding.patientImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nextActivity("Patient");
            }
        });



    }

    private void nextActivity(String loginType) {

        Intent intent = new Intent(LoginOptionActivity.this, LoginActivity.class);
        intent.putExtra("type", loginType);
        startActivity(intent);

    }





    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}