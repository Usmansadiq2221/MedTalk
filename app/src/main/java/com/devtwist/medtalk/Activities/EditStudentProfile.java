package com.devtwist.medtalk.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.devtwist.medtalk.Models.DoctorData;
import com.devtwist.medtalk.Models.NotificationData;
import com.devtwist.medtalk.Models.StudentData;
import com.devtwist.medtalk.R;
import com.devtwist.medtalk.databinding.ActivityCreateStudentProfileBinding;
import com.devtwist.medtalk.databinding.ActivityEditProfileBinding;
import com.devtwist.medtalk.databinding.ActivityEditStudentProfileBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.InputStream;
import java.util.Date;

public class EditStudentProfile extends AppCompatActivity {

    private ActivityEditStudentProfileBinding binding;
    private FirebaseUser mAuth;
    private String uId;

    // used to put data for show in in spinner...
    private ArrayAdapter<CharSequence> adapter, genderAdapter, categoryAdapter;


    //used to store reference for uploading data in realtime database...
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;

    //used to get the current user...

    private Uri proPicFilePath = null;
    private Bitmap bitmap;

    //used to store data temporarily when user create his profile...
    private SharedPreferences preferences;

    private FirebaseStorage storage;

    private String token, username, gender, hospital, education, extraQualification, certifications, category, bio,
            profileUrl, phoneNo, email, province, city, address;

    private boolean isProfileUpdated = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditStudentProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        storage = FirebaseStorage.getInstance();
        mAuth = FirebaseAuth.getInstance().getCurrentUser();
        uId = mAuth.getUid();


        FirebaseDatabase.getInstance().getReference().child("Users").child("Student")
                .child(uId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        StudentData studentData = snapshot.getValue(StudentData.class);
                        Picasso.get().load(studentData.getProfileUrl()).placeholder(R.drawable.enter_profile_icon).into(binding.profileimage);
                        binding.username.setText(studentData.getUsername());
                        binding.hospital.setText(studentData.getHospital());
                        binding.phoneNoText.setText(studentData.getPhone());
                        binding.bio.setText(studentData.getBiography());
                        binding.emailText.setText(studentData.getEmail());
                        binding.address.setText(studentData.getAddress());
                        binding.education.setText(studentData.getEducation());
                        binding.certification.setText(studentData.getCertifications());
                        binding.extraQualification.setText(studentData.getExtraQualification());
                        binding.loadingBar.setVisibility(View.GONE);
                        binding.detailsLayout.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }

                });

        username = gender = hospital = education = extraQualification = certifications = category = bio
                = profileUrl = phoneNo = email = province = city = address = "";


        binding.backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });


        genderAdapter = ArrayAdapter.createFromResource(this,
                R.array.gender_array, R.layout.spinner_style);
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.gender.setAdapter(genderAdapter);

        adapter = ArrayAdapter.createFromResource(this,
                R.array.province_array, R.layout.spinner_style);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.province.setAdapter(adapter);
        binding.province.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                //cities spinner array list values when values in province spinner changes...
                province = binding.province.getSelectedItem().toString();
                if (i==1){
                    adapter = ArrayAdapter.createFromResource(EditStudentProfile.this,
                            R.array.punjab_array, R.layout.spinner_style);
                }
                else if (i==2){
                    adapter = ArrayAdapter.createFromResource(EditStudentProfile.this,
                            R.array.balochistan_array, R.layout.spinner_style);
                }
                else if (i==3){
                    adapter = ArrayAdapter.createFromResource(EditStudentProfile.this,
                            R.array.kpk_array, R.layout.spinner_style);
                }
                else if (i==4){
                    adapter = ArrayAdapter.createFromResource(EditStudentProfile.this,
                            R.array.sindh_array, R.layout.spinner_style);
                }
                else{
                    adapter = ArrayAdapter.createFromResource(EditStudentProfile.this,
                            R.array.punjab_array, R.layout.spinner_style);
                }
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                binding.city.setAdapter(adapter);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        categoryAdapter = ArrayAdapter.createFromResource(this,
                R.array.category_array, R.layout.spinner_style);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.category.setAdapter(categoryAdapter);


        binding.profileimage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                captureImage();
            }
        });

        binding.submitProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAttributes();
            }
        });






    }

    private void captureImage() {
        //check if the user allow the permission of external storage or not, if no then ask for permission...
        Dexter.withActivity(EditStudentProfile.this).withPermission(Manifest.permission.READ_EXTERNAL_STORAGE).withListener(new PermissionListener() {

            @Override
            public void onPermissionGranted(PermissionGrantedResponse response) {

                try {
                    //select image from gallery for profile...
                    Intent select = new Intent(Intent.ACTION_PICK);
                    select.setType("image/*");
                    startActivityForResult(Intent.createChooser(select,"Select an Image"), 1);
                } catch (Exception e) {
                    Log.i("CropImage Error",e.getMessage().toString());
                }


            }

            @Override
            public void onPermissionDenied(PermissionDeniedResponse response) {
                Toast.makeText(EditStudentProfile.this, "Permission Denied", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                token.continuePermissionRequest();
            }
        }).check();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //for cropping image...
        if (requestCode==1 && resultCode == RESULT_OK ) {
            proPicFilePath = data.getData();
            try {
                CropImage.activity(proPicFilePath)
                        .setAspectRatio(1,1)
                        .start(EditStudentProfile.this);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                proPicFilePath = result.getUri();
                try {
                    //load image into _profileimage...
                    InputStream inputStream = getContentResolver().openInputStream(proPicFilePath);
                    bitmap = BitmapFactory.decodeStream(inputStream);
                    binding.profileimage.setImageBitmap(bitmap);
                    //_cpProfileError.setVisibility(View.GONE);
                    isProfileUpdated = true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }



    }

    private void checkAttributes() {
        phoneNo = binding.phoneNoText.getText().toString();
        email = binding.emailText.getText().toString();
        username = binding.username.getText().toString().trim();
        gender = binding.gender.getSelectedItem().toString();
        hospital = binding.hospital.getText().toString().trim();
        bio = binding.bio.getText().toString().trim();
        province = binding.province.getSelectedItem().toString();
        city = binding.city.getSelectedItem().toString();
        address = binding.address.getText().toString().trim();
        education = binding.education.getText().toString().trim();
        category = binding.category.getSelectedItem().toString();
        certifications = binding.certification.getText().toString().trim();
        extraQualification = binding.extraQualification.getText().toString().trim();



        if (username.length()>=3 && !gender.equals("Select Gender") && hospital.length()>0 && bio.length()>1 && !province.equals("Select Province") && !city.equals("Select City") &&
                address.length()>1 && education.length()>1 && !category.equals("Select Category") && certifications.length()>1 && extraQualification.length()>1){

            binding.submitProfile.setVisibility(View.GONE);
            binding.progressBar.setVisibility(View.VISIBLE);
            submitProfile();

        }
        else {
            if (username.length()<1){
                binding.usernameError.setVisibility(View.VISIBLE);
            }
            else if (username.length() < 3 && username.length()>0) {
                binding.username.setText("Invalid username!");
                binding.usernameError.setVisibility(View.VISIBLE);
            }
            else {
                binding.usernameError.setVisibility(View.GONE);
            }

            if (gender.equals("Select Gender")){
                binding.genderError.setVisibility(View.VISIBLE);
            }
            else {
                binding.genderError.setVisibility(View.GONE);
            }

            if (bio.length()<1){
                binding.bioError.setVisibility(View.VISIBLE);
            }
            else {
                binding.bioError.setVisibility(View.GONE);
            }

            if (province.equals("Select Province")){
                binding.provinceError.setVisibility(View.VISIBLE);
            }
            else {
                binding.provinceError.setVisibility(View.GONE);
            }

            if (city.equals("Select City")){
                binding.cityError.setVisibility(View.VISIBLE);
            }
            else {
                binding.cityError.setVisibility(View.GONE);
            }

            if (address.length()<1){
                binding.addressError.setVisibility(View.VISIBLE);
            }
            else {
                binding.addressError.setVisibility(View.GONE);
            }

            if (education.length()<1){
                binding.educationError.setVisibility(View.VISIBLE);
            }
            else {
                binding.educationError.setVisibility(View.GONE);
            }

            if (city.equals("Select Category")){
                binding.categoryError.setVisibility(View.VISIBLE);
            }
            else {
                binding.categoryError.setVisibility(View.GONE);
            }

            if (certifications.length()<1){
                binding.certificationError.setVisibility(View.VISIBLE);
            }
            else {
                binding.certificationError.setVisibility(View.GONE);
            }

            if (extraQualification.length()<1){
                binding.extraQualificationError.setVisibility(View.VISIBLE);
            }
            else {
                binding.extraQualificationError.setVisibility(View.GONE);
            }

        }


    }

    private void submitProfile() {

        Date date = new Date();
        double timeStamp = date.getTime();
        uId = mAuth.getUid();

        if (isProfileUpdated) {
            StorageReference uploadfile = storage.getReference().child("Profile Pictures").child(mAuth.getUid());

            //upload file in cloud storage...
            uploadfile.putFile(proPicFilePath).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    uploadfile.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            FirebaseDatabase.getInstance().getReference().child("Users").child("Student").child(uId)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            //initialize object of model class and pass values to its constructor...
                                            StudentData getData = snapshot.getValue(StudentData.class);
                                            StudentData studentData = new StudentData(uId, getData.getToken(), username, gender, hospital, bio, education, extraQualification, certifications,
                                                    uri.toString(), province, city, address, phoneNo, email, 0.0, 0.0, getData.getBadReviews());

                                            //upload data to firebase...
                                            FirebaseDatabase.getInstance().getReference().child("Users").child("Student").child(uId).setValue(studentData).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void unused) {

                                                    binding.progressBar.setVisibility(View.GONE);
                                                    binding.submitProfile.setVisibility(View.VISIBLE);

                                                    Toast.makeText(EditStudentProfile.this, "Changes Saved Successfully!", Toast.LENGTH_SHORT).show();
                                                    finish();
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    binding.progressBar.setVisibility(View.GONE);
                                                    binding.submitProfile.setVisibility(View.VISIBLE);
                                                    Toast.makeText(EditStudentProfile.this, "Check your internet\nconnection", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                            binding.progressBar.setVisibility(View.GONE);
                                            binding.submitProfile.setVisibility(View.VISIBLE);
                                            Toast.makeText(EditStudentProfile.this, "Check your internet\nconnection", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    });

                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {

                }
            });
        }
        else {
            FirebaseDatabase.getInstance().getReference().child("Users").child("Student").child(uId)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            //initialize object of model class and pass values to its constructor...
                            StudentData getData = snapshot.getValue(StudentData.class);
                            StudentData studentData = new StudentData(uId, getData.getToken(), username, gender, hospital, bio, education, extraQualification, certifications,
                                    getData.getProfileUrl(), province, city, address, phoneNo, email, 0.0, 0.0, getData.getBadReviews());

                            //upload data to firebase...
                            FirebaseDatabase.getInstance().getReference().child("Users").child("Student").child(uId).setValue(studentData).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {

                                    binding.progressBar.setVisibility(View.GONE);
                                    binding.submitProfile.setVisibility(View.VISIBLE);

                                    Toast.makeText(EditStudentProfile.this, "Changes Saved Successfully!", Toast.LENGTH_SHORT).show();
                                    finish();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    binding.progressBar.setVisibility(View.GONE);
                                    binding.submitProfile.setVisibility(View.VISIBLE);
                                    Toast.makeText(EditStudentProfile.this, "Check your internet\nconnection", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            binding.progressBar.setVisibility(View.GONE);
                            binding.submitProfile.setVisibility(View.VISIBLE);
                            Toast.makeText(EditStudentProfile.this, "Check your internet\nconnection", Toast.LENGTH_SHORT).show();
                        }
                    });
        }

    }


}