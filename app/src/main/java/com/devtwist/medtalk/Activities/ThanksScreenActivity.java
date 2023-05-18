package com.devtwist.medtalk.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

import com.devtwist.medtalk.databinding.ActivityThanksScreenBinding;

public class ThanksScreenActivity extends AppCompatActivity {


    private ActivityThanksScreenBinding binding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityThanksScreenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        binding.thanksImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        binding.thanksText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        binding.thanksLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }
}