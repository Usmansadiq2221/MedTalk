package com.devtwist.medtalk.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.devtwist.medtalk.Adapters.MessageAdapter;
import com.devtwist.medtalk.Models.CallData;
import com.devtwist.medtalk.Models.ChatListData;
import com.devtwist.medtalk.Models.MessageData;
import com.devtwist.medtalk.Models.NotificationData;
import com.devtwist.medtalk.Models.SendNotification;
import com.devtwist.medtalk.R;
import com.devtwist.medtalk.databinding.ActivityChatBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class ChatActivity extends AppCompatActivity {

    private ActivityChatBinding binding;

    private String receiverProfileString, senderUsername, username, receiverUId, senderUId, userToken;

    private MessageAdapter messageAdapter;

    // used to retrieve and store messages data...
    private ArrayList<MessageData> messages;

    // senderRoom will store the id for sender...
    // receiverRoom will store the Id for Receiver...
    // mDate will store the current date for sending message...
    // mTime will store the time for sending message...
    private String timeString, senderRoom, receiverRoom, mDate, mTime, myType = "", senderType = "", receiverType = "", senderProfileString;

    // used to store the file path  of the selected image for message...
    private Uri filePath;

    // used to getting data from ChatlistAdapter or ViewPostActivity or NotificationAdapter...
    private Intent intent;

    // used to get and set valuie for intent...
    private Bundle bundle;

    private FirebaseDatabase database;
    private DatabaseReference reference,reference2;
    private FirebaseStorage storage;
    private ProgressDialog dialog;
    //used to initialize the layout manager for _preMessagesView(RecyclerView)...
    private LinearLayoutManager linearLayoutManager;

    // used to check the the messages seend by the receiver or not...
    private ValueEventListener msgSeenListener1, msgSeenListener2;

    private boolean isAudio = true, isRecording = false;

    private int secCount = 0;
    private MediaRecorder recorder = null;

    private CountDownTimer recTimer;

    private String audioFileName = "", audioFilePath = "", fileName = "" ;

    private static final String LOG_TAG = "Recorder_log";

    private boolean isFile = false, isIconGreen = false;
    private static final int REQUEST_PDF_FILE = 1;
    Uri pdfFileUri = null;






    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        SharedPreferences preferences = getSharedPreferences("MedTalkData", MODE_PRIVATE);
        senderType = preferences.getString("accType", "");

        try {


            //geting intent values from ChatListAdapter or ViewPostActivity or NotificationAdapter...
            intent = getIntent();
            bundle = intent.getExtras();
            receiverUId = bundle.getString("userId");
            receiverProfileString = bundle.getString("profileUri");
            username = bundle.getString("username");
            userToken = bundle.getString("token");
            receiverType = bundle.getString("rType");
            recorder = new MediaRecorder();

            fileName = "audio-" + System.currentTimeMillis() + ".mp4";
            audioFilePath = getExternalFilesDir(null).getAbsolutePath() + "/" + fileName;
//            audioFileName = Environment.getExternalStorageDirectory() + File.separator
//                    + Environment.DIRECTORY_DCIM + File.separator + "/recorded_audio.3gp";


            // getting id of current user from firebase...
            senderUId = FirebaseAuth.getInstance().getCurrentUser().getUid();

            filePath = null;

            //initializing progress dialoge for uploading image for message...
            dialog = new ProgressDialog(this);
            dialog.setMessage("Uploading Image...");
            dialog.setCancelable(false);

            binding.chatUsername.setText(username);


            // passing and opening SellerProfileActivity to show the user Profile...
            /*
            binding.chatUsername.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    intent = new Intent(ChatActivity.this, SellerProfileActivity.class);
                    bundle.putString("name", username);
                    bundle.putString("phone", receiverUId);
                    bundle.putString("pic", profilePicString);
                    intent.putExtras(bundle);
                    startActivity(intent);
                }
            });
             */

            //initializing chat room for sender and receiver to store messages...
            senderRoom = senderUId + receiverUId;
            receiverRoom = receiverUId + senderUId;

            // storing username of the current user in senderUsername...
            FirebaseDatabase.getInstance().getReference().child("Users").child(senderType)
                    .child(senderUId).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            senderUsername = snapshot.child("username").getValue(String.class);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

            //initialize arraylist for storing previous messages of the chat...
            messages = new ArrayList<>();

            //initialize MaeesageAdapter and pass value to its constructor...
            messageAdapter = new MessageAdapter(this, messages, senderUId,receiverUId, receiverProfileString, senderType, receiverType);

            // initialize database objects...
            database = FirebaseDatabase.getInstance();
            storage = FirebaseStorage.getInstance();

            try {
                database.getReference().child("Chat")
                        .child(senderRoom)
                        .child("messages")
                        .addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                try {
                                    // clearing previous data of messages array list...
                                    messages.clear();
                                    // retrieving and storing previous messages data in messages arraylist...
                                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                        MessageData messageData = dataSnapshot.getValue(MessageData.class);
                                        messages.add(messageData);
                                    }

                                    // sort the messages array list on time...
                                    messages.sort(Comparator.comparing(MessageData::getTimestamp));
                                } catch (Exception e) {
                                    Log.i("ChatError", e.getMessage().toString());
                                }
                                binding.preMessagesView.hideShimmerAdapter();
                                //hiding loading animation for messages...

                                // it will notify the adapter to mif any changes occure in the data of messages to modify the RecyclerView...
                                messageAdapter.notifyDataSetChanged();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
            } catch (Exception e) {
                Log.i("Error Chat", e.getMessage().toString());
            }

            //initializing layout manager for Recycler View...
            linearLayoutManager = new LinearLayoutManager(this);
            linearLayoutManager.setStackFromEnd(true);
            //linearLayoutManager.setReverseLayout(true);
            binding.preMessagesView.setLayoutManager(linearLayoutManager);
            binding.preMessagesView.setAdapter(messageAdapter);

            //it will show the loading animation while data is retrieving from data base in messages array list...
            binding.preMessagesView.showShimmerAdapter();

            //set the position of recycler view to end of list...
            binding.preMessagesView.scrollToPosition(messages.size()-1);

            binding.messageInput.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    checkText();
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });

            binding.imageMessage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //check is user allow the permission of external storage or not if no then ask for permission...
                    Dexter.withActivity(ChatActivity.this)
                            .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                            .withListener(new PermissionListener() {
                                @Override
                                public void onPermissionGranted(PermissionGrantedResponse response) {

                                    //selecting image from phone gallery...
                                    Intent select = new Intent(Intent.ACTION_PICK);
                                    select.setType("image/*");
                                    startActivityForResult(Intent.createChooser(select,"Select an Image"), 1);

                                }

                                @Override
                                public void onPermissionDenied(PermissionDeniedResponse response) {
                                    //show the toast message to the user if user denied the permission of the external storage...
                                    Toast.makeText(ChatActivity.this, "Permission Denied", Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                                    token.continuePermissionRequest();
                                }
                            }).check();



                }
            });

            binding.selectFileButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Dexter.withActivity(ChatActivity.this)
                            .withPermission(Manifest.permission.RECORD_AUDIO)
                            .withListener(new PermissionListener() {
                                @Override
                                public void onPermissionGranted(PermissionGrantedResponse response) {

                                    isFile = true;

                                    String[] supportedMimeTypes = {"application/pdf"};
                                    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);

                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                                        intent.setType(supportedMimeTypes.length == 1 ? supportedMimeTypes[0] : "*/*");
                                        if (supportedMimeTypes.length > 0) {
                                            intent.putExtra(Intent.EXTRA_MIME_TYPES, supportedMimeTypes);
                                        }
                                    } else {
                                        String mimeTypes = "";
                                        for (String mimeType : supportedMimeTypes) {
                                            mimeTypes += mimeType + "|";
                                        }
                                        intent.setType(mimeTypes.substring(0,mimeTypes.length() - 1));
                                    }
                                    startActivityForResult(intent, REQUEST_PDF_FILE);
                                }

                                @Override
                                public void onPermissionDenied(PermissionDeniedResponse response) {
                                    //show the toast message to the user if user denied the permission of the external storage...
                                    Toast.makeText(ChatActivity.this, "Permission Denied", Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                                    token.continuePermissionRequest();
                                }
                            }).check();
                }
            });

            binding.sendFileButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    binding.sendFileButton.setVisibility(View.GONE);
                    binding.fileUploadProgress.setVisibility(View.VISIBLE);
                    sendFileMessage();
                }
            });

            binding.removeFile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    isFile = false;
                    pdfFileUri = null;
                    binding.docFileName.setText("");

                    binding.fileIcon.setImageResource(R.drawable.baseline_picture_as_pdf_24);
                    binding.docLayout.setVisibility(View.GONE);
                    binding.sendFileButton.setVisibility(View.GONE);
                    binding.recAudioButton.setVisibility(View.VISIBLE);
                    binding.selectFileButton.setVisibility(View.VISIBLE);
                    binding.messageInput.setVisibility(View.VISIBLE);
                    binding.imageMessage.setVisibility(View.VISIBLE);
                }
            });


            binding.sendMessage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    // get input data and store in realtime database...
                    sendMessage();
                }
            });

            //start voice Call...
            binding.chatACallIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    //check the permission for microphone...
                    Dexter.withActivity(ChatActivity.this).withPermission(Manifest.permission.RECORD_AUDIO).withListener(new PermissionListener() {
                        @Override
                        public void onPermissionGranted(PermissionGrantedResponse response) {
                            String messageBody = senderUId;

                            //initializing the object of the model class and passing value to its constructor to start call...
                            CallData callData = new CallData("calling", "AC", senderUId, senderType, receiverUId, receiverType, "");

                            // generating unique key for storing data in database...
                            String randomKey = database.getReference().push().getKey();

                            //storing data for database...
                            database.getReference().child("Call")
                                    .child(senderRoom)
                                    .setValue(callData).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            String callRandomkey = database.getReference().push().getKey();
                                            Date date = new Date();
                                            mDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
                                            mTime = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
                                            double timeStamp = date.getTime();


                                            //sending notification to the receiver to attend or reject the call...
//                                            SendNotification sendNotification = new SendNotification("Audio Call", messageBody, userToken, senderUId, ChatActivity.this);
//                                            sendNotification.sendNotification();

                                            //initializing NotificationData(Model Class) Object and passing values to its constructor...
                                            NotificationData notificationData = new NotificationData(callRandomkey, senderUId, senderType, "Audio Call", senderUsername, "Voice Call", false, date.getTime());

                                            //storing data in database to show the call notification Notification Activity...
                                            FirebaseDatabase.getInstance().getReference().child("Notifications").child(receiverUId).child(callRandomkey).setValue(notificationData);


                                        }
                                    });
                            //open the OutgoingCallACtivity to wiat for the receiver to atend the call...
                            nextActivity();
                        }

                        @Override
                        public void onPermissionDenied(PermissionDeniedResponse response) {
                            //show the toast message to the user if user will not allow the permission asked from the user...
                            Toast.makeText(ChatActivity.this, "Permission Denied", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                            token.continuePermissionRequest();
                        }
                    }).check();

                }
            });

            //start the Video Call...
            binding.chatVCallIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //check the permission for camera...
                    Dexter.withActivity(ChatActivity.this).withPermission(Manifest.permission.CAMERA).withListener(new PermissionListener() {
                        @Override
                        public void onPermissionGranted(PermissionGrantedResponse response) {
                            //check the permission for microphone...
                            Dexter.withActivity(ChatActivity.this).withPermission(Manifest.permission.RECORD_AUDIO).withListener(new PermissionListener() {
                                @Override
                                public void onPermissionGranted(PermissionGrantedResponse response) {
                                    String messageBody = senderUId;

                                    //initializing the object of the model class and passing value to its constructor to start call...
                                    CallData callData = new CallData("calling", "VC", senderUId, senderType, receiverUId, receiverType, "");

                                    // generating unique key for storing data in database...
                                    String randomKey = database.getReference().push().getKey();

                                    //storing values in database...
                                    database.getReference().child("Call")
                                            .child(senderRoom)
                                            .setValue(callData).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void unused) {
                                                    //generating unique key to store the data of the call...
                                                    String callRandomkey = database.getReference().push().getKey();
                                                    Date date = new Date();
                                                    mDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
                                                    mTime = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());

                                                    //storing call data for receiver in database to show the call in notification activity...
                                                    NotificationData notificationData = new NotificationData(callRandomkey, senderUId, senderType, "Video Call", senderUsername, "Video Call", false, date.getTime());
                                                    FirebaseDatabase.getInstance().getReference().child("Notifications").child(receiverUId).child(callRandomkey).setValue(notificationData);

                                                    //sending notification to the receiver to attend or reject the call..
//                                                    SendNotification sendNotification = new SendNotification("Video Call", messageBody, userToken, senderUId, ChatActivity.this);
//                                                    sendNotification.sendNotification();
                                                }
                                            });
                                    nextActivity();
                                }

                                @Override
                                public void onPermissionDenied(PermissionDeniedResponse response) {
                                    Toast.makeText(ChatActivity.this, "Permission Denied", Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                                    token.continuePermissionRequest();
                                }
                            }).check();
                        }

                        @Override
                        public void onPermissionDenied(PermissionDeniedResponse response) {
                            Toast.makeText(ChatActivity.this, "Permission Denied", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                            token.continuePermissionRequest();
                        }
                    }).check();

                }
            });

            binding.cancelChatImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //removing selecting image from _chatImage(view)
                    binding.cancelChatImage.setVisibility(View.GONE);
                    binding.chatImage.setVisibility(View.GONE);
                    binding.chatImage.setImageResource(com.google.firebase.inappmessaging.display.R.drawable.image_placeholder);
                    binding.imageMessage.setVisibility(View.VISIBLE);
                    checkText();
                    filePath = null;
                }
            });

            binding.deleteAudioButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    timeString = "";
                    secCount = 0;
                    binding.recAudioLayout.setVisibility(View.GONE);
                    binding.sendVoiceButton.setVisibility(View.GONE);
                    binding.selectFileButton.setVisibility(View.VISIBLE);
                    binding.messageInput.setVisibility(View.VISIBLE);
                    binding.imageMessage.setVisibility(View.VISIBLE);
                    binding.recAudioButton.setVisibility(View.VISIBLE);
                }
            });

            //passing image data to ViewDocsActivity to show the full image in ViewDocsActivity...
            binding.chatImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent viewIntent = new Intent(ChatActivity.this, ViewDocumentsActivity.class);
                    Bundle viewBundle = new Bundle();
                    viewBundle.putString("tag","i");
                    viewBundle.putString("imgUrl", filePath.toString());
                    viewIntent.putExtras(viewBundle);
                    startActivity(viewIntent);
                }
            });


        } catch (Exception e) {
            Log.i("Chat Error", e.getMessage().toString());
        }


        binding.recAudioButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN){
                    Dexter.withActivity(ChatActivity.this)
                            .withPermission(Manifest.permission.RECORD_AUDIO)
                            .withListener(new PermissionListener() {
                                @Override
                                public void onPermissionGranted(PermissionGrantedResponse response) {
                                    binding.selectFileButton.setVisibility(View.GONE);
                                    binding.messageInput.setVisibility(View.GONE);
                                    binding.imageMessage.setVisibility(View.GONE);
                                    binding.recAudioLayout.setVisibility(View.VISIBLE);
                                    recTimer = new CountDownTimer(60000,1000) {
                                        @Override
                                        public void onTick(long millisUntilFinished) {

                                            if (secCount<10) {
                                                timeString = "00:0" + secCount;
                                            }
                                            else if (secCount<60){
                                                timeString = "00:" + secCount;
                                            }
                                            new CountDownTimer(500, 500) {
                                                @Override
                                                public void onTick(long millisUntilFinished) {
                                                    binding.voiceRecIcon.animate().alpha(0.25f).setDuration(500);
                                                }

                                                @Override
                                                public void onFinish() {
                                                    binding.voiceRecIcon.animate().alpha(1f).setDuration(500);
                                                    if (!isIconGreen){
                                                        binding.voiceRecIcon.setImageResource(R.drawable.baseline_mic_24_green);
                                                        isIconGreen = !isIconGreen;
                                                    }
                                                    else {
                                                        binding.voiceRecIcon.setImageResource(R.drawable.baseline_mic_24);
                                                        isIconGreen = !isIconGreen;
                                                    }
                                                }
                                            }.start();
                                            binding.audioRecTime.setText(timeString);
                                            ++secCount;
                                        }

                                        @Override
                                        public void onFinish() {
                                            setupAudio(timeString);
                                        }

                                    };

                                    startRecording();

                                    recTimer.start();
                                }

                                @Override
                                public void onPermissionDenied(PermissionDeniedResponse response) {
                                    //show the toast message to the user if user denied the permission of the external storage...
                                    Toast.makeText(ChatActivity.this, "Permission Denied", Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                                    token.continuePermissionRequest();
                                }
                            }).check();
                    //startRecording();

                }
                else if (event.getAction() == MotionEvent.ACTION_UP) {
                    recTimer.cancel();
                    stopRecording();
                    setupAudio(timeString);
                }
                return false;
            }
        });

        binding.sendVoiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendVoiceMessage();
            }
        });

        // close ChatActivity...
        binding.chatBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //this function call will check is receiver seen the new received messages or not...
        seenMessage(receiverUId);



    }

    private void sendFileMessage() {
        Date date = new Date();
        mDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
        mTime = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());

        //get message text entered by the user...
        String pdfFileName = binding.docFileName.getText().toString().trim();

        //generate unique id to store the data of message...
        String randomKey = database.getReference().push().getKey();

        if (pdfFileUri == null){
            Toast.makeText(ChatActivity.this, "Enter Message!", Toast.LENGTH_SHORT).show();
        }
        else {

            try {
                //Initialize calender...
                Calendar calendar = Calendar.getInstance();

                //storing reference in reference object...
                StorageReference reference = storage.getReference().child("Chats").child("Files").child(randomKey);


                //uploading selected image for message in database...
                reference.putFile(pdfFileUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {
                            //get url of the uploaded image to this in message data in realtime database...
                            reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String filePath = uri.toString();


                                    // initializing MessageData and model class to store data in database...
                                    MessageData messageData = new MessageData(randomKey, pdfFileName, senderUId, filePath, receiverRoom, mTime, mDate, date.getTime(), false, "pdf");

                                    //store data for sender...
                                    database.getReference().child("Chat")
                                            .child(senderRoom)
                                            .child("messages").child(randomKey)
                                            .setValue(messageData).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void unused) {
                                                    //store data for receiver...
                                                    database.getReference().child("Chat")
                                                            .child(receiverRoom)
                                                            .child("messages").child(randomKey)
                                                            .setValue(messageData).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void unused) {
                                                                    //generating unique id to store data for notification...
                                                                    String notificationId = senderUId; //FirebaseDatabase.getInstance().getReference().push().getKey();

                                                                    //initialize model class and pass values to its constructor to store data ion database...
                                                                    NotificationData notificationData = new NotificationData(notificationId, senderUId, senderType, "message", senderUsername, "Pdf File", false, date.getTime());

                                                                    //storing data in database to show message notifications in NotificationActivity of the receiver...
                                                                    FirebaseDatabase.getInstance().getReference().child("Notifications").child(receiverUId).child(notificationId).setValue(notificationData);

                                                                    //send notification to the receiver...
                                                                    SendNotification sendNotification = new SendNotification(senderUsername, "Pdf File", userToken, senderUId, senderType, ChatActivity.this, "Chat");
                                                                    sendNotification.sendNotification();


                                                                    double timeStamp = date.getTime();

                                                                    //storing userdata in Chatlist to show the receiver in the chat list of the sender...
                                                                    ChatListData chatListData = new ChatListData(receiverUId, username, "Pdf File", receiverType,  "pdf", timeStamp);
                                                                    database.getReference().child("ChatList").child(senderUId)
                                                                            .child(receiverUId).setValue(chatListData);

                                                                    //storing userdata in Chatlist to show the sender in the chat list of the receiver...
                                                                    chatListData = new ChatListData(senderUId, senderUsername, "Pdf File", senderType, "pdf", timeStamp);
                                                                    database.getReference().child("ChatList").child(receiverUId)
                                                                            .child(senderUId).setValue(chatListData);
                                                                    binding.fileUploadProgress.setVisibility(View.GONE);
                                                                    binding.docLayout.setVisibility(View.GONE);
                                                                    binding.sendFileButton.setVisibility(View.GONE);
                                                                    binding.recAudioButton.setVisibility(View.VISIBLE);
                                                                    binding.selectFileButton.setVisibility(View.VISIBLE);
                                                                    binding.messageInput.setVisibility(View.VISIBLE);
                                                                    binding.imageMessage.setVisibility(View.VISIBLE);
                                                                }
                                                            });
                                                }
                                            });
                                    binding.messageInput.setText("");
                                }
                            });
                        }
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    private void setupAudio(String timeString) {
        binding.recAudioButton.setVisibility(View.GONE);
        binding.sendVoiceButton.setVisibility(View.VISIBLE);
    }


    private void startRecording() {
//        recorder = new MediaRecorder();
//        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
//        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
//        recorder.setOutputFile(audioFileName);
//        System.out.println(audioFileName);
//        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
//
//        try {
//            recorder.prepare();
//            recorder.start();
//            isRecording = true;
//        } catch (IOException e) {
//            Log.e(LOG_TAG, "prepare() failed");
//            //e.printStackTrace();
//        }
        MediaRecorder recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        fileName = "audio-" + System.currentTimeMillis() + ".3gpp";
        audioFilePath = getExternalFilesDir(null).getAbsolutePath() + "/" + fileName;
        recorder.setOutputFile(audioFilePath);

        try {
            recorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }

        recorder.start();

    }

    private void stopRecording() {

        new CountDownTimer(2000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                if (recorder != null) {
                    try {
                        recorder.stop();
                        recorder.release();

                    } catch (RuntimeException e) {
                        e.printStackTrace();
                    }
                }

            }
        }.start();
    }

    private void sendVoiceMessage() {
        Uri aFilePath = Uri.fromFile(new File(audioFilePath));
        binding.sendVoiceButton.setVisibility(View.GONE);
        binding.audioUploadProgress.setVisibility(View.VISIBLE);
        Date date = new Date();
        mDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
        mTime = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());

        //generate unique id to store the data of message...
        String randomKey = database.getReference().push().getKey();

        if (audioFilePath == null){
            Toast.makeText(ChatActivity.this, "NO Audio Found!", Toast.LENGTH_SHORT).show();
        }
        else {

            try {
                //Initialize calender...
                Calendar calendar = Calendar.getInstance();

                //storing reference in reference object...
                StorageReference reference = storage.getReference().child("Chats").child("Audios").child(randomKey);


                //uploading selected image for message in database...
                reference.putFile(aFilePath).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {
                            //get url of the uploaded image to this in message data in realtime database...
                            reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String filePath = uri.toString();


                                    // initializing MessageData and model class to store data in database...
                                    MessageData messageData = new MessageData(randomKey, timeString, senderUId, filePath, receiverRoom, mTime, mDate, date.getTime(), false, "aud");

                                    //store data for sender...
                                    database.getReference().child("Chat")
                                            .child(senderRoom)
                                            .child("messages").child(randomKey)
                                            .setValue(messageData).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void unused) {
                                                    //store data for receiver...
                                                    database.getReference().child("Chat")
                                                            .child(receiverRoom)
                                                            .child("messages").child(randomKey)
                                                            .setValue(messageData).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void unused) {
                                                                    //generating unique id to store data for notification...
                                                                    String notificationId = senderUId; //FirebaseDatabase.getInstance().getReference().push().getKey();

                                                                    //initialize model class and pass values to its constructor to store data ion database...
                                                                    NotificationData notificationData = new NotificationData(notificationId, senderUId, senderType, "message", senderUsername, "aud", false, date.getTime());

                                                                    //storing data in database to show message notifications in NotificationActivity of the receiver...
                                                                    FirebaseDatabase.getInstance().getReference().child("Notifications").child(receiverUId).child(notificationId).setValue(notificationData);

                                                                    //send notification to the receiver...
                                                                    SendNotification sendNotification = new SendNotification(senderUsername, "Voice message", userToken, senderUId, senderType, ChatActivity.this, "Chat");
                                                                    sendNotification.sendNotification();


                                                                    double timeStamp = date.getTime();

                                                                    //storing userdata in Chatlist to show the receiver in the chat list of the sender...
                                                                    ChatListData chatListData = new ChatListData(receiverUId, username, "Voice", receiverType, "aud", timeStamp);
                                                                    database.getReference().child("ChatList").child(senderUId)
                                                                            .child(receiverUId).setValue(chatListData);

                                                                    //storing userdata in Chatlist to show the sender in the chat list of the receiver...
                                                                    chatListData = new ChatListData(senderUId, senderUsername, "Voice", senderType, "aud", timeStamp);
                                                                    database.getReference().child("ChatList").child(receiverUId)
                                                                            .child(senderUId).setValue(chatListData);
                                                                    binding.audioUploadProgress.setVisibility(View.GONE);
                                                                    binding.recAudioLayout.setVisibility(View.GONE);
                                                                    binding.sendVoiceButton.setVisibility(View.GONE);
                                                                    binding.recAudioButton.setVisibility(View.VISIBLE);
                                                                    binding.selectFileButton.setVisibility(View.VISIBLE);
                                                                    binding.messageInput.setVisibility(View.VISIBLE);
                                                                    binding.imageMessage.setVisibility(View.VISIBLE);
                                                                    timeString = "";
                                                                    secCount = 0;
                                                                }
                                                            });
                                                }
                                            });
                                    binding.messageInput.setText("");
                                }
                            });
                        }

                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }

    private void checkText() {
        if (binding.messageInput.getText().toString().length()>0){
            isAudio = false;
            binding.selectFileButton.setVisibility(View.GONE);
            binding.recAudioButton.setVisibility(View.GONE);
            binding.sendVoiceButton.setVisibility(View.VISIBLE);
            binding.sendMessage.setVisibility(View.VISIBLE);
        }
        else {
            isAudio = true;
            binding.sendMessage.setVisibility(View.GONE);
            binding.selectFileButton.setVisibility(View.VISIBLE);
            binding.recAudioButton.setVisibility(View.VISIBLE);
        }
    }


    //send data in firebase to show the messages to the receiver...
    private void sendMessage(){
        Date date = new Date();
        mDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
        mTime = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());

        //get message text entered by the user...
        String messageText = binding.messageInput.getText().toString().trim();

        //generate unique id to store the data of message...
        String randomKey = database.getReference().push().getKey();

        if (messageText.length()<1 && filePath ==null){
            Toast.makeText(ChatActivity.this, "Enter Message!", Toast.LENGTH_SHORT).show();
        }
        else {
            //if user select the image for message...
            if (filePath !=null){
                try {
                    //Initialize calender...
                    Calendar calendar = Calendar.getInstance();

                    //storing reference in reference object...
                    StorageReference reference = storage.getReference().child("Chats").child(calendar.getTimeInMillis() + "");
                    dialog.show();

                    //uploading selected image for message in database...
                    reference.putFile(filePath).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if(task.isSuccessful()){
                                //get url of the uploaded image to this in message data in realtime database...
                                reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        String filePath = uri.toString();
                                        dialog.dismiss();

                                        // initializing MessageData and model class to store data in database...
                                        MessageData messageData = new MessageData(randomKey, messageText, senderUId, filePath, receiverRoom,
                                                mTime, mDate, date.getTime(),false, "img");

                                        //store data for sender...
                                        database.getReference().child("Chat")
                                                .child(senderRoom)
                                                .child("messages").child(randomKey)
                                                .setValue(messageData).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void unused) {
                                                        //store data for receiver...
                                                        database.getReference().child("Chat")
                                                                .child(receiverRoom)
                                                                .child("messages").child(randomKey)
                                                                .setValue(messageData).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                    @Override
                                                                    public void onSuccess(Void unused) {
                                                                        //generating unique id to store data for notification...
                                                                        String notificationId = senderUId; //FirebaseDatabase.getInstance().getReference().push().getKey();

                                                                        //initialize model class and pass values to its constructor to store data ion database...
                                                                        NotificationData notificationData = new NotificationData(notificationId, senderUId, senderType, "message", senderUsername, "Photo", false, date.getTime());

                                                                        //storing data in database to show message notifications in NotificationActivity of the receiver...
                                                                        FirebaseDatabase.getInstance().getReference().child("Notifications").child(receiverUId).child(notificationId).setValue(notificationData);

                                                                        //send notification to the receiver...
                                                                        SendNotification sendNotification = new SendNotification(senderUsername, "Photo", userToken, senderUId, senderType, ChatActivity.this, "Chat");
                                                                        sendNotification.sendNotification();

                                                                        double timeStamp = date.getTime();

                                                                        //storing userdata in Chatlist to show the receiver in the chat list of the sender...
                                                                        ChatListData chatListData = new ChatListData(receiverUId, username, "Photo", receiverType, "img", timeStamp);
                                                                        database.getReference().child("ChatList").child(senderUId)
                                                                                .child(receiverUId).setValue(chatListData);

                                                                        //storing userdata in Chatlist to show the sender in the chat list of the receiver...
                                                                        chatListData = new ChatListData(senderUId, senderUsername, "Photo", senderType, "img", timeStamp);
                                                                        database.getReference().child("ChatList").child(receiverUId)
                                                                                .child(senderUId).setValue(chatListData);
                                                                    }
                                                                });
                                                    }
                                                });
                                        binding.messageInput.setText("");
                                    }
                                });
                            }
                        }
                    });
                    //reseting values after message sent to the receiver...
                    binding.messageInput.setText("");
                    binding.cancelChatImage.setVisibility(View.GONE);
                    binding.chatImage.setVisibility(View.GONE);
                    binding.chatImage.setImageResource(com.google.firebase.inappmessaging.display.R.drawable.image_placeholder);
                    filePath = null;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            //if user did'nt select any image for message...
            else {
                // initializing MessageData and model class to store data in database...
                MessageData messageData = new MessageData(randomKey, messageText, senderUId, "", receiverRoom, mTime, mDate, date.getTime(),false, "txt");

                //store data for sender...
                database.getReference().child("Chat")
                        .child(senderRoom)
                        .child("messages").child(randomKey)
                        .setValue(messageData).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                //store data for receiver...
                                database.getReference().child("Chat")
                                        .child(receiverRoom)
                                        .child("messages").child(randomKey)
                                        .setValue(messageData).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                //generating unique id to store data for notification...
                                                String notificationId = senderUId; //FirebaseDatabase.getInstance().getReference().push().getKey();

                                                //initialize model class and pass values to its constructor to store data ion database...
                                                NotificationData notificationData = new NotificationData(notificationId, senderUId, senderType, "message", senderUsername, messageText, false, date.getTime());

                                                //storing data in database to show message notifications in NotificationActivity of the receiver...
                                                FirebaseDatabase.getInstance().getReference().child("Notifications").child(receiverUId).child(notificationId).setValue(notificationData);



                                                //send notification to the receiver...
                                                SendNotification sendNotification = new SendNotification(senderUsername, messageText, userToken, senderUId, senderType, ChatActivity.this, "Chat");
                                                sendNotification.sendNotification();

                                                //sendMessageNotification(username, messageText, userToken);

                                                double timeStamp = date.getTime();

                                                //storing userdata in Chatlist to show the receiver in the chat list of the sender...
                                                ChatListData chatListData = new ChatListData(receiverUId, username, messageText, receiverType, "txt", timeStamp);
                                                database.getReference().child("ChatList").child(senderUId)
                                                        .child(receiverUId).setValue(chatListData);

                                                //storing userdata in Chatlist to show the sender in the chat list of the receiver...
                                                chatListData = new ChatListData(senderUId, senderUsername, messageText, senderType, "txt", timeStamp);
                                                database.getReference().child("ChatList").child(receiverUId)
                                                        .child(senderUId).setValue(chatListData);

                                                binding.preMessagesView.scrollToPosition(messages.size() - 1);

                                            }
                                        });
                            }
                        });
                //reseting values after message sent to the receiver...
                binding.messageInput.setText("");
            }
        }
    }


    //check is receiver see the messages or not...
    private void seenMessage(String userId){
        String myId= FirebaseAuth.getInstance().getCurrentUser().getUid();
        reference = FirebaseDatabase.getInstance().getReference("Chat").child(myId+userId).child("messages");
        reference2 = FirebaseDatabase.getInstance().getReference("Chat").child(userId+myId).child("messages");
        msgSeenListener1 = reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //update the value of message data if receiver has seen the message...
                for(DataSnapshot dataSnapshot: snapshot.getChildren()){
                    MessageData messageData = dataSnapshot.getValue(MessageData.class);
                    if(messageData.getSenderId().equals(myId)){

                    }else{
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("seen",true);
                        dataSnapshot.getRef().updateChildren(hashMap);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        msgSeenListener2 = reference2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //update the value of message data if receiver has seen the message...
                for(DataSnapshot dataSnapshot: snapshot.getChildren()){
                    MessageData messageData = dataSnapshot.getValue(MessageData.class);
                    if(messageData.getSenderId().equals(myId)){

                    }else{
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("seen",true);
                        dataSnapshot.getRef().updateChildren(hashMap);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void nextActivity(){
        //opening OutgoingCallActivity when audio or video call started...
        //intent = new Intent(ChatActivity.this, OutGoingCallActivity.class);
        bundle = new Bundle();
        bundle.putString("userId", receiverUId);
        bundle.putString("myId", senderUId);
        bundle.putString("profileUri", receiverProfileString);
        bundle.putString("username", username);
        bundle.putString("token", userToken);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (isFile){
            if (requestCode == REQUEST_PDF_FILE && resultCode == RESULT_OK) {
                // Get the URI of the selected audio file
                pdfFileUri = data.getData();
                binding.docFileName.setText(getFileNameFromUri(pdfFileUri));

                binding.fileIcon.setImageResource(R.drawable.baseline_picture_as_pdf_24);
                binding.recAudioButton.setVisibility(View.GONE);
                binding.selectFileButton.setVisibility(View.GONE);
                binding.messageInput.setVisibility(View.GONE);
                binding.imageMessage.setVisibility(View.GONE);
                binding.docLayout.setVisibility(View.VISIBLE);
                binding.sendFileButton.setVisibility(View.VISIBLE);

            }
        }
        else {
            if (requestCode == 1 && resultCode == RESULT_OK) {
                filePath = data.getData();
                try {
                    CropImage.activity(filePath)
                            .start(ChatActivity.this);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                if (resultCode == RESULT_OK) {
                    filePath = result.getUri();
                    try {
                        InputStream inputStream = getContentResolver().openInputStream(filePath);
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        binding.imageMessage.setVisibility(View.GONE);
                        binding.selectFileButton.setVisibility(View.GONE);
                        binding.recAudioButton.setVisibility(View.GONE);
                        binding.chatImage.setImageBitmap(bitmap);
                        binding.sendMessage.setVisibility(View.VISIBLE);
                        binding.chatImage.setVisibility(View.VISIBLE);
                        binding.cancelChatImage.setVisibility(View.VISIBLE);
                        isAudio = false;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                    Exception error = result.getError();
                }
            }
        }
    }

    private String getFileNameFromUri(Uri uri) {
        String fileName = null;
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
            if (nameIndex >= 0) {
                fileName = cursor.getString(nameIndex);
            }
        }
        if (cursor != null) {
            cursor.close();
        }
        return fileName;
    }


    @Override
    protected void onPause() {
        super.onPause();
        reference.removeEventListener(msgSeenListener1);
        reference2.removeEventListener(msgSeenListener2);
    }

}