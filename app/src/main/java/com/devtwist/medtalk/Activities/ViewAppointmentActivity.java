package com.devtwist.medtalk.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;

import com.devtwist.medtalk.Models.AppointmentData;
import com.devtwist.medtalk.Models.AppointmentListData;
import com.devtwist.medtalk.Models.CallData;
import com.devtwist.medtalk.Models.DoctorData;
import com.devtwist.medtalk.Models.FeedbackData;
import com.devtwist.medtalk.Models.NotificationData;
import com.devtwist.medtalk.Models.SendNotification;
import com.devtwist.medtalk.Models.StudentData;
import com.devtwist.medtalk.R;
import com.devtwist.medtalk.databinding.ActivityViewAppointmentBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class ViewAppointmentActivity extends AppCompatActivity {

    private ActivityViewAppointmentBinding binding;
    private String appointId = "", myId = "", accType = "";
    private DatabaseReference reference;
    private AppointmentListData appointmentListData;

    private String receiverUId, receiverType, meetingRoom, mDate, mTime, userType;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityViewAppointmentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        SharedPreferences preferences = getSharedPreferences("MedTalkData", MODE_PRIVATE);
        accType = preferences.getString("accType", "");

        appointId = getIntent().getStringExtra("appointId");

        myId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        reference = FirebaseDatabase.getInstance().getReference();

        reference.child("AppointList").child(myId)
                .child(appointId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        appointmentListData = snapshot.getValue(AppointmentListData.class);


                        reference.child("Appointments").child(appointId)
                                .addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        AppointmentData appointmentData = snapshot.getValue(AppointmentData.class);
                                        reference.child("Users").child(appointmentListData.getClientType()).child(appointmentListData.getUserId())
                                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                                                        //i am using StudentData class for both doctor and student to get his/her profilePic, name and category.
                                                        StudentData userdata = snapshot.getValue(StudentData.class);
                                                        binding.username.setText(userdata.getUsername());
                                                        if (userdata.getProfileUrl().length() > 1) {
                                                            Picasso.get().load(userdata.getProfileUrl()).placeholder(R.drawable.enter_profile_icon).into(binding.clientProfile);
                                                        }

                                                        if (!appointmentListData.getClientType().equals("Patient")) {
                                                            binding.monitor2.setText(userdata.getCategory());
                                                        }
                                                        else {
                                                            binding.monitor2.setText(userdata.getCity());
                                                        }

                                                        //for start meeting...
                                                        if (accType.equals("Doctor")){
                                                            receiverType = appointmentListData.getClientType();
                                                            receiverUId = appointmentListData.getUserId();
                                                            meetingRoom = appointmentData.getDocId()+appointmentListData.getUserId();
                                                        }
                                                        else {
                                                            receiverType = "Doctor";
                                                            receiverUId = appointmentData.getDocId();
                                                            meetingRoom = appointmentListData.getUserId()+appointmentData.getDocId();
                                                        }


                                                        binding.voiceCallIcon.setOnClickListener(new View.OnClickListener() {
                                                            @Override
                                                            public void onClick(View v) {

                                                                //check the permission for microphone...
                                                                Dexter.withActivity(ViewAppointmentActivity.this).withPermission(Manifest.permission.RECORD_AUDIO).withListener(new com.karumi.dexter.listener.single.PermissionListener() {
                                                                    @Override
                                                                    public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                                                                        String messageBody = myId;


                                                                        //initializing the object of the model class and passing value to its constructor to start call...
                                                                        CallData callData = new CallData("calling", "AC", myId, accType, receiverUId, receiverType, "");

                                                                        // generating unique key for storing data in database...
                                                                        String randomKey = reference.push().getKey();

                                                                        //storing data for database...
                                                                        reference.child("Call")
                                                                                .child(meetingRoom)
                                                                                .setValue(callData).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                    @Override
                                                                                    public void onSuccess(Void unused) {
                                                                                        String callRandomkey = reference.push().getKey();
                                                                                        Date date = new Date();
                                                                                        mDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
                                                                                        mTime = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
                                                                                        double timeStamp = date.getTime();

                                                                                        reference.child("Users").child(accType).child(myId).addListenerForSingleValueEvent(new ValueEventListener() {
                                                                                            @Override
                                                                                            public void onDataChange(@NonNull DataSnapshot snapshot) {

                                                                                                //i am using StudentData class for both doctor and student to get his/her profilePic, name and category.
                                                                                                StudentData data = snapshot.getValue(StudentData.class);
                                                                                                //send notification to the receiver...

                                                                                                SendNotification sendNotification = new SendNotification("Incoming Call", data.getUsername(), data.getToken(), accType, myId, ViewAppointmentActivity.this, "Chat");
                                                                                                sendNotification.sendNotification();

                                                                                                //initializing NotificationData(Model Class) Object and passing values to its constructor...
                                                                                                NotificationData notificationData = new NotificationData(callRandomkey, myId, accType, "Audio Call", data.getUsername(), "Voice Call", false, date.getTime());

                                                                                                //storing data in database to show the call notification Notification Activity...
                                                                                                FirebaseDatabase.getInstance().getReference().child("Notifications").child(receiverUId).child(callRandomkey).setValue(notificationData);

                                                                                                //open the OutgoingCallACtivity to wiat for the receiver to atend the call...
                                                                                                nextActivity(receiverType, userdata.getUsername(), userdata.getProfileUrl(), userdata.getToken());

                                                                                            }

                                                                                            @Override
                                                                                            public void onCancelled(@NonNull DatabaseError error) {

                                                                                            }
                                                                                        });


                                                                                    }
                                                                                });

                                                                    }

                                                                    @Override
                                                                    public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                                                                        Toast.makeText(ViewAppointmentActivity.this, "Permission Denied", Toast.LENGTH_SHORT).show();
                                                                    }

                                                                    @Override
                                                                    public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken token) {
                                                                        token.continuePermissionRequest();
                                                                    }
                                                                }).check();



                                                            }
                                                        });

                                                        //start the Video Call...
                                                        binding.videoCallIcon.setOnClickListener(new View.OnClickListener() {
                                                            @Override
                                                            public void onClick(View v) {
                                                                //check the permission for camera...
                                                                Dexter.withActivity(ViewAppointmentActivity.this).withPermission(Manifest.permission.CAMERA).withListener(new PermissionListener() {
                                                                    @Override
                                                                    public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                                                                        Dexter.withActivity(ViewAppointmentActivity.this).withPermission(Manifest.permission.RECORD_AUDIO).withListener(new com.karumi.dexter.listener.single.PermissionListener() {
                                                                            @Override
                                                                            public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                                                                                String messageBody = myId;


                                                                                //initializing the object of the model class and passing value to its constructor to start call...
                                                                                CallData callData = new CallData("calling", "VC", myId, accType, receiverUId, receiverType, "");

                                                                                // generating unique key for storing data in database...
                                                                                String randomKey = reference.push().getKey();

                                                                                //storing data for database...
                                                                                reference.child("Call")
                                                                                        .child(meetingRoom)
                                                                                        .setValue(callData).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                            @Override
                                                                                            public void onSuccess(Void unused) {
                                                                                                String callRandomkey = reference.push().getKey();
                                                                                                Date date = new Date();
                                                                                                mDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
                                                                                                mTime = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
                                                                                                double timeStamp = date.getTime();

                                                                                                reference.child("Users").child(accType).child(myId).addListenerForSingleValueEvent(new ValueEventListener() {
                                                                                                    @Override
                                                                                                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                                                                                                        //i am using StudentData class for both doctor and student to get his/her profilePic, name and category.
                                                                                                        StudentData data = snapshot.getValue(StudentData.class);
                                                                                                        //send notification to the receiver...

                                                                                                        SendNotification sendNotification = new SendNotification("Incoming Call", data.getUsername(), data.getToken(), accType, myId, ViewAppointmentActivity.this, "Call");
                                                                                                        sendNotification.sendNotification();

                                                                                                        //initializing NotificationData(Model Class) Object and passing values to its constructor...
                                                                                                        NotificationData notificationData = new NotificationData(callRandomkey, myId, accType, "Video Call", data.getUsername(), "Video Call", false, date.getTime());

                                                                                                        //storing data in database to show the call notification Notification Activity...
                                                                                                        FirebaseDatabase.getInstance().getReference().child("Notifications").child(receiverUId).child(callRandomkey).setValue(notificationData);

                                                                                                        //open the OutgoingCallACtivity to wiat for the receiver to atend the call...
                                                                                                        nextActivity(receiverType, userdata.getUsername(), userdata.getProfileUrl(), userdata.getToken());

                                                                                                    }

                                                                                                    @Override
                                                                                                    public void onCancelled(@NonNull DatabaseError error) {

                                                                                                    }
                                                                                                });


                                                                                            }
                                                                                        });

                                                                            }

                                                                            @Override
                                                                            public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                                                                                Toast.makeText(ViewAppointmentActivity.this, "Permission Denied", Toast.LENGTH_SHORT).show();
                                                                            }

                                                                            @Override
                                                                            public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken token) {
                                                                                token.continuePermissionRequest();
                                                                            }
                                                                        }).check();

                                                                    }

                                                                    @Override
                                                                    public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                                                                        Toast.makeText(ViewAppointmentActivity.this, "Permission Denied", Toast.LENGTH_SHORT).show();
                                                                    }

                                                                    @Override
                                                                    public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                                                                        permissionToken.continuePermissionRequest();
                                                                    }
                                                                }).check();

                                                            }
                                                        });

                                                        binding.chatIcon.setOnClickListener(new View.OnClickListener() {
                                                            @Override
                                                            public void onClick(View v) {
                                                                Intent intent = new Intent(ViewAppointmentActivity.this, ChatActivity.class);
                                                                Bundle sendValues = new Bundle();
                                                                sendValues.putString("userId",receiverUId);
                                                                sendValues.putString("username", userdata.getUsername());
                                                                sendValues.putString("profileUri", userdata.getProfileUrl());
                                                                sendValues.putString("myId", myId);
                                                                sendValues.putString("token", userdata.getToken());
                                                                sendValues.putString("rType", receiverType);
                                                                intent.putExtras(sendValues);
                                                                startActivity(intent);
                                                            }
                                                        });

                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError error) {

                                                    }
                                                });

                                        binding.patientName.setText(appointmentData.getPatientName());
                                        binding.patientProblem.setText(appointmentData.getProblemDetail());
                                        binding.patientAge.setText(appointmentData.getPatientAge());
                                        binding.appointDate.setText(appointmentData.getAppointDate());
                                        binding.appointTime.setText(appointmentData.getAppointTime());



                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });

                        if (appointmentListData.getAppointStatus().equals("Pending")){
                            if (accType.equals("Doctor")) {
                                binding.appointStatus.setText("Appointment request is pending.");
                                binding.completeAppoint.setVisibility(View.GONE);
                                binding.rejectAppoint.setVisibility(View.GONE);
                                binding.acceptAppoint.setVisibility(View.VISIBLE);
                                binding.rejectAppoint.setVisibility(View.VISIBLE);
                            }
                            else {
                                binding.appointStatus.setText("Please wait for the doctor to approve your appointment.");
                            }
                        }
                        else if(appointmentListData.getAppointStatus().equals("Rejected")
                                || appointmentListData.getAppointStatus().equals("Completed")){
                            binding.acceptAppoint.setVisibility(View.GONE);
                            binding.rejectAppoint.setVisibility(View.GONE);
                            binding.completeAppoint.setVisibility(View.GONE);
                            binding.resheduleAppoint.setVisibility(View.VISIBLE);
                            if (appointmentListData.getAppointStatus().equals("Rejected")){
                                if (accType.equals("Doctor")){
                                    binding.appointStatus.setText("You rejected this appointment.");
                                }else {
                                    binding.appointStatus.setText("Your appointment has been rejected by doctor.");
                                }
                            }
                            else {
                                binding.appointStatus.setText("Your appointment completed successfully");
                            }
                        }
                        else {
//                            if (accType.equals("Doctor")){
                                binding.completeAppoint.setVisibility(View.VISIBLE);
                                binding.resheduleAppoint.setVisibility(View.VISIBLE);
                                binding.acceptAppoint.setVisibility(View.GONE);
                                binding.rejectAppoint.setVisibility(View.GONE);
//                            }
                            binding.appointStatus.setText(appointmentListData.getAppointStatus());
                        }
                        binding.loadingBar.setVisibility(View.GONE);
                        binding.viewAppointLayout.setVisibility(View.VISIBLE);


                        //resheduling the existing appointment...
                        binding.resheduleAppoint.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                String appointStatus = binding.appointStatus.getText().toString();
                                if (appointStatus.equals("Your appointment completed successfully") && !accType.equals("Doctor")){
                                    FirebaseDatabase.getInstance().getReference().child("Users").child("Doctor")
                                            .child(appointmentListData.getUserId()).addValueEventListener(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                    DoctorData doctorData = snapshot.getValue(DoctorData.class);
                                                    Intent intent = new Intent(ViewAppointmentActivity.this, ViewDocProfile.class);
                                                    Bundle bundle = new Bundle();
                                                    bundle = getIntent().getExtras();
                                                    bundle.putString("userId", doctorData.getUserId());
                                                    bundle.putString("name", doctorData.getUsername());
                                                    bundle.putString("profile", doctorData.getProfileUrl());
                                                    bundle.putString("rate", doctorData.getSessionPrice());
                                                    bundle.putString("category", doctorData.getCategory());
                                                    bundle.putString("info", doctorData.getBiography());
                                                    bundle.putString("patients", doctorData.getPatients()+"");
                                                    bundle.putString("experience", doctorData.getExperience());
                                                    bundle.putString("rating", doctorData.getRating()+"");
                                                    bundle.putString("token", doctorData.getToken());
                                                    intent.putExtras(bundle);
                                                    startActivity(intent);

                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError error) {

                                                }
                                            });


                                } else if (appointStatus.contains("rejected")) {
                                    binding.acceptAppoint.setVisibility(View.GONE);
                                    binding.rejectAppoint.setVisibility(View.GONE);
                                    binding.completeAppoint.setVisibility(View.GONE);
                                    binding.resheduleAppoint.setVisibility(View.GONE);
                                    binding.pickDate.setVisibility(View.VISIBLE);
                                    binding.pickTime.setVisibility(View.VISIBLE);
                                    binding.updateShedule.setVisibility(View.VISIBLE);

                                }
                            }
                        });

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

        binding.rejectAppoint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatabaseReference appointReference = FirebaseDatabase.getInstance().getReference().child("AppointList");
                appointReference.child(myId).child(appointId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        AppointmentListData data = snapshot.getValue(AppointmentListData.class);
                        Date date = new Date();
                        HashMap<String, Object> appointMap = new HashMap<>();
                        appointMap.put("appointStatus", "Rejected");
                        appointMap.put("timestamp", date.getTime());
                            if (accType.equals("Doctor")){
                                appointReference.child(myId).child(appointId).updateChildren(appointMap)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        appointReference.child(data.getUserId()).child(appointId).updateChildren(appointMap);
                                    }
                                });
                            }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });

        binding.acceptAppoint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatabaseReference appointReference = FirebaseDatabase.getInstance().getReference().child("AppointList");
                appointReference.child(myId).child(appointId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        AppointmentListData data = snapshot.getValue(AppointmentListData.class);
                        Date date = new Date();
                        HashMap<String, Object> appointMap = new HashMap<>();
                        appointMap.put("appointStatus", "InProgress");
                        appointMap.put("timestamp", date.getTime());
                        if (accType.equals("Doctor")){
                            appointReference.child(myId).child(appointId).updateChildren(appointMap)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            appointReference.child(data.getUserId()).child(appointId).updateChildren(appointMap);
                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });

        binding.completeAppoint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatabaseReference appointReference = FirebaseDatabase.getInstance().getReference().child("AppointList");
                appointReference.child(myId).child(appointId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        AppointmentListData data = snapshot.getValue(AppointmentListData.class);
//                        if (data.getAppointStatus().equals("InProgress")) {
                            if (!accType.equals("Doctor")) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(ViewAppointmentActivity.this);
                                ViewGroup viewGroup = findViewById(android.R.id.content);
                                View dialogView = LayoutInflater.from(ViewAppointmentActivity.this).inflate(R.layout.feedback_dialoge, viewGroup, false);
                                Button submitFeedback=dialogView.findViewById(R.id.submitFeedback);
                                RatingBar ratingBar = dialogView.findViewById(R.id.ratingBar);
                                EditText feedbackInput = dialogView.findViewById(R.id.feedbackInput);
                                builder.setView(dialogView);
                                final AlertDialog alertDialog = builder.create();
                                Date date = new Date();
                                submitFeedback.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        float rating = ratingBar.getRating();
                                        String feedbackMsg = feedbackInput.getText().toString().trim();
                                        HashMap<String, Object> appointMap = new HashMap<>();
                                        appointMap.put("appointStatus", "Completed");
                                        appointMap.put("timestamp", date.getTime());
                                        appointReference.child(myId).child(appointId).updateChildren(appointMap)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void unused) {
                                                        FirebaseDatabase.getInstance().getReference().child("Users").child("Doctor").
                                                                child(appointmentListData.getUserId()).addListenerForSingleValueEvent(new ValueEventListener() {
                                                                    @Override
                                                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                                        DoctorData doctorData = dataSnapshot.getValue(DoctorData.class);
                                                                        float totalRating = (float) ((rating+doctorData.getRating())/2);
                                                                        totalRating = (float) (Math.round(totalRating*10.0)/10.0);

                                                                        int totalPatients = doctorData.getPatients()+1;
                                                                        HashMap<String, Object> hashMap = new HashMap<>();
                                                                        hashMap.put("rating",totalRating);
                                                                        hashMap.put("patients", totalPatients);
                                                                        dataSnapshot.getRef().updateChildren(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                            @Override
                                                                            public void onSuccess(Void unused) {
                                                                                appointReference.child(data.getUserId()).child(appointId).updateChildren(appointMap)
                                                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                            @Override
                                                                                            public void onSuccess(Void unused) {
                                                                                                String feedbackId = FirebaseDatabase.getInstance().getReference().push().getKey();
                                                                                                FeedbackData feedbackData = new FeedbackData(feedbackId, myId,  feedbackMsg, accType, rating, date.getTime());
                                                                                                FirebaseDatabase.getInstance().getReference().child("Feedback").child(appointmentListData.getUserId())
                                                                                                        .child(feedbackId).setValue(feedbackData).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                                            @Override
                                                                                                            public void onSuccess(Void unused) {
                                                                                                                alertDialog.dismiss();
                                                                                                            }
                                                                                                        });
                                                                                            }
                                                                                        });

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
                                });
                                alertDialog.show();

                            }
                            else {
                                appointReference.child(myId).child(appointId).child("appointStatus").setValue("Request to complete appointment generated successfully")
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                appointReference.child(data.getUserId()).child(appointId).child("appointStatus")
                                                        .setValue("Request to complete appointment generated successfully");

                                            }
                                        });
                            }
//                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });

//        binding.resheduleAppoint.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                String appointStatus = binding.appointStatus.getText().toString();
//                if (appointStatus.equals("Your appointment completed successfully") && !accType.equals("Doctor")){
//
//                } else if (appointStatus.contains("rejected")) {
//                    binding.acceptAppoint.setVisibility(View.GONE);
//                    binding.rejectAppoint.setVisibility(View.GONE);
//                    binding.completeAppoint.setVisibility(View.GONE);
//                    binding.resheduleAppoint.setVisibility(View.GONE);
//                    binding.pickDate.setVisibility(View.VISIBLE);
//                    binding.pickTime.setVisibility(View.VISIBLE);
//                    binding.updateShedule.setVisibility(View.VISIBLE);
//
//                }
//            }
//        });

        binding.cancelReShedule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.pickDate.setVisibility(View.GONE);
                binding.pickTime.setVisibility(View.GONE);
                binding.updateShedule.setVisibility(View.GONE);
                binding.cancelReShedule.setVisibility(View.GONE);
                String appointStatus = binding.appointStatus.getText().toString();
                if (appointStatus.equals("Appointment request is pending.") ||
                        appointStatus.equals("Please wait for the doctor to approve your appointment.")){
                    if (accType.equals("Doctor")) {
                        binding.completeAppoint.setVisibility(View.GONE);
                        binding.rejectAppoint.setVisibility(View.GONE);
                        binding.acceptAppoint.setVisibility(View.VISIBLE);
                        binding.rejectAppoint.setVisibility(View.VISIBLE);
                    }
                }
                else if(appointStatus.equals("You rejected this appointment.")
                        || appointStatus.equals("Your appointment has been rejected by doctor.")
                        || appointStatus.equals("Your appointment completed successfully")){
                    binding.acceptAppoint.setVisibility(View.GONE);
                    binding.rejectAppoint.setVisibility(View.GONE);
                    binding.completeAppoint.setVisibility(View.GONE);
                    binding.resheduleAppoint.setVisibility(View.VISIBLE);

                }
                else {
                    if (accType.equals("Doctor")){
                        binding.completeAppoint.setVisibility(View.VISIBLE);
                        binding.resheduleAppoint.setVisibility(View.VISIBLE);
                        binding.acceptAppoint.setVisibility(View.GONE);
                        binding.rejectAppoint.setVisibility(View.GONE);
                    }
                }

            }
        });

        binding.pickTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.datePicker.setVisibility(View.GONE);
                binding.timePickerLayout.setVisibility(View.VISIBLE);
                binding.timePicker.setVisibility(View.VISIBLE);
                binding.setOnlineTimerText.setVisibility(View.VISIBLE);
                binding.cancelOTimer.setVisibility(View.VISIBLE);
            }
        });

        binding.pickDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.setOnlineTimerText.setVisibility(View.GONE);
                binding.timePicker.setVisibility(View.GONE);
                binding.timePickerLayout.setVisibility(View.VISIBLE);
                binding.datePicker.setVisibility(View.VISIBLE);
                binding.setDate.setVisibility(View.VISIBLE);
                binding.cancelODate.setVisibility(View.VISIBLE);
            }
        });


        binding.setDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.pickDate.setText(setDateString(binding.datePicker.getMonth(), binding.datePicker.getDayOfMonth()));
                hideDatePicker();
            }
        });

        binding.setOnlineTimerText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.pickTime.setText(setTimeString(binding.timePicker.getHour(), binding.timePicker.getMinute()));
                hideTimePicker();
            }
        });

        binding.cancelODate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideDatePicker();
            }
        });

        binding.cancelOTimer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideTimePicker();
            }
        });






    }


    private void nextActivity(String receiverType, String username, String profileUrl, String token){
        //opening OutgoingCallActivity when audio or video call started...
        Intent intent = new Intent(ViewAppointmentActivity.this, OutgoingCallActivity.class);

        Bundle bundle = new Bundle();
        bundle.putString("userType", receiverType);
        bundle.putString("userId", receiverUId);
        bundle.putString("myId", myId);
        bundle.putString("profileUri", profileUrl);
        bundle.putString("username", username);
        bundle.putString("token", token);
        intent.putExtras(bundle);
        startActivity(intent);
    }


    private void hideDatePicker() {
        binding.setDate.setVisibility(View.GONE);
        binding.datePicker.setVisibility(View.GONE);
        binding.timePickerLayout.setVisibility(View.GONE);
        binding.cancelODate.setVisibility(View.GONE);
    }

    private void hideTimePicker() {
        binding.setOnlineTimerText.setVisibility(View.GONE);
        binding.timePicker.setVisibility(View.GONE);
        binding.timePickerLayout.setVisibility(View.GONE);
        binding.cancelOTimer.setVisibility(View.GONE);
    }


    private String setDateString(int month, int dayOfMonth) {
        String mName = "", dateString = "";
        if (month == 0) {
            if (dayOfMonth > 31){
                dayOfMonth %= 31;
                mName = "Feb";
            }
            else {
                mName = "Jan";
            }
        }
        else if (month == 1) {
            if (dayOfMonth > 28){
                dayOfMonth %= 28;
                mName = "Mar";
            }
            else {
                mName = "Feb";
            }
        }
        else if (month == 2) {
            if (dayOfMonth > 31){
                dayOfMonth %= 31;
                mName = "Apr";
            }
            else {
                mName = "Mar";
            }
        }
        else if (month == 3) {
            if (dayOfMonth > 30){
                dayOfMonth %= 30;
                mName = "May";
            }
            else {
                mName = "Apr";
            }
        }
        else if (month == 4) {
            if (dayOfMonth > 31){
                dayOfMonth %= 31;
                mName = "Jun";
            }
            else {
                mName = "May";
            }
        }
        else if (month == 5) {
            if (dayOfMonth > 30){
                dayOfMonth %= 30;
                mName = "Jul";
            }
            else {
                mName = "Jun";
            }
        }
        else if (month == 6) {
            if (dayOfMonth > 31){
                dayOfMonth %= 31;
                mName = "Aug";
            }
            else {
                mName = "Jul";
            }
        }
        else if (month == 7) {
            if (dayOfMonth > 31){
                dayOfMonth %= 31;
                mName = "Sep";
            }
            else {
                mName = "Aug";
            }
        }
        else if (month == 8) {
            if (dayOfMonth > 30){
                dayOfMonth %= 30;
                mName = "Oct";
            }
            else {
                mName = "Sep";
            }
        }
        else if (month == 9) {
            if (dayOfMonth > 31) {
                dayOfMonth %= 31;
                mName = "Nov";
            }
            else {
                mName = "Oct";
            }
        }
        else if (month == 10) {
            if (dayOfMonth > 30){
                dayOfMonth %= 30;
                mName = "Dec";
            }
            else {
                mName = "Nov";
            }
        }
        else if (month == 11) {
            if (dayOfMonth > 31) {
                dayOfMonth %= 31;
                mName = "Jan";
            }
            else {
                mName = "Dec";
            }
        }
        if (dayOfMonth<10){
            dateString = mName + "\n0" + dayOfMonth;
        }
        else {
            dateString = mName + "\n" + dayOfMonth;
        }
        return dateString;

    }

    private String setTimeString(int hour, int minute) {

        String timeString = "", minuteString = "";
        if (minute < 10){
            minuteString = "0" + minute;
        }
        else if (minute == 0){
            minuteString = "00";
        }
        else {
            minuteString = minute + "";
        }

        if (hour > 12) {
            hour = hour % 12;
            if (hour == 0){
                timeString = "12:" + minuteString + " PM";
            }
            else if (hour < 10) {
                timeString = "0" + hour + ":" + minuteString +" PM";
            } else {
                timeString = hour + ":" + minuteString + " PM";
            }
        }
        else {
            if (hour == 0) {
                timeString = "12:" + minuteString +  " AM";
            }
            else if (hour < 10) {
                timeString = "0" + hour + ":" + minuteString + " AM";
            } else {
                timeString = hour + ":" + minuteString + " AM";
            }
        }
        return timeString;
    }



}