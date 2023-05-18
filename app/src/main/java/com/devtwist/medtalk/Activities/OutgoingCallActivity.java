package com.devtwist.medtalk.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Toast;

import com.devtwist.medtalk.Models.CallData;
import com.devtwist.medtalk.R;
import com.devtwist.medtalk.databinding.ActivityOutgoingCallBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.jitsi.meet.sdk.JitsiMeetActivity;
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions;

import java.net.MalformedURLException;
import java.net.URL;

public class OutgoingCallActivity extends AppCompatActivity {

    private ActivityOutgoingCallBinding binding;

    private Intent intent;
    private Bundle bundle;

    //used to store passed values got from intent...
    private String username, profileUri, userId, myId, userToken, meetingRoom, status, userType;

    //used to start the ringtone when activity start...
    private MediaPlayer player;

    //used to start the waiting timer to wait for the receiver to attend the call...
    private CountDownTimer countDownTimer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOutgoingCallBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());



        //geting passed values from ChatActivity...
        intent = getIntent();
        bundle = intent.getExtras();


        myId = bundle.getString("myId");
        userId = bundle.getString("userId");
        userType = bundle.getString("userType");
        username = bundle.getString("username");
        profileUri = bundle.getString("profileUri");
        userToken = bundle.getString("token");

        //generating meeting room id to create the meeting room id to access the meeting room in realtime database...
        meetingRoom = myId+userId;

        //initializing and creating audio player...
//        player = MediaPlayer.create(OutgoingCallActivity.this,R.raw.call_beep_sound);

        //start the timer to wait for the user to attend the call...
        countDownTimer = new CountDownTimer(40000,2000) {
            @Override
            public void onTick(long millisUntilFinished) {
                player.start();
            }

            @Override
            public void onFinish() {
                //stop the player if user does not attend the call...
//                player.stop();

                //update the status of the call to not allow the user to join the call after ending timer...
                FirebaseDatabase.getInstance().getReference().child("Call").child(meetingRoom)
                        .child("callStatus").setValue("ended");
                Toast.makeText(OutgoingCallActivity.this, "User is not receiving the\ncall please try again later", Toast.LENGTH_SHORT).show();
                //move to previous activity...
                finish();
            }
        };
        //start the timer...
        countDownTimer.start();

        //load and set the profile image of the receiver...
        if (profileUri.length()>1) {
            Picasso.get().load(profileUri).placeholder(R.drawable.enter_profile_icon).into(binding.ogCallProfilePic);
        }

        //set the data of the user in views...
        binding.ogCallUsername.setText(username);
        binding.ogCallStatus.setText("Calling");

        //end the call
        binding.endCallbUTTON.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //stop the ring...
//                player.stop();

                //updating meeting rom that user end the call...
                FirebaseDatabase.getInstance().getReference().child("Call").child(meetingRoom)
                        .child("callStatus").setValue("ended");
                countDownTimer.cancel();
                finish();
            }
        });

        //checking is user attend the call or not...
        FirebaseDatabase.getInstance().getReference().child("Call").child(meetingRoom).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                CallData callData = snapshot.getValue(CallData.class);

                //end the call if receiver reject the call...
                if (callData.getCallStatus().equals("rejected")){
//                    player.stop();
                    countDownTimer.cancel();
                    Toast.makeText(OutgoingCallActivity.this, "User is busy\nPlease try later", Toast.LENGTH_LONG).show();
                    finish();
                }
                //join the cal if receiver attend the calll...
                if (callData.getCallStatus().equals("attended")){
                    countDownTimer.cancel();

                    //stop the ring...
//                    player.stop();

                    //join with video if title equal VC...
                    if (callData.getType().equals("VC")) {
                        joinVideoCall(meetingRoom);
                    }

                    //join with audio only if title equal AC...
                    else if(callData.getType().equals("AC")){
                        joinAudioCall(meetingRoom);
                    }
                }
                if(callData.getCallStatus().equals("ended")){

//                    player.stop();
                    countDownTimer.cancel();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    //ask the user that he/she really want to end the call or not when he/she press back button...
    @Override
    public void onBackPressed() {

        new AlertDialog.Builder(OutgoingCallActivity.this)
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
                        countDownTimer.cancel();
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
                    .setFeatureFlag("welcomepage.enabled", false)
                    .setConfigOverride("MedTalk", true)
                    .setRoom(meetingRoom)
                    .build();
            //starting video call...
            JitsiMeetActivity.launch(OutgoingCallActivity.this, options);
            finish();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    //joining call without video using Jitsi meet api...
    private void joinAudioCall(String meetingRoom) {
        try {
            //initializing audio call...
            JitsiMeetConferenceOptions options = new JitsiMeetConferenceOptions.Builder()
                    .setServerURL(new URL("https://meet.jit.si"))
                    .setRoom(meetingRoom)
                    .setFeatureFlag("welcomepage.enabled", false)
                    .setAudioOnly(true)
                    .setConfigOverride("MedTalk", true)
                    .build();
            //starting audio call...
            JitsiMeetActivity.launch(OutgoingCallActivity.this, options);
            finish();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

}