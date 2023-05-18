package com.devtwist.medtalk.Activities;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.telecom.Call;
import android.view.View;
import android.widget.Toast;

import com.devtwist.medtalk.Models.CallData;
import com.devtwist.medtalk.Models.DoctorData;
import com.devtwist.medtalk.Models.PatientData;
import com.devtwist.medtalk.Models.StudentData;
import com.devtwist.medtalk.R;
import com.devtwist.medtalk.databinding.ActivityIncomingCallBinding;
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

import org.jitsi.meet.sdk.JitsiMeetActivity;
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions;

import java.net.MalformedURLException;
import java.net.URL;

public class IncomingCallActivity extends AppCompatActivity {

    private ActivityIncomingCallBinding binding;

    //used to get passed values from MyFirebaseServices or NotificationAdapter...
    private Intent intent;

    //used to store passed values got from intent...
    private String username, profileUri, userType, userId, myId, meetingRoom;

    private DatabaseReference callReference;

    //used to start the ringtone when activity start...
    private MediaPlayer player;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityIncomingCallBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        //getting values from MyFirebaseServices or NotificationActivity...
        intent = getIntent();

        //getting userId of the sender...

        userId = intent.getStringExtra("userId");
        userType = intent.getStringExtra("userType");

        //getting id of the current user from firebase auth...
        myId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        //generating meeting room id to access the meeting room in firebase database...
        meetingRoom = userId+myId;

        //storing the reference of the meeting room in callReference object...
        callReference = FirebaseDatabase.getInstance().getReference().child("Call").child(meetingRoom);

        //initializing and creating audio player...
        player = MediaPlayer.create(IncomingCallActivity.this, R.raw.call_ringtoon);

        //start ringtone...
        player.start();

        callReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //retrieving data of sender to showthe values in views...
                CallData callData = snapshot.getValue(CallData.class);
                if (callData.getType().equals("Doctor")){
                    FirebaseDatabase.getInstance().getReference().child("Users").child("Doctor").child(userId)
                            .addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    DoctorData userdata = snapshot.getValue(DoctorData.class);
                                    profileUri = userdata.getProfileUrl();
                                    username = userdata.getUsername();
                                    if (userdata.getProfileUrl().toString().length()>1) {
                                        Picasso.get().load(profileUri).placeholder(R.drawable.enter_profile_icon).into(binding.icCallProfilePic);
                                    }
                                    binding.icCallUsername.setText(username);
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                }
                else if (callData.getType().equals("Patient")){
                    FirebaseDatabase.getInstance().getReference().child("Users").child("Patient").child(userId)
                            .addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    PatientData userdata = snapshot.getValue(PatientData.class);
                                    profileUri = userdata.getProfileUrl();
                                    username = userdata.getUsername();
                                    if (userdata.getProfileUrl().toString().length()>1) {
                                        Picasso.get().load(profileUri).placeholder(R.drawable.enter_profile_icon).into(binding.icCallProfilePic);
                                    }
                                    binding.icCallUsername.setText(username);
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                }
                else if (callData.getType().equals("Student")){
                    FirebaseDatabase.getInstance().getReference().child("Users").child("Student").child(userId)
                            .addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    StudentData userdata = snapshot.getValue(StudentData.class);
                                    profileUri = userdata.getProfileUrl();
                                    username = userdata.getUsername();
                                    if (userdata.getProfileUrl().toString().length()>1) {
                                        Picasso.get().load(profileUri).placeholder(R.drawable.enter_profile_icon).into(binding.icCallProfilePic);
                                    }
                                    binding.icCallUsername.setText(username);
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        //accepting call...


        //cheking if the sender end the or not...
        FirebaseDatabase.getInstance().getReference().child("Call").child(meetingRoom).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                //retrieving type and status of the call...
                String type = snapshot.child("type").getValue(String.class);
                String status = snapshot.child("callStatus").getValue(String.class);

                //set Voice call in _icCallStatus view if type is AC otherwise Video Call...
                if (type.equals("AC")){
                    binding.icCallStatus.setText("Voice Call"+"\n"+"Ringing");
                }
                else if (type.equals("VC")){
                    binding.icCallStatus.setText("Video Call");
                }
                //stop the ringtone and move back to previous activity if sender end the call...
                if (status.equals("ended")){
                    player.stop();
                    Toast.makeText(IncomingCallActivity.this, "Call ended by\n" + username, Toast.LENGTH_LONG).show();
                    finish();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        binding.icAttendCallButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //check if user allow the audio permission or not if no then ask for permission...
                Dexter.withActivity(IncomingCallActivity.this).withPermission(Manifest.permission.RECORD_AUDIO).withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        //stop the ringtone...
                        player.stop();

                        //updating status in meeting Room...
                        callReference.child("callStatus").setValue("attended");
                        callReference.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                CallData callData = snapshot.getValue(CallData.class);
                                //join with video if title equal VC...
                                if (callData.getType().equals("VC")){
                                    joinVideoCall(meetingRoom);
                                }
                                //join with only audio if title equal AC...
                                else if (callData.getType().equals("AC")){
                                    joinAudioCall(meetingRoom);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        Toast.makeText(IncomingCallActivity.this, "Permission Denied", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();
            }
        });

        //denied the call
        binding.icRejectCallButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //stop the ringtone...
                player.stop();

                //update the meeting room...
                FirebaseDatabase.getInstance().getReference().child("Call").child(meetingRoom)
                        .child("callStatus").setValue("rejected");
                finish();
            }
        });

    }

    //ask the user that he/she really want to end the call or not when he/she press back button...
    @Override
    public void onBackPressed() {

        new AlertDialog.Builder(IncomingCallActivity.this)
                .setTitle("End this call")
                .setIcon(R.drawable.app_logo)
                .setMessage("Are you sure you want to end this call?")

                // Specifying a listener allows you to take an action before dismissing the dialog.
                // The dialog is automatically dismissed when a dialog button is clicked.
                .setPositiveButton("End Call", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        player.stop();
                        FirebaseDatabase.getInstance().getReference().child("Call").child(meetingRoom)
                                .child("callStatus").setValue("ended");
                        finish();
                    }
                }).setNegativeButton("Keep calling", null)
                .setCancelable(false)
                .show();
    }


    //joining call with video using Jitsi meet api...
    private void joinVideoCall(String meetingRoom) {
        try {
            //initializing video call...
            JitsiMeetConferenceOptions options = new JitsiMeetConferenceOptions.Builder()
                    .setServerURL(new URL("https://meet.jit.si"))
                    .setRoom(meetingRoom)
                    .setFeatureFlag("welcomepage.enabled", false)
                    .setConfigOverride("Thriftie", true)
                    .build();
            //starting video call...
            JitsiMeetActivity.launch(IncomingCallActivity.this, options);
            finish();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    //joining call with out video using Jitsi meet api...
    private void joinAudioCall(String meetingRoom) {
        try {
            //initializing audio call...
            JitsiMeetConferenceOptions options = new JitsiMeetConferenceOptions.Builder()
                    .setServerURL(new URL("https://meet.jit.si"))
                    .setRoom(meetingRoom)
                    .setFeatureFlag("welcomepage.enabled", false)
                    .setAudioOnly(true)
                    .setConfigOverride("Thriftie", true)
                    .build();

            //starting audio call...
            JitsiMeetActivity.launch(IncomingCallActivity.this, options);
            finish();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }



}