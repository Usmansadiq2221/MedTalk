package com.devtwist.medtalk.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;

import com.devtwist.medtalk.Adapters.AllCategoryAdapter;
import com.devtwist.medtalk.Adapters.CategoryAdapter;
import com.devtwist.medtalk.R;
import com.devtwist.medtalk.databinding.ActivityAllCategoriesBinding;

public class AllCategoriesActivity extends AppCompatActivity {

    private ActivityAllCategoriesBinding binding;
    private AllCategoryAdapter categoryAdapter;

    private int[] categoryImaegList = {R.drawable.anesthesiologist, R.drawable.cardiologist, R.drawable.dermatologist,R.drawable.dentist, R.drawable.endocrinologist, R.drawable.gynaecologist,
            R.drawable.gastroenterologist, R.drawable.neurologist, R.drawable.orthopedist, R.drawable.otolaryngologist, R.drawable.oncologist, R.drawable.ophthalmologist, R.drawable.paediatrician,
            R.drawable.psychologist, R.drawable.pulmonologist, R.drawable.radiologist, R.drawable.surgeon, R.drawable.urologist, R.drawable.veterinarian};

    // adding strings in array list for showing list of categories in _categoryView(RecyclerView)...
    private String[] categoryTextList = {"Anesthesiologist", "Cardiologist", "Dermatologist", "Dentist", "Endocrinologist", "Gynaecologist",
            "Gastroenterologist", "Neurologist", "Orthopedist", "Otolaryngologist", "Oncologist", "Ophthalmologist", "Paediatrician",
            "Psychologist", "Pulmonologist", "Radiologist", "Surgeon", "Urologist", "Veterinarian"};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAllCategoriesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        //set up layout manager for _categoryView(RecyclerView to load category data...
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        binding.categoryView.setLayoutManager(linearLayoutManager);

        //initializing Category Adapter class and also pass parameters to its constructor to load lists in _categoryView...
        categoryAdapter = new AllCategoryAdapter(this, categoryImaegList,categoryTextList);
        binding.categoryView.setAdapter(categoryAdapter);



    }
}