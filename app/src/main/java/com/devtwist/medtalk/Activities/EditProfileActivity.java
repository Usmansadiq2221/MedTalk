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
import com.devtwist.medtalk.Models.StudentData;
import com.devtwist.medtalk.R;
import com.devtwist.medtalk.databinding.ActivityCreateProfileBinding;
import com.devtwist.medtalk.databinding.ActivityEditProfileBinding;
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


//For Edit Doctor Profile...
public class EditProfileActivity extends AppCompatActivity {

    private ActivityEditProfileBinding binding;

    // used to put data for show in in spinner...
    private ArrayAdapter<CharSequence> adapter, genderAdapter, categoryAdapter;

    //used to set values in realtime database...
    private DoctorData doctorData;

    //used to store reference for uploading data in realtime database...
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;

    //used to get the current user...
    private FirebaseUser mAuth;

    private Uri proPicFilePath = null;
    private Bitmap bitmap;

    //used to store data temporarily when user create his profile...
    private SharedPreferences preferences;

    private FirebaseStorage storage;

    private String token, uId, username, gender, hospital, education, extraQualification, certifications, category, biography,
            experience, sessionPrice, sessionTime,  profileUrl, phoneNo, email, province, city, address;

    private boolean isProfileUpdated = false;



    //For Edit Doctor Profile...

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        storage = FirebaseStorage.getInstance();
        mAuth = FirebaseAuth.getInstance().getCurrentUser();
        uId = mAuth.getUid();

        FirebaseDatabase.getInstance().getReference().child("Users").child("Doctor")
                .child(uId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        DoctorData data = snapshot.getValue(DoctorData.class);
                        Picasso.get().load(data.getProfileUrl()).placeholder(R.drawable.enter_profile_icon).into(binding.profileimage);
                        binding.username.setText(data.getUsername());
                        binding.hospital.setText(data.getHospital());
                        binding.phoneNoText.setText(data.getPhone());
                        binding.biography.setText(data.getBiography());
                        binding.emailText.setText(data.getEmail());
                        binding.address.setText(data.getAddress());
                        binding.education.setText(data.getEducation());
                        binding.certification.setText(data.getCertifications());
                        binding.extraQualification.setText(data.getExtraQualification());
                        binding.sessionPrice.setText(data.getSessionPrice());
                        binding.sessionTime.setText(data.getSessionTime());
                        binding.loadingBar.setVisibility(View.GONE);
                        binding.detailsLayout.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        binding.backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        token = uId = username = gender = hospital = education = extraQualification = certifications = category = biography =
                experience = sessionPrice = sessionTime =  profileUrl = phoneNo = email = province = city = address = "";

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
                    adapter = ArrayAdapter.createFromResource(EditProfileActivity.this,
                            R.array.punjab_array, R.layout.spinner_style);
                }
                else if (i==2){
                    adapter = ArrayAdapter.createFromResource(EditProfileActivity.this,
                            R.array.balochistan_array, R.layout.spinner_style);
                }
                else if (i==3){
                    adapter = ArrayAdapter.createFromResource(EditProfileActivity.this,
                            R.array.kpk_array, R.layout.spinner_style);
                }
                else if (i==4){
                    adapter = ArrayAdapter.createFromResource(EditProfileActivity.this,
                            R.array.sindh_array, R.layout.spinner_style);
                }
                else{
                    adapter = ArrayAdapter.createFromResource(EditProfileActivity.this,
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
        Dexter.withActivity(EditProfileActivity.this).withPermission(Manifest.permission.READ_EXTERNAL_STORAGE).withListener(new PermissionListener() {

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
                Toast.makeText(EditProfileActivity.this, "Permission Denied", Toast.LENGTH_SHORT).show();
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
                        .start(EditProfileActivity.this);
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
        phoneNo = binding.phoneNoText.getText().toString().trim();
        email = binding.emailText.toString().trim();
        username = binding.username.getText().toString().trim();
        gender = binding.gender.getSelectedItem().toString();
        hospital = binding.hospital.getText().toString().trim();
        biography = binding.biography.getText().toString().trim();
        province = binding.province.getSelectedItem().toString();
        city = binding.city.getSelectedItem().toString();
        address = binding.address.getText().toString().trim();
        education = binding.education.getText().toString().trim();
        category = binding.category.getSelectedItem().toString();
        certifications = binding.certification.getText().toString().trim();
        extraQualification = binding.extraQualification.getText().toString().trim();
        experience = binding.experience.getText().toString().trim();
        sessionTime = binding.sessionTime.getText().toString().trim();
        sessionPrice = binding.sessionPrice.getText().toString().trim();


        if (username.length()>=3 && !gender.equals("Select Gender") && hospital.length()>0 && biography.length()>1 && !province.equals("Select Province") && !city.equals("Select City") &&
                address.length()>1 && education.length()>1 && !category.equals("Select Category") && certifications.length()>1 && extraQualification.length()>1 && experience.length()>0 &&
                sessionTime.length()>0 && sessionPrice.length()>0){

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

            if (hospital.length()<1){
                binding.hospitalError.setVisibility(View.VISIBLE);
            }
            else {
                binding.hospitalError.setVisibility(View.GONE);
            }

            if (gender.equals("Select Gender")){
                binding.genderError.setVisibility(View.VISIBLE);
            }
            else {
                binding.genderError.setVisibility(View.GONE);
            }

            if (biography.length()<1){
                binding.biographyError.setVisibility(View.VISIBLE);
            }
            else {
                binding.biographyError.setVisibility(View.GONE);
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

            if (experience.length()<1){
                binding.experienceError.setVisibility(View.VISIBLE);
            }
            else {
                binding.experienceError.setVisibility(View.GONE);
            }

            if (sessionTime.length()<1){
                binding.sessionTimeError.setVisibility(View.VISIBLE);
            }
            else {
                binding.sessionTimeError.setVisibility(View.GONE);
            }

            if (sessionPrice.length()<1){
                binding.sessionPriceError.setVisibility(View.VISIBLE);
            }
            else {
                binding.sessionPriceError.setVisibility(View.GONE);
            }

        }


    }

    private void submitProfile() {

        Date date = new Date();
        double timeStamp = date.getTime();
        uId = mAuth.getUid();
        StorageReference uploadfile = storage.getReference().child("Profile Pictures").child(mAuth.getUid());
        if (isProfileUpdated) {
            //upload file in cloud storage...
            uploadfile.putFile(proPicFilePath).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    uploadfile.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            //initialize object of model class and pass values to its constructor...
                            FirebaseDatabase.getInstance().getReference().child("Users").child("Doctor").child(uId).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    DoctorData getData = snapshot.getValue(DoctorData.class);
                                    DoctorData doctorData = new DoctorData(uId, getData.getToken(), username, gender, hospital, education, extraQualification, certifications, category, biography,
                                            experience, sessionPrice, sessionTime, uri.toString(), phoneNo, email, province, city, address,
                                            getData.getAccBalance(), getData.getRating(), 0.0, 0.0, timeStamp, getData.getPatients(), getData.getBadReviews());

                                    //upload data to firebase...
                                    FirebaseDatabase.getInstance().getReference().child("Users").child("Doctor").child(uId).setValue(doctorData).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            Toast.makeText(EditProfileActivity.this, "Changes saved successfully!", Toast.LENGTH_SHORT).show();
                                            finish();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            binding.progressBar.setVisibility(View.GONE);
                                            binding.submitProfile.setVisibility(View.VISIBLE);
                                            Toast.makeText(EditProfileActivity.this, "Check your internet\nconnection", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

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
            //initialize object of model class and pass values to its constructor...
            FirebaseDatabase.getInstance().getReference().child("Users").child("Doctor").child(uId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    DoctorData getData = snapshot.getValue(DoctorData.class);
                    DoctorData doctorData = new DoctorData(uId, getData.getToken(), username, gender, hospital, education, extraQualification, certifications, category, biography,
                            experience, sessionPrice, sessionTime, getData.getProfileUrl(), phoneNo, email, province, city, address,
                            getData.getAccBalance(), getData.getRating(), 0.0, 0.0, timeStamp, getData.getPatients(), getData.getBadReviews());

                    //upload data to firebase...
                    FirebaseDatabase.getInstance().getReference().child("Users").child("Doctor").child(uId).setValue(doctorData).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {

                            binding.progressBar.setVisibility(View.GONE);
                            binding.submitProfile.setVisibility(View.VISIBLE);

                            //open Post Activity after completing uploading data...
                            Toast.makeText(EditProfileActivity.this, "Changes saved successfully!", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            binding.progressBar.setVisibility(View.GONE);
                            binding.submitProfile.setVisibility(View.VISIBLE);
                            Toast.makeText(EditProfileActivity.this, "Check your internet\nconnection", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.submitProfile.setVisibility(View.VISIBLE);
                    Toast.makeText(EditProfileActivity.this, "Check your internet\nconnection", Toast.LENGTH_SHORT).show();
                }
            });


        }
    }

}