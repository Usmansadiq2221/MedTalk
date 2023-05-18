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

import com.devtwist.medtalk.Models.NotificationData;
import com.devtwist.medtalk.Models.PatientData;
import com.devtwist.medtalk.Models.StudentData;
import com.devtwist.medtalk.R;
import com.devtwist.medtalk.databinding.ActivityCreatePatientProfileBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
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
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.InputStream;
import java.util.Date;

public class CreatePatientProfileActivity extends AppCompatActivity {

    private ActivityCreatePatientProfileBinding binding;

    private String accType = "", loginType = "", emailPattern;

    // used to put data for show in in spinner...
    private ArrayAdapter<CharSequence> adapter, genderAdapter, categoryAdapter;

    //used to set values in realtime database...
    private StudentData studentData;

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

    private String token, uId, username, gender, profileUrl, phoneNo, email, province, city, address;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityCreatePatientProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        accType = getIntent().getStringExtra("accType");
        loginType = getIntent().getStringExtra("loginType");

        storage = FirebaseStorage.getInstance();
        mAuth = FirebaseAuth.getInstance().getCurrentUser();



        token = uId = username = gender = profileUrl = phoneNo = email = province = city = address = "";
        Toast.makeText(this, accType, Toast.LENGTH_SHORT).show();


        emailPattern = "[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,}$";

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
                    adapter = ArrayAdapter.createFromResource(CreatePatientProfileActivity.this,
                            R.array.punjab_array, R.layout.spinner_style);
                }
                else if (i==2){
                    adapter = ArrayAdapter.createFromResource(CreatePatientProfileActivity.this,
                            R.array.balochistan_array, R.layout.spinner_style);
                }
                else if (i==3){
                    adapter = ArrayAdapter.createFromResource(CreatePatientProfileActivity.this,
                            R.array.kpk_array, R.layout.spinner_style);
                }
                else if (i==4){
                    adapter = ArrayAdapter.createFromResource(CreatePatientProfileActivity.this,
                            R.array.sindh_array, R.layout.spinner_style);
                }
                else{
                    adapter = ArrayAdapter.createFromResource(CreatePatientProfileActivity.this,
                            R.array.punjab_array, R.layout.spinner_style);
                }
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                binding.city.setAdapter(adapter);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        if (loginType.equals("Google")){
            email = mAuth.getEmail();
            binding.email.setVisibility(View.GONE);
            binding.emailText.setText(email);
            binding.emailText.setVisibility(View.VISIBLE);
        }
        else if (loginType.equals("Phone")){
            phoneNo = mAuth.getPhoneNumber();
            binding.phoneNo.setVisibility(View.GONE);
            binding.phoneNoText.setText(phoneNo);
            binding.phoneNoText.setVisibility(View.VISIBLE);
        }

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
        Dexter.withActivity(CreatePatientProfileActivity.this).withPermission(Manifest.permission.READ_EXTERNAL_STORAGE).withListener(new PermissionListener() {

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
                Toast.makeText(CreatePatientProfileActivity.this, "Permission Denied", Toast.LENGTH_SHORT).show();
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
                        .start(CreatePatientProfileActivity.this);
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
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }



    }

    private void checkAttributes() {
        if (loginType.equals("Google")){
            phoneNo = binding.phoneNo.getText().toString().trim();
        }
        else if (loginType.equals("Phone")){
            email = binding.email.getText().toString().trim();
        }
        username = binding.username.getText().toString().trim();
        gender = binding.gender.getSelectedItem().toString();
        province = binding.province.getSelectedItem().toString();
        city = binding.city.getSelectedItem().toString();
        address = binding.address.getText().toString().trim();




        if (proPicFilePath!=null && username.length()>=3 && !gender.equals("Select Gender")
                && !province.equals("Select Province") && !city.equals("Select City") && address.length()>1 ){

            binding.submitProfile.setVisibility(View.GONE);
            binding.progressBar.setVisibility(View.VISIBLE);
            submitProfile();

        }
        else {
            if (proPicFilePath==null){
                binding.profilePicError.setVisibility(View.VISIBLE);
            }
            else {
                binding.profilePicError.setVisibility(View.GONE);
            }

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

        }


    }

    private void submitProfile() {

        Date date = new Date();
        double timeStamp = date.getTime();
        uId = mAuth.getUid();
        StorageReference uploadfile = storage.getReference().child("Profile Pictures").child(mAuth.getUid());

        //upload file in cloud storage...
        uploadfile.putFile(proPicFilePath).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                uploadfile.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        //get token for the user from firebase....
                        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(new OnSuccessListener<String>() {
                            @Override
                            public void onSuccess(String s) {
                                //initialize object of model class and pass values to its constructor...
                                PatientData patientData = new PatientData(uId, s, username, gender, uri.toString(), province, city, address, phoneNo, email, 0.0, 0.0, 0);

                                //upload data to firebase...
                                FirebaseDatabase.getInstance().getReference().child("Users").child(accType).child(uId).setValue(patientData).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        //get unique key to store the data of nitification...
                                        String notificationId = FirebaseDatabase.getInstance().getReference().push().getKey();
                                        String notificationMessage = "Your account has been successfully registered";


                                        //send notification to the user...
//                                        SendNotification sendNotification = new SendNotification("Registration Successfull", notificationMessage, token, uId, CreateProfileActivity.this);
//                                        sendNotification.sendNotification();

                                        //store value to the shared preferences to confirm the user regestration...
                                        preferences = getSharedPreferences("MedTalkData", MODE_PRIVATE);
                                        SharedPreferences.Editor editor = preferences.edit();
                                        editor.putBoolean("isProfileCreated", true);
                                        editor.commit();
                                        binding.progressBar.setVisibility(View.GONE);
                                        binding.submitProfile.setVisibility(View.VISIBLE);

                                        //open Post Activity after completing uploading data...
                                        Intent intent = new Intent(CreatePatientProfileActivity.this, PatientHomeActivity.class);
                                        startActivity(intent);
                                        finish();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        binding.progressBar.setVisibility(View.GONE);
                                        binding.submitProfile.setVisibility(View.VISIBLE);
                                        Toast.makeText(CreatePatientProfileActivity.this, "Check your internet\nconnection", Toast.LENGTH_SHORT).show();
                                    }
                                });
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

}