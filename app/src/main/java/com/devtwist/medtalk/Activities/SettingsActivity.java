package com.devtwist.medtalk.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.devtwist.medtalk.R;
import com.devtwist.medtalk.databinding.ActivitySettingsBinding;

public class SettingsActivity extends AppCompatActivity {

    private ActivitySettingsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        binding.backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        binding.privacy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SettingsActivity.this, ViewDocumentsActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("tag","p");
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });

        binding.terms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SettingsActivity.this, ViewDocumentsActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("tag","c");
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });

    }
}