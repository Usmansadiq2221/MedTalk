package com.devtwist.medtalk.Models;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.devtwist.medtalk.Activities.ChatActivity;
import com.devtwist.medtalk.Activities.IncomingCallActivity;

import com.devtwist.medtalk.Activities.NotificationActivity;
import com.devtwist.medtalk.Fragments.ChatListFragment;
import com.devtwist.medtalk.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

public class MyFirebaseService extends FirebaseMessagingService {
    private String messageText,userId, userType;

    private NotificationManager mNotifyManager;

    private Intent intent;

    //set the data and open the new activity...
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String channel_id = "1";


        Uri notifyRing = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Ringtone ringtone = RingtoneManager.getRingtone(getApplicationContext(), notifyRing);
        ringtone.play();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ringtone.setLooping(false);
        }

        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        long[] pattern = {100, 300, 300, 300};
        vibrator.vibrate(pattern,-1);

//        int resourceImage = getResources().getIdentifier(remoteMessage.getNotification().getIcon());

        if (remoteMessage.getData().get("toGo").equals("Call")){
            intent = new Intent(this, IncomingCallActivity.class);
            intent.putExtra("userType", remoteMessage.getData().get("userType"));
            intent.putExtra("userId", remoteMessage.getData().get("userId"));
        }
        else {
            intent = new Intent(this, NotificationActivity.class);
        }

        PendingIntent pendingIntent;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            pendingIntent = PendingIntent.getActivity(this, 1, intent,
                    PendingIntent.FLAG_MUTABLE);
        }
        else
        {
            pendingIntent = PendingIntent.getActivity(this, 1, intent,
                    PendingIntent.FLAG_ONE_SHOT);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channel_id);
        builder.setSmallIcon(R.drawable.app_logo);
        builder.setContentTitle(remoteMessage.getNotification().getTitle());
        builder.setContentText(remoteMessage.getNotification().getBody());
        builder.setContentIntent(pendingIntent);
        builder.setAutoCancel(true);
//        builder.setStyle(new NotificationCompat.BigTextStyle().bigText(remoteMessage.getNotification().getBody()));
        builder.setPriority(Notification.PRIORITY_MAX);

        mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);


        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channel_id,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_HIGH);
            mNotifyManager.createNotificationChannel(channel);
        }


        mNotifyManager.notify(0, builder.build());


    }

}
