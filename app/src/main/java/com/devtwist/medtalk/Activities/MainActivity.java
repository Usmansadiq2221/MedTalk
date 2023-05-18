package com.devtwist.medtalk.Activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.devtwist.medtalk.R;
import com.devtwist.medtalk.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private boolean isProfileCreated = false, isLogedIn = false;

    private Intent intent;

    private String accType = "", loginType = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        binding.innerLogo.animate().translationYBy(-1500).setDuration(100);
        //binding.outerLogo.animate().scaleX(1.5f).scaleY(1.5f).translationYBy(1500).setDuration(100);
        //binding.outerLogo.animate().translationYBy(1500).setDuration(100);
        binding.appName.animate().scaleY(0f).setDuration(10);
        isProfileCreated = isLogedIn = false;


        startAnimation();

    }



    private void startAnimation() {
        new CountDownTimer(500, 500) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                binding.innerLogo.animate().alpha(1f).setDuration(100);
                binding.innerLogo.animate().translationYBy(1600).setDuration(500);

            }
        }.start();
        new CountDownTimer(1200, 1200) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                binding.innerLogo.animate().translationYBy(-100).setDuration(500);
            }
        }.start();
        new CountDownTimer(2000, 2000) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                binding.outerLogo.setAlpha(1f);
                binding.outerLogo.animate().rotationYBy(180).translationYBy(-50f).setDuration(700);
            }
        }.start();
        new CountDownTimer(2500, 2500) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                new CountDownTimer(500, 500) {
                    @Override
                    public void onTick(long millisUntilFinished) {

                    }

                    @Override
                    public void onFinish() {
                        binding.appName.animate().scaleY(1f).setDuration(500);

                    }
                }.start();
                new CountDownTimer(3000, 1000) {
                    @Override
                    public void onTick(long millisUntilFinished) {

                    }

                    @Override
                    public void onFinish() {
                        nextActivity();
                    }
                }.start();
            }
        }.start();
    }


    private void nextActivity() {
        SharedPreferences preferences = getSharedPreferences("MedTalkData", MODE_PRIVATE);
        isLogedIn = preferences.getBoolean("isLogedIn", false);
        isProfileCreated = preferences.getBoolean("isProfileCreated", false);
        accType = preferences.getString("accType", "");
        loginType = preferences.getString("loginType", "");

        if (isLogedIn && isProfileCreated ) {
            if (accType.equals("Doctor")) {
                intent = new Intent(MainActivity.this, DoctorHomeActivity.class);
            } else if (accType.equals("Student")) {
                intent = new Intent(MainActivity.this, StudentHomeActivity.class);
            } else if (accType.equals("Patient")) {
                intent = new Intent(MainActivity.this, PatientHomeActivity.class);
            }
//            intent = new Intent(MainActivity.this, LoginOptionActivity.class);
            intent.putExtra("loginType", loginType);
            intent.putExtra("accType", accType);
            startActivity(intent);
            finish();
        } else {
            if (!isLogedIn) {
                intent = new Intent(MainActivity.this, LoginOptionActivity.class);
                startActivity(intent);
                finish();
            }
            else if (isLogedIn && !isProfileCreated){
                if (accType.equals("Doctor")) {
                    intent = new Intent(MainActivity.this, CreateProfileActivity.class);
                } else if (accType.equals("Student")) {
                    intent = new Intent(MainActivity.this, CreateStudentProfileActivity.class);
                }else if (accType.equals("Patient")){
                    intent = new Intent(MainActivity.this, CreatePatientProfileActivity.class);
                }
                intent.putExtra("accType", accType);
                intent.putExtra("loginType", loginType);
                startActivity(intent);
                finish();

            }
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}