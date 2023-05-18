package com.devtwist.medtalk.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import com.devtwist.medtalk.Adapters.DoctorAdapter;
import com.devtwist.medtalk.Models.DoctorData;
import com.devtwist.medtalk.R;
import com.devtwist.medtalk.databinding.ActivitySearchDoctorBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Comparator;

public class SearchDoctorActivity extends AppCompatActivity {

    private ActivitySearchDoctorBinding binding;

    private ArrayList<DoctorData> docList;

    // used for showing data in _prePostView(recyclerview)
    private DoctorAdapter doctorAdapter;

    private String myId = "", searchText = "", searchType = "", category = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivitySearchDoctorBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        myId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        searchType = getIntent().getStringExtra("searchType");
        if (searchType.equals("category")){
            Bundle bundle = getIntent().getExtras();
            category = bundle.getString("text");
        }

        binding.backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        LinearLayoutManager docLayoutManager = new LinearLayoutManager(this);
        docLayoutManager.setReverseLayout(true);
        docLayoutManager.setStackFromEnd(true);

        binding.popularDocView.setLayoutManager(docLayoutManager);
        docList = new ArrayList<>();
        doctorAdapter = new DoctorAdapter(this, docList, myId);
        binding.popularDocView.setAdapter(doctorAdapter);
        binding.popularDocView.showShimmerAdapter();


        binding.searchDoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });


        readDoctorList();

        binding.searchDoc.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                readDoctorList();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


    }

    private void readDoctorList() {
        if (category.length()<1) {
            FirebaseDatabase.getInstance().getReference().child("Users")
                    .child("Doctor").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            docList.clear();
                            binding.noInternet.setVisibility(View.GONE);
                            binding.popularDocView.setVisibility(View.VISIBLE);
                            searchText = binding.searchDoc.getText().toString().toLowerCase().trim();
                            if (searchText.length() < 1) {
                                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                    DoctorData doctorData = dataSnapshot.getValue(DoctorData.class);
                                    docList.add(doctorData);
                                }
                            } else {
                                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                    DoctorData doctorData = dataSnapshot.getValue(DoctorData.class);
                                    if (doctorData.getHospital().toLowerCase().contains(searchText) ||
                                            doctorData.getUsername().toLowerCase().contains(searchText) ||
                                            doctorData.getCategory().toLowerCase().contains(searchText)) {

                                        docList.add(doctorData);

                                    }
                                }
                            }
                            docList.sort(Comparator.comparing(DoctorData::getRating));
                            doctorAdapter.notifyDataSetChanged();
                            binding.popularDocView.hideShimmerAdapter();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
//                        binding.popularDocView.setVisibility(View.GONE);
//                        binding.noInternet.setVisibility(View.VISIBLE);
                        }


                    });
        }
        else {
            FirebaseDatabase.getInstance().getReference().child("Users")
                    .child("Doctor").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            docList.clear();
                            binding.noInternet.setVisibility(View.GONE);
                            binding.popularDocView.setVisibility(View.VISIBLE);
                            searchText = binding.searchDoc.getText().toString().toLowerCase().trim();
                            if (searchText.length() < 1) {
                                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                    DoctorData doctorData = dataSnapshot.getValue(DoctorData.class);
                                    if (doctorData.getCategory().equals(category)) {
                                        docList.add(doctorData);
                                    }
                                }
                            } else {
                                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                    DoctorData doctorData = dataSnapshot.getValue(DoctorData.class);
                                    if ((doctorData.getHospital().toLowerCase().contains(searchText) ||
                                            doctorData.getUsername().toLowerCase().contains(searchText) ||
                                            doctorData.getCategory().toLowerCase().contains(searchText)) && doctorData.getCategory().equals(category)) {
                                        docList.add(doctorData);

                                    }
                                }
                            }
                            docList.sort(Comparator.comparing(DoctorData::getRating));
                            doctorAdapter.notifyDataSetChanged();
                            binding.popularDocView.hideShimmerAdapter();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
//                        binding.popularDocView.setVisibility(View.GONE);
//                        binding.noInternet.setVisibility(View.VISIBLE);
                        }


                    });
        }
    }


}