package com.devtwist.medtalk.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.devtwist.medtalk.Adapters.CountryAdapter;
import com.devtwist.medtalk.Models.CountryCodeData;
import com.devtwist.medtalk.R;
import com.devtwist.medtalk.databinding.ActivityLoginBinding;
import com.google.android.gms.auth.api.identity.BeginSignInRequest;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.internal.InternalTokenProvider;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;


    //used to load the list of country data in _countryList (RecyclerView)...
    private ArrayList<String> _countryNameList, _countryCodeList, _countryAbbrList;
    private Resources res;
    private ArrayList<CountryCodeData> cCodeList;

    //(Model Class) used to get and set data...
    private CountryCodeData countryCodeData;

    private Intent intent;


    private CountryAdapter countryAdapter;
    private boolean isFound, isPhone = false;

    private GoogleSignInClient googleSignInClient;
    private FirebaseAuth firebaseAuth;
    private GoogleSignInOptions googleSignInOptions;
    private String accType = "", loginType = "", firebaseOtp, phoneNo;

    private SharedPreferences preferences;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        accType = getIntent().getStringExtra("type");
        firebaseAuth = FirebaseAuth.getInstance();

        binding.textView.setText("Login as " + accType);


// Initialize sign in options the client-id is copied form google-services.json file
        googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();


        binding.phoneLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginType = "Phone";
                isPhone = true;
                binding.loginVector.setImageResource(R.drawable.otp_vector);
                binding.loginTypeLayout.setVisibility(View.GONE);
                binding.otpLayout.setVisibility(View.VISIBLE);
            }
        });

        binding.googleLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Initialize sign in client
                loginType = "Google";
                googleSignInClient = GoogleSignIn.getClient(LoginActivity.this, googleSignInOptions);
                binding.googleLogin.setVisibility(View.GONE);
                binding.googleProgressBar.setVisibility(View.VISIBLE);

                // Initialize sign in intent
                Intent intent = googleSignInClient.getSignInIntent();

                // Start activity for result
                startActivityForResult(intent, 100);


            }
        });


        isFound = false;

        //initializing arraylists for storing data for adapter class...
        _countryNameList = new ArrayList<String>();
        _countryCodeList = new ArrayList<String>();
        _countryAbbrList = new ArrayList<String>();

        //retrieving data from string file from value resources...
        res = getResources();
        Collections.addAll(_countryNameList, res.getStringArray(R.array.country_name_array));
        Collections.addAll(_countryCodeList, res.getStringArray(R.array.country_code_array));
        Collections.addAll(_countryAbbrList, res.getStringArray(R.array.abbreviation_array));

        //set up layout manager for _counterList (RecyclerView)...
        LinearLayoutManager countryLayoutManager = new LinearLayoutManager(this);
        countryLayoutManager.setReverseLayout(true);
        countryLayoutManager.setStackFromEnd(true);
        binding.countryList.setLayoutManager(countryLayoutManager);

        cCodeList = new ArrayList<>();

        //initializing CountryAdapter class and pass values to its constructor to load data in _countryList(RecyclerVeiw)...
        countryAdapter = new CountryAdapter(this, cCodeList, binding.otpLayout, binding.cCodeLayout,
                binding.otpCountryCode, binding.otpCountryName);

        binding.countryList.setHasFixedSize(true);
        binding.countryList.setAdapter(countryAdapter);

        //store values in arrraylists ...
        readCountryList();

        binding.searchCountry.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                readCountryList();
            }

            @Override
            public void afterTextChanged(Editable s) {
                readCountryList();
            }
        });


        //showing _cCodeLayout to select the country code...
        binding.otpCountryName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.otpLayout.setVisibility(View.GONE);
                binding.cCodeLayout.setVisibility(View.VISIBLE);
            }
        });



        //showing _cCodeLayout to select the country code...
        binding.otpCountryName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.otpLayout.setVisibility(View.GONE);
                binding.cCodeLayout.setVisibility(View.VISIBLE);
            }
        });

        //hiding the _cCodeLayout...
        binding.cCodeCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.cCodeLayout.setVisibility(View.GONE);
                binding.otpLayout.setVisibility(View.VISIBLE);
            }
        });

        //getting otp code digits one by one in each box from the user...
        binding.otpCodeInput1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                binding.otpCodeInput2.requestFocus();
            }
        });

        binding.otpCodeInput2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                binding.otpCodeInput3.requestFocus();
            }
        });

        binding.otpCodeInput3.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                binding.otpCodeInput4.requestFocus();
            }
        });

        binding.otpCodeInput4.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                binding.otpCodeInput5.requestFocus();
            }
        });

        binding.otpCodeInput5.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                binding.otpCodeInput6.requestFocus();
            }
        });

        //submiting the users enter phone number to generate and send the otp code...
        binding.otpPhoneSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (binding.otpPhoneInput.getText().length()>0) {
                    //hiding the keybord...
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(),0);
                    //_otpPhoneSubmit.setVisibility(View.GONE);
                    binding.otpProgressBar.setVisibility(View.VISIBLE);
                    sendOtp();
                }
            }
        });

        //sending the request to the server to send otp again...
        binding.resendOtpText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendOtp();
            }
        });

        // submiting the otp code to verify the user...
        binding.otpCodeSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String code = binding.otpCodeInput1.getText().toString() +
                        binding.otpCodeInput2.getText().toString() +
                        binding.otpCodeInput3.getText().toString() +
                        binding.otpCodeInput4.getText().toString() +
                        binding.otpCodeInput5.getText().toString() +
                        binding.otpCodeInput6.getText().toString() ;

                if (code.length()==6){
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(),0);
                    verifyCode(code);
                    binding.otpCodeSubmit.setVisibility(View.GONE);
                    binding.otpProgressBar.setVisibility(View.VISIBLE);
                }else {
                    Toast.makeText(LoginActivity.this, "Invalid Otp!", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void readCountryList() {
        cCodeList.clear();
        if (binding.searchCountry.getText().length()<1) {
            for (int i = 0; i < _countryCodeList.size(); i++) {
                countryCodeData = new CountryCodeData(_countryNameList.get(i), _countryCodeList.get(i), _countryAbbrList.get(i));
                cCodeList.add(countryCodeData);
            }
            countryAdapter.notifyDataSetChanged();
        }
        else{
            for (int i = 0; i < _countryCodeList.size(); i++) {
                if (_countryCodeList.get(i).toLowerCase().contains(binding.searchCountry.getText().toString().toLowerCase())
                        || _countryNameList.get(i).toLowerCase().contains(binding.searchCountry.getText().toString().toLowerCase()))
                {
                    countryCodeData = new CountryCodeData(_countryNameList.get(i), _countryCodeList.get(i), _countryAbbrList.get(i));
                    cCodeList.add(countryCodeData);
                }
            }
            countryAdapter.notifyDataSetChanged();
        }

    }


    private void signInWithCredential(PhoneAuthCredential credential) {
        // inside this method we are checking if
        // the code entered is correct or not.
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // if the code is correct and the task is successful
                            // we are sending our user to new activity.
                            nextActivity();
                        } else {
                            // if the code is not correct then we are
                            // displaying an error message to the user.
                            Toast.makeText(LoginActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });


    }




    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Check condition
        if (requestCode == 100) {
            // When request code is equal to 100 initialize task
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());


            } catch (ApiException e) {



            }
            // check condition

        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken,null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            FirebaseUser user = firebaseAuth.getCurrentUser();
                            nextActivity();
                        }
                        else {
                            binding.googleProgressBar.setVisibility(View.GONE);
                            binding.googleLogin.setVisibility(View.VISIBLE);
                            Toast.makeText(LoginActivity.this, "Network Problem\nCheck your internet connection!", Toast.LENGTH_SHORT).show();

                        }
                    }
                });
    }

    private void nextActivity() {
        try {

            String uID = FirebaseAuth.getInstance().getCurrentUser().getUid();
            FirebaseDatabase.getInstance().getReference().child("Users")
                    .child(accType).child(uID).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DataSnapshot> task) {
                            isFound = false;
                            if (task.isSuccessful()) {
                                DataSnapshot snapshot = task.getResult();
                                if (snapshot.exists()) {
                                    isFound = true;
                                }
                            }
                            if (isFound) {
                                preferences = getSharedPreferences("MedTalkData", MODE_PRIVATE);
                                SharedPreferences.Editor editor = preferences.edit();
                                editor.putBoolean("isLogedIn", true);
                                editor.putBoolean("isProfileCreated", true);
                                editor.putString("loginType", loginType);
                                editor.putString("accType", accType);
                                editor.commit();
                                //Toast.makeText(MainActivity.this, "found", Toast.LENGTH_SHORT).show();
                                FirebaseMessaging.getInstance().getToken().addOnSuccessListener(new OnSuccessListener<String>() {
                                    @Override
                                    public void onSuccess(String s) {
                                        String token = s;
                                        String uId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                        HashMap<String, Object> hashMap = new HashMap<>();
                                        hashMap.put("token", token);
                                        FirebaseDatabase.getInstance().getReference().child("Users")
                                                .child(accType).child(uId).updateChildren(hashMap);

                                    }
                                });
                                if (accType.equals("Doctor")) {
                                    intent = new Intent(LoginActivity.this, DoctorHomeActivity.class);
                                } else if (accType.equals("Student")) {
                                    intent = new Intent(LoginActivity.this, StudentHomeActivity.class);
                                } else if (accType.equals("Patient")) {
                                    intent = new Intent(LoginActivity.this, PatientHomeActivity.class);
                                }
                                intent.putExtra("accType", accType);
                                intent.putExtra("loginType", loginType);
                                startActivity(intent);
                                finish();
                            } else {
                                preferences = getSharedPreferences("MedTalkData", MODE_PRIVATE);
                                SharedPreferences.Editor editor = preferences.edit();
                                editor.putBoolean("isLogedIn", true);
                                editor.putString("loginType", loginType);
                                editor.putString("accType", accType);
                                editor.commit();
                                //Toast.makeText(MainActivity.this, "Not Found", Toast.LENGTH_SHORT).show();
                                if (accType.equals("Doctor")) {
                                    intent = new Intent(LoginActivity.this, CreateProfileActivity.class);
                                } else if (accType.equals("Student")) {
                                    intent = new Intent(LoginActivity.this, CreateStudentProfileActivity.class);
                                }else if (accType.equals("Patient")){
                                    intent = new Intent(LoginActivity.this, CreatePatientProfileActivity.class);
                                }
                                intent.putExtra("accType", accType);
                                intent.putExtra("loginType", loginType);
                                startActivity(intent);
                                finish();
                            }

                        }
                    });


        } catch (Exception e) {
            Log.i("Open Activity Error",e.getMessage().toString());
        }
    }


    private void sendOtp() {
        try {
            phoneNo = binding.otpCountryCode.getText().toString().trim() + binding.otpPhoneInput.getText().toString().trim();
            PhoneAuthOptions options =
                    PhoneAuthOptions.newBuilder(firebaseAuth)
                            .setPhoneNumber(phoneNo)		 // Phone number to verify
                            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                            .setActivity(this)				 // Activity (for callback binding)
                            .setCallbacks(mCallBack)// OnVerificationStateChangedCallbacks
                            .build();
            PhoneAuthProvider.verifyPhoneNumber(options);
        } catch (Exception e) {
            e.printStackTrace();
            Log.i("Send Otp Error", e.getMessage().toString());
        }
    }

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks

            // initializing our callbacks for on
            // verification callback method.
            mCallBack = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        // below method is used when
        // OTP is sent from Firebase
        @Override
        public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);

            binding.otpPhoneInputLayout.setVisibility(LinearLayout.GONE);
            binding.otpProgressBar.setVisibility(View.GONE);
            binding.otpCodeInputLayout.setVisibility(LinearLayout.VISIBLE);
            firebaseOtp = s;

        }

        @Override
        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
            // below line is used for getting OTP code
            // which is sent in phone auth credentials.
            final String code = phoneAuthCredential.getSmsCode();

            // checking if the code
            // is null or not.
            if (code.length()==6) {
                binding.otpCodeInput1.setText(code.substring(0,1));
                binding.otpCodeInput2.setText(code.substring(1,2));
                binding.otpCodeInput3.setText(code.substring(2,3));
                binding.otpCodeInput4.setText(code.substring(3,4));
                binding.otpCodeInput5.setText(code.substring(4,5));
                binding.otpCodeInput6.setText(code.substring(5,6));
                verifyCode(code);
            }else{
                Toast.makeText(LoginActivity.this, "Invalid Otp!", Toast.LENGTH_SHORT).show();
            }
        }

        // this method is called when firebase doesn't
        // sends our OTP code due to any error or issue.
        @Override
        public void onVerificationFailed(FirebaseException e) {
            // displaying error message with firebase exception.
            binding.otpProgressBar.setVisibility(View.GONE);
            binding.otpCodeSubmit.setVisibility(View.VISIBLE);
            Log.i("Send Otp Error", e.getMessage().toString());
        }
    };

    private void verifyCode(String code) {
        // below line is used for getting getting
        // credentials from our verification id and code.
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(firebaseOtp, code);

        // after getting credential we are
        // calling sign in method.
        signInWithCredential(credential);
    }

    @Override
    public void onBackPressed() {
        if (isPhone){
            isPhone = false;
            binding.loginVector.setImageResource(R.drawable.login_vector);
            binding.otpLayout.setVisibility(View.GONE);
            binding.loginTypeLayout.setVisibility(View.VISIBLE);
        }
        else {
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}