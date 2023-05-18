package com.devtwist.medtalk.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Toast;

import com.devtwist.medtalk.Models.BlockListData;
import com.devtwist.medtalk.Models.PatientData;
import com.devtwist.medtalk.Models.StudentData;
import com.devtwist.medtalk.R;
import com.devtwist.medtalk.databinding.ActivityViewDocProfileBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class ViewDocProfile extends AppCompatActivity {

    private ActivityViewDocProfileBinding binding;
    private Bundle bundle, sendValues;

    private String myId, token, docId, docName, docProfileUrl, docRate,
            docCategory, docInfo, docPatients, docExperience, docRating, accType;

//    private int hour, month, minutes, date;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityViewDocProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        SharedPreferences preferences = getSharedPreferences("MedTalkData", MODE_PRIVATE);
        accType = preferences.getString("accType", "");

        bundle = getIntent().getExtras();
        docId = bundle.getString("userId");
        docName = bundle.getString("name");
        docProfileUrl = bundle.getString("profile");
        docRate = bundle.getString("rate");
        docCategory = bundle.getString("category");
        docInfo = bundle.getString("info");
        docPatients = "Patient\n \n" + bundle.getString("patients");
        docExperience = "Experience\n \n" +  bundle.getString("experience");
        docRating = "Rating\n \n" + bundle.getString("rating");
        token = bundle.getString("token");
//        hour = month = minutes = date = 0;

        myId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        if (docProfileUrl.length()>1){
            Picasso.get().load(docProfileUrl).into(binding.profileimage);
        }

        binding.username.setText(docName);
        binding.category.setText(docCategory);
        binding.patients.setText(docPatients);
        binding.experience.setText(docExperience);
        binding.rating.setText(docRating);
        binding.biography.setText(docInfo);


        binding.backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        binding.chatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ViewDocProfile.this, ChatActivity.class);
                sendValues = new Bundle();
                sendValues.putString("userId",docId);
                sendValues.putString("username", docName);
                sendValues.putString("profileUri", docProfileUrl);
                sendValues.putString("myId", myId);
                sendValues.putString("token", token);
                sendValues.putString("rType", "Doctor");
                intent.putExtras(sendValues);
                startActivity(intent);
            }
        });

        binding.blockDoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                new AlertDialog.Builder(ViewDocProfile.this)
                        .setTitle("MedTalk")
                        .setIcon(R.drawable.app_logo)
                        .setMessage("Are you sure you want to block this doctor?")

                        // Specifying a listener allows you to take an action before dismissing the dialog.
                        // The dialog is automatically dismissed when a dialog button is clicked.
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // Continue with block operation
                                BlockListData blockListData = new BlockListData("Doctor", docId);
                                FirebaseDatabase.getInstance().getReference().child("BlockList").child(myId).child(docId)
                                        .setValue(blockListData).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                Toast.makeText(ViewDocProfile.this, "User Successfully added\nin your block list", Toast.LENGTH_SHORT).show();
                                                finish();
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(ViewDocProfile.this, "Please check your\ninternet Connection!", Toast.LENGTH_SHORT).show();
                                            }
                                        });

                            }
                        }).setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {

                            }
                        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        }).setCancelable(true)
                        .show();

            }
        });

        if (accType.equals("Student")) {
            binding.bookAppoint.setText("Book An Appointment");
        }
        else {
            binding.bookAppoint.setText("Book Consultation");
        }

        binding.fOptionTime.setText(setTimeString(binding.timePicker.getHour()+2, 0));
        binding.appointTime.setText(setTimeString(binding.timePicker.getHour()+4, 0));
        binding.lOptionTime.setText(setTimeString(binding.timePicker.getHour()+6, 0));

        binding.fOptionDay.setText(setDateString(binding.datePicker.getMonth(), binding.datePicker.getDayOfMonth() + 1));
        binding.appointDay.setText(setDateString(binding.datePicker.getMonth(), binding.datePicker.getDayOfMonth() + 3));
        binding.lOptionDay.setText(setDateString(binding.datePicker.getMonth(), binding.datePicker.getDayOfMonth() + 6));

        binding.appointTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.docProfileLayout.animate().alpha(0.5f).setDuration(500);
                binding.datePicker.setVisibility(View.GONE);
                binding.timePickerLayout.setVisibility(View.VISIBLE);
                binding.timePicker.setVisibility(View.VISIBLE);
                binding.setOnlineTimerText.setVisibility(View.VISIBLE);
                binding.cancelOTimer.setVisibility(View.VISIBLE);
            }
        });

        binding.appointDay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.docProfileLayout.animate().alpha(0.5f).setDuration(500);
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
                binding.appointDay.setText(setDateString(binding.datePicker.getMonth(), binding.datePicker.getDayOfMonth()));
                hideDatePicker();
            }
        });

        binding.setOnlineTimerText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.appointTime.setText(setTimeString(binding.timePicker.getHour(), binding.timePicker.getMinute()));
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

        binding.bookAppoint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ViewDocProfile.this, CreateAppointActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("time", binding.appointTime.getText().toString());
                bundle.putString("date", binding.appointDay.getText().toString());
                bundle.putString("docId", docId);
                bundle.putString("useId", myId);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });

    }

    private void hideDatePicker() {
        binding.setDate.setVisibility(View.GONE);
        binding.datePicker.setVisibility(View.GONE);
        binding.timePickerLayout.setVisibility(View.GONE);
        binding.cancelODate.setVisibility(View.GONE);
        binding.docProfileLayout.animate().alpha(1f).setDuration(500);
    }

    private void hideTimePicker() {
        binding.setOnlineTimerText.setVisibility(View.GONE);
        binding.timePicker.setVisibility(View.GONE);
        binding.timePickerLayout.setVisibility(View.GONE);
        binding.cancelOTimer.setVisibility(View.GONE);
        binding.docProfileLayout.animate().alpha(1f).setDuration(500);
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
            dateString = mName + " 0" + dayOfMonth;
        }
        else {
            dateString = mName + " " + dayOfMonth;
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