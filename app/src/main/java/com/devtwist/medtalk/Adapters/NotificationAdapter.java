package com.devtwist.medtalk.Adapters;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.devtwist.medtalk.Activities.ChatActivity;
import com.devtwist.medtalk.Activities.IncomingCallActivity;
import com.devtwist.medtalk.Models.CallData;
import com.devtwist.medtalk.Models.DoctorData;
import com.devtwist.medtalk.Models.NotificationData;
import com.devtwist.medtalk.Models.PatientData;
import com.devtwist.medtalk.Models.StudentData;
import com.devtwist.medtalk.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>{

    //for storing context of the activity passed from Call Fragment...
    private Context context;

    //for storing the list of calls data...
    private ArrayList<NotificationData> notificationList;


    private String myId, notificationType, senderId, username, userProfileUrl, userToken;

    //initializing the list and variables and list in constructor passed from the NotificationActivity...
    public NotificationAdapter(Context context, ArrayList<NotificationData> notificationList, String myId) {
        this.context = context;
        this.notificationList = notificationList;
        this.myId = myId;
    }


    // We are inflating the notification_items
    // inside on Create View Holder method...
    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.notification_items, parent,false);
        return new NotificationViewHolder(view);
    }

    // Inside on bind view holder we will
    // set data to item of Recycler View...
    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        NotificationData model = notificationList.get(position);
        notificationType = model.getNotificationType();

        holder._notificationTitleItem.setText(model.getNotificationTitle());
        holder._notificationMessageItem.setText(model.getNotificationMessage());

        if (notificationType.equals("post") || notificationType.equals("message") || notificationType.equals("Audio Call")
                || notificationType.equals("Video Call")) {

            senderId = model.getSenderId();
            if (model.getSenderType().equals("Doctor")){

                FirebaseDatabase.getInstance().getReference().child("Users").child("Doctor")
                        .child(senderId).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                DoctorData userdata = snapshot.getValue(DoctorData.class);
                                userProfileUrl = userdata.getProfileUrl();
                                username = userdata.getUsername();
                                userToken = userdata.getToken();
                                String notificationId = model.getNotificationId();
                                if (model.getSeen()){
                                    holder.itemView.setAlpha(0.5f);
                                }

                                if (userProfileUrl.length()>1) {
                                    Picasso.get().load(userProfileUrl).placeholder(R.drawable.enter_profile_icon).into(holder._notificationProfileItem);
                                }

                                holder.itemView.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        holder.itemView.setAlpha(0.7f);
                                        FirebaseDatabase.getInstance().getReference().child("Notifications").child(myId)
                                                .child(notificationId).child("seen").setValue(true);
                                        if (model.getNotificationType().equals("message")) {
                                            Intent intent = new Intent(v.getContext(), ChatActivity.class);
                                            Bundle bundle = new Bundle();
                                            bundle.putString("myId", myId);
                                            bundle.putString("userId", userdata.getUserId());
                                            bundle.putString("username", userdata.getUsername());
                                            bundle.putString("profileUri", userdata.getProfileUrl());
                                            bundle.putString("token", userdata.getToken());
                                            intent.putExtras(bundle);
                                            v.getContext().startActivity(intent);

                                        }

                                        if (model.getNotificationType().equals("Video Call")){
                                            FirebaseDatabase.getInstance().getReference().child("Call").child(userdata.getUserId()+myId)
                                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                            CallData callData = snapshot.getValue(CallData.class);
                                                            if (callData.getCallStatus().equals("rejected") || callData.getCallStatus().equals("attended")
                                                                    || callData.getCallStatus().equals("ended")){
                                                                if (callData.getCallStatus().equals("ended")) {
                                                                    Toast.makeText(context, "Sorry You miss this call", Toast.LENGTH_SHORT).show();
                                                                }
                                                                else{
                                                                    Intent intent = new Intent(v.getContext(), ChatActivity.class);
                                                                    Bundle bundle = new Bundle();
                                                                    bundle.putString("myId", myId);
                                                                    bundle.putString("userId", userdata.getUserId());
                                                                    bundle.putString("username", userdata.getUsername());
                                                                    bundle.putString("profileUri", userdata.getProfileUrl());
                                                                    bundle.putString("token", userdata.getToken());
                                                                    intent.putExtras(bundle);
                                                                    v.getContext().startActivity(intent);
                                                                }
                                                            }else{
                                                                Intent intent = new Intent(v.getContext(), IncomingCallActivity.class);
                                                                Bundle bundle = new Bundle();
                                                                bundle.putString("nType", "VC");
                                                                bundle.putString("userId",userdata.getUserId());
                                                                intent.putExtras(bundle);
                                                                v.getContext().startActivity(intent);
                                                            }
                                                        }

                                                        @Override
                                                        public void onCancelled(@NonNull DatabaseError error) {

                                                        }
                                                    });
                                        }

                                        if (model.getNotificationType().equals("Audio Call")){
                                            FirebaseDatabase.getInstance().getReference().child("Call").child(userdata.getUserId()+myId)
                                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                            CallData callData = snapshot.getValue(CallData.class);
                                                            if (callData.getCallStatus().equals("rejected") || callData.getCallStatus().equals("attended")
                                                                    || callData.getCallStatus().equals("ended")){
                                                                if (callData.getCallStatus().equals("ended")) {
                                                                    Toast.makeText(context, "Sorry You miss this call", Toast.LENGTH_SHORT).show();
                                                                }
                                                                else{
                                                                    Intent intent = new Intent(v.getContext(), ChatActivity.class);
                                                                    Bundle bundle = new Bundle();
                                                                    bundle.putString("myId", myId);
                                                                    bundle.putString("userId", userdata.getUserId());
                                                                    bundle.putString("username", userdata.getUsername());
                                                                    bundle.putString("profileUri", userdata.getProfileUrl());
                                                                    bundle.putString("token", userdata.getToken());
                                                                    intent.putExtras(bundle);
                                                                    v.getContext().startActivity(intent);
                                                                }
                                                            }else{
                                                                Intent intent = new Intent(v.getContext(), IncomingCallActivity.class);
                                                                Bundle bundle = new Bundle();
                                                                bundle.putString("nType", "AC");
                                                                bundle.putString("userId",userdata.getUserId());
                                                                intent.putExtras(bundle);
                                                                v.getContext().startActivity(intent);
                                                            }
                                                        }

                                                        @Override
                                                        public void onCancelled(@NonNull DatabaseError error) {

                                                        }
                                                    });

                                        }
                                    }
                                });
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

            }
            else if (model.getSenderType().equals("Patient")){
                FirebaseDatabase.getInstance().getReference().child("Users").child("Patient")
                        .child(senderId).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                PatientData userdata = snapshot.getValue(PatientData.class);
                                userProfileUrl = userdata.getProfileUrl();
                                username = userdata.getUsername();
                                userToken = userdata.getToken();
                                String notificationId = model.getNotificationId();
                                if (model.getSeen()){
                                    holder.itemView.setAlpha(0.5f);
                                }

                                if (userProfileUrl.length()>1) {
                                    Picasso.get().load(userProfileUrl).placeholder(R.drawable.enter_profile_icon).into(holder._notificationProfileItem);
                                }

                                holder.itemView.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        holder.itemView.setAlpha(0.7f);
                                        FirebaseDatabase.getInstance().getReference().child("Notifications").child(myId)
                                                .child(notificationId).child("seen").setValue(true);
                                        if (model.getNotificationType().equals("message")) {
                                            Intent intent = new Intent(v.getContext(), ChatActivity.class);
                                            Bundle bundle = new Bundle();
                                            bundle.putString("myId", myId);
                                            bundle.putString("userId", userdata.getUserId());
                                            bundle.putString("username", userdata.getUsername());
                                            bundle.putString("profileUri", userdata.getProfileUrl());
                                            bundle.putString("token", userdata.getToken());
                                            intent.putExtras(bundle);
                                            v.getContext().startActivity(intent);

                                        }

                                        if (model.getNotificationType().equals("Video Call")){
                                            FirebaseDatabase.getInstance().getReference().child("Call").child(userdata.getUserId()+myId)
                                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                            CallData callData = snapshot.getValue(CallData.class);
                                                            if (callData.getCallStatus().equals("rejected") || callData.getCallStatus().equals("attended")
                                                                    || callData.getCallStatus().equals("ended")){
                                                                if (callData.getCallStatus().equals("ended")) {
                                                                    Toast.makeText(context, "Sorry You miss this call", Toast.LENGTH_SHORT).show();
                                                                }
                                                                else{
                                                                    Intent intent = new Intent(v.getContext(), ChatActivity.class);
                                                                    Bundle bundle = new Bundle();
                                                                    bundle.putString("myId", myId);
                                                                    bundle.putString("userId", userdata.getUserId());
                                                                    bundle.putString("username", userdata.getUsername());
                                                                    bundle.putString("profileUri", userdata.getProfileUrl());
                                                                    bundle.putString("token", userdata.getToken());
                                                                    intent.putExtras(bundle);
                                                                    v.getContext().startActivity(intent);
                                                                }
                                                            }else{
                                                                Intent intent = new Intent(v.getContext(), IncomingCallActivity.class);
                                                                Bundle bundle = new Bundle();
                                                                bundle.putString("nType", "VC");
                                                                bundle.putString("userId",userdata.getUserId());
                                                                intent.putExtras(bundle);
                                                                v.getContext().startActivity(intent);
                                                            }
                                                        }

                                                        @Override
                                                        public void onCancelled(@NonNull DatabaseError error) {

                                                        }
                                                    });
                                        }

                                        if (model.getNotificationType().equals("Audio Call")){
                                            FirebaseDatabase.getInstance().getReference().child("Call").child(userdata.getUserId()+myId)
                                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                            CallData callData = snapshot.getValue(CallData.class);
                                                            if (callData.getCallStatus().equals("rejected") || callData.getCallStatus().equals("attended")
                                                                    || callData.getCallStatus().equals("ended")){
                                                                if (callData.getCallStatus().equals("ended")) {
                                                                    Toast.makeText(context, "Sorry You miss this call", Toast.LENGTH_SHORT).show();
                                                                }
                                                                else{
                                                                    Intent intent = new Intent(v.getContext(), ChatActivity.class);
                                                                    Bundle bundle = new Bundle();
                                                                    bundle.putString("myId", myId);
                                                                    bundle.putString("userId", userdata.getUserId());
                                                                    bundle.putString("username", userdata.getUsername());
                                                                    bundle.putString("profileUri", userdata.getProfileUrl());
                                                                    bundle.putString("token", userdata.getToken());
                                                                    intent.putExtras(bundle);
                                                                    v.getContext().startActivity(intent);
                                                                }
                                                            }else{
                                                                Intent intent = new Intent(v.getContext(), IncomingCallActivity.class);
                                                                Bundle bundle = new Bundle();
                                                                bundle.putString("nType", "AC");
                                                                bundle.putString("userId",userdata.getUserId());
                                                                intent.putExtras(bundle);
                                                                v.getContext().startActivity(intent);
                                                            }
                                                        }

                                                        @Override
                                                        public void onCancelled(@NonNull DatabaseError error) {

                                                        }
                                                    });

                                        }
                                    }
                                });
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

            }
            else if (model.getSenderType().equals("Student")){
                FirebaseDatabase.getInstance().getReference().child("Users").child("Student")
                        .child(senderId).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                StudentData userdata = snapshot.getValue(StudentData.class);
                                userProfileUrl = userdata.getProfileUrl();
                                username = userdata.getUsername();
                                userToken = userdata.getToken();
                                String notificationId = model.getNotificationId();
                                if (model.getSeen()){
                                    holder.itemView.setAlpha(0.5f);
                                }

                                if (userProfileUrl.length()>1) {
                                    Picasso.get().load(userProfileUrl).placeholder(R.drawable.enter_profile_icon).into(holder._notificationProfileItem);
                                }

                                holder.itemView.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        holder.itemView.setAlpha(0.7f);
                                        FirebaseDatabase.getInstance().getReference().child("Notifications").child(myId)
                                                .child(notificationId).child("seen").setValue(true);
                                        if (model.getNotificationType().equals("message")) {
                                            Intent intent = new Intent(v.getContext(), ChatActivity.class);
                                            Bundle bundle = new Bundle();
                                            bundle.putString("myId", myId);
                                            bundle.putString("userId", userdata.getUserId());
                                            bundle.putString("username", userdata.getUsername());
                                            bundle.putString("profileUri", userdata.getProfileUrl());
                                            bundle.putString("token", userdata.getToken());
                                            intent.putExtras(bundle);
                                            v.getContext().startActivity(intent);

                                        }

                                        if (model.getNotificationType().equals("Video Call")){
                                            FirebaseDatabase.getInstance().getReference().child("Call").child(userdata.getUserId()+myId)
                                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                            CallData callData = snapshot.getValue(CallData.class);
                                                            if (callData.getCallStatus().equals("rejected") || callData.getCallStatus().equals("attended")
                                                                    || callData.getCallStatus().equals("ended")){
                                                                if (callData.getCallStatus().equals("ended")) {
                                                                    Toast.makeText(context, "Sorry You miss this call", Toast.LENGTH_SHORT).show();
                                                                }
                                                                else{
                                                                    Intent intent = new Intent(v.getContext(), ChatActivity.class);
                                                                    Bundle bundle = new Bundle();
                                                                    bundle.putString("myId", myId);
                                                                    bundle.putString("userId", userdata.getUserId());
                                                                    bundle.putString("username", userdata.getUsername());
                                                                    bundle.putString("profileUri", userdata.getProfileUrl());
                                                                    bundle.putString("token", userdata.getToken());
                                                                    intent.putExtras(bundle);
                                                                    v.getContext().startActivity(intent);
                                                                }
                                                            }else{
                                                                Intent intent = new Intent(v.getContext(), IncomingCallActivity.class);
                                                                Bundle bundle = new Bundle();
                                                                bundle.putString("nType", "VC");
                                                                bundle.putString("userId",userdata.getUserId());
                                                                intent.putExtras(bundle);
                                                                v.getContext().startActivity(intent);
                                                            }
                                                        }

                                                        @Override
                                                        public void onCancelled(@NonNull DatabaseError error) {

                                                        }
                                                    });
                                        }

                                        if (model.getNotificationType().equals("Audio Call")){
                                            FirebaseDatabase.getInstance().getReference().child("Call").child(userdata.getUserId()+myId)
                                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                            CallData callData = snapshot.getValue(CallData.class);
                                                            if (callData.getCallStatus().equals("rejected") || callData.getCallStatus().equals("attended")
                                                                    || callData.getCallStatus().equals("ended")){
                                                                if (callData.getCallStatus().equals("ended")) {
                                                                    Toast.makeText(context, "Sorry You miss this call", Toast.LENGTH_SHORT).show();
                                                                }
                                                                else{
                                                                    Intent intent = new Intent(v.getContext(), ChatActivity.class);
                                                                    Bundle bundle = new Bundle();
                                                                    bundle.putString("myId", myId);
                                                                    bundle.putString("userId", userdata.getUserId());
                                                                    bundle.putString("username", userdata.getUsername());
                                                                    bundle.putString("profileUri", userdata.getProfileUrl());
                                                                    bundle.putString("token", userdata.getToken());
                                                                    intent.putExtras(bundle);
                                                                    v.getContext().startActivity(intent);
                                                                }
                                                            }else{
                                                                Intent intent = new Intent(v.getContext(), IncomingCallActivity.class);
                                                                Bundle bundle = new Bundle();
                                                                bundle.putString("nType", "AC");
                                                                bundle.putString("userId",userdata.getUserId());
                                                                intent.putExtras(bundle);
                                                                v.getContext().startActivity(intent);
                                                            }
                                                        }

                                                        @Override
                                                        public void onCancelled(@NonNull DatabaseError error) {

                                                        }
                                                    });

                                        }
                                    }
                                });
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
            }

        }
        else {
            holder._notificationProfileItem.setBackground(null);
            holder._notificationProfileItem.setImageResource(R.drawable.app_logo);
        }


    }

    // this method will return the count of our list...
    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    public class NotificationViewHolder extends RecyclerView.ViewHolder{

        // Adapter class for initializing
        // the views of our notification_items view...
        private ImageView _notificationProfileItem;
        private TextView _notificationTitleItem, _notificationMessageItem;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            _notificationProfileItem = (ImageView) itemView.findViewById(R.id._notificationProfileItem);
            _notificationTitleItem = (TextView) itemView.findViewById(R.id._notificationTitleItem);
            _notificationMessageItem = (TextView) itemView.findViewById(R.id._notificationMessageItem);
        }
    }
}
