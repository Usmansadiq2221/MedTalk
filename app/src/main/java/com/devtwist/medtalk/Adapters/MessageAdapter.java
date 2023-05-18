package com.devtwist.medtalk.Adapters;

import static android.content.Context.MODE_PRIVATE;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.devtwist.medtalk.Activities.ViewDocumentsActivity;
import com.devtwist.medtalk.Models.DoctorData;
import com.devtwist.medtalk.Models.MessageData;
import com.devtwist.medtalk.Models.PatientData;
import com.devtwist.medtalk.Models.StudentData;
import com.devtwist.medtalk.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;

public class MessageAdapter extends RecyclerView.Adapter {

    private Context context;
    private ArrayList<MessageData> messages;
    private final int item_sent = 1;
    private final int item_receive = 2;

    private MediaPlayer mediaPlayer;
    private Handler handler = new Handler();

    private CountDownTimer timer;
    private String myId,userId, receiverProfileString, senderType, receiverType, senderProfilePic = "";

    public MessageAdapter(Context context, ArrayList<MessageData> messages, String _myId, String userId, String receiverProfileString, String senderType, String receiverType) {
        this.context = context;
        this.messages = messages;
        this.myId = _myId;
        this.userId = userId;
        this.receiverProfileString = receiverProfileString;
        this.senderType = senderType;
        this.receiverType = receiverType;
    }

    public MessageAdapter() {
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if(viewType == item_sent){
            View view = LayoutInflater.from(context).inflate(R.layout.sent_message_item, parent, false);
            return new SentViewHolder(view);
        }else {
            View view = LayoutInflater.from(context).inflate(R.layout.receive_message_items, parent, false);
            return new ReceiverViewHolder(view);
        }
    }

    @Override
    public int getItemViewType(int position) {
        MessageData messageData = messages.get(position);
        if (myId.equals(messageData.getSenderId())) {
            return item_sent;
        } else {
            return item_receive;
        }
    }

    // Inside on bind view holder we will
    // set data to item of Recycler View...
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        MessageData messageData = messages.get(position);
        String  message = messageData.getMessageText();

        if (holder.getClass() == SentViewHolder.class) {

            //show the messages of the sender...
            SentViewHolder viewHolder = (SentViewHolder) holder;
            viewHolder._sentViewItem.setVisibility(View.GONE);
            viewHolder._sentMessageTime.setText(messageData.getTime());
            SharedPreferences accPreferences = context.getSharedPreferences("MedTalkData", MODE_PRIVATE);
            String accType = accPreferences.getString("accType", "");


            FirebaseDatabase.getInstance().getReference().child("Users").child(accType)
                    .child(myId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            if (senderType.equals("Doctor")) {
                                DoctorData doctorData = snapshot.getValue(DoctorData.class);
                                senderProfilePic = doctorData.getProfileUrl();
                            } else if (senderType.equals("Patient")) {
                                PatientData patientData = snapshot.getValue(PatientData.class);
                                senderProfilePic = patientData.getProfileUrl();
                            } else if (senderType.equals("Student")) {
                                StudentData studentData = snapshot.getValue(StudentData.class);
                                senderProfilePic = studentData.getProfileUrl();
                            }
                            if (senderProfilePic.length() > 1) {
                                Picasso.get().load(senderProfilePic).placeholder(R.drawable.profile_placeholder).into(viewHolder.senderProfilePic);
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

            if (messageData.getType().equals("txt") || messageData.getType().equals("img")) {
                viewHolder.sentMsgLayout.setVisibility(View.VISIBLE);
                if (messageData.getType().equals("img")) {
                    if (messageData.getFileUrl().length() > 1) {
                        Picasso.get().load(messageData.getFileUrl()).into(viewHolder._sendImage);
                    }
                    viewHolder._sendImage.setVisibility(ImageView.VISIBLE);
                    if (messageData.getMessageText().trim().length() < 1) {
                        viewHolder._sentMessage.setVisibility(TextView.GONE);
                    } else {
                        viewHolder._sentMessage.setText(messageData.getMessageText().toString().trim());
                    }

                    //allow the user to delete the items...
                    viewHolder._sendImage.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            viewHolder._sentViewItem.setVisibility(View.VISIBLE);
                            AlertDialog.Builder builder = new AlertDialog.Builder(context);
                            builder.setTitle("Delete message...");

                            String[] options = {"Delete for me", "Undo Message"};
                            builder.setItems(options, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int position) {
                                    if (position == 0) {
                                        FirebaseDatabase.getInstance().getReference().child("Chat").child(myId + userId)
                                                .child("messages").child(messageData.getMessageId()).removeValue();
                                    /*if (getItemCount()-1!=0){
                                        MessageData messageData1 = messages.get(getItemCount()-1);
                                        FirebaseDatabase.getInstance().getReference().child("ChatList").child(myId).child(userId).child("lastMessage").setValue(messageData1.getMessageText());
                                        FirebaseDatabase.getInstance().getReference().child("ChatList").child(myId).child(userId).child("timeStamp").setValue(messageData1.getTimestamp());
                                    }*/
                                    } else if (position == 1) {
                                        if (messageData.getFileUrl().length() > 1) {
                                            viewHolder._sImgDellProgress.setVisibility(View.VISIBLE);
                                            StorageReference deleteImg = FirebaseStorage.getInstance().getReferenceFromUrl(messageData.getFileUrl());
                                            deleteImg.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void unused) {
                                                    FirebaseDatabase.getInstance().getReference().child("Chat").child(myId + userId)
                                                            .child("messages").child(messageData.getMessageId()).removeValue();
                                                    FirebaseDatabase.getInstance().getReference().child("Chat").child(userId + myId)
                                                            .child("messages").child(messageData.getMessageId()).removeValue();
                                                    viewHolder._sImgDellProgress.setVisibility(View.GONE);
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Toast.makeText(context, "Network Error", Toast.LENGTH_SHORT).show();
                                                    viewHolder._sImgDellProgress.setVisibility(View.GONE);
                                                }
                                            });
                                        } else {
                                            FirebaseDatabase.getInstance().getReference().child("Chat").child(myId + userId)
                                                    .child("messages").child(messageData.getMessageId()).removeValue()
                                                    .addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            Toast.makeText(context, "Network Error", Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                            FirebaseDatabase.getInstance().getReference().child("Chat").child(userId + myId)
                                                    .child("messages").child(messageData.getMessageId()).removeValue().addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            Toast.makeText(context, "Network Error", Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                        }
                                    }
                                }
                            }).setOnCancelListener(new DialogInterface.OnCancelListener() {
                                @Override
                                public void onCancel(DialogInterface dialog) {
                                    viewHolder._sentViewItem.setVisibility(View.GONE);
                                }
                            });
                            // create and show the alert dialog
                            AlertDialog dialog = builder.create();
                            dialog.show();
                            return true;
                        }
                    });

                    //opening the ViewDocsActivity to show the image...
                    viewHolder._sendImage.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(v.getContext(), ViewDocumentsActivity.class);
                            Bundle bundle = new Bundle();
                            bundle.putString("tag", "i");
                            bundle.putString("imgUrl", messageData.getFileUrl());
                            intent.putExtras(bundle);
                            v.getContext().startActivity(intent);
                        }
                    });
                } else {
                    viewHolder._sentMessage.setText(message);
                }
            }
            else if (messageData.getType().equals("pdf")){
                viewHolder.sentFmLayout.setVisibility(View.VISIBLE);
                viewHolder._sentFileName.setText(messageData.getMessageText());
                viewHolder._sentFileTime.setText(messageData.getTime());
                viewHolder.sentFileIcon.setImageResource(R.drawable.baseline_picture_as_pdf_24);
                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setDataAndType(Uri.parse(messageData.getFileUrl()), "application/pdf");
                            intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                            context.startActivity(intent);
                        } catch (Exception e) {
                            Log.i("PdfViewerError", e.getMessage().toString());
                        }
                    }
                });
            }
            else if (messageData.getType().equals("aud")) {



                viewHolder.sentVmLayout.setVisibility(View.VISIBLE);
                viewHolder._sentAmTime.setText(messageData.getTime());
                viewHolder._sentAudioTime.setText("");
                viewHolder.sentPlayButton.setImageResource(R.drawable.baseline_play_arrow_24);
//                try {
//                    mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
//                    mediaPlayer.setDataSource(messageData.getImageUrl());
//                } catch (IllegalArgumentException e) {
//                    e.printStackTrace();
//                } catch (IllegalStateException e) {
//                    e.printStackTrace();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
                viewHolder.sentSeekBar.setMax(100);
                final boolean[] isPLayed = {false};
                final boolean[] isPrepared = {false};
                mediaPlayer = new MediaPlayer();

                viewHolder.sentPlayButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        isPLayed[0] = !isPLayed[0];

                        if (isPLayed[0]) {
                            if (!isPrepared[0]) {

                                viewHolder.sentPlayButton.setVisibility(View.GONE);
                                viewHolder.sentPlayProgress.setVisibility(View.VISIBLE);
                                isPrepared[0] = true;
                                try {

                                    mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                                    mediaPlayer.setDataSource(messageData.getFileUrl());
                                    //mediaPlayer.seekTo();
                                } catch (IllegalArgumentException e) {
                                    e.printStackTrace();
                                } catch (IllegalStateException e) {
                                    e.printStackTrace();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                try {
                                    mediaPlayer.prepare();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                            viewHolder.sentSeekBar.setOnTouchListener(new View.OnTouchListener() {
                                @Override
                                public boolean onTouch(View v, MotionEvent event) {
                                    SeekBar seekBar = (SeekBar) v;
                                    int playPosition = (mediaPlayer.getDuration()/100) * seekBar.getProgress();
                                    mediaPlayer.seekTo(playPosition);
                                    return false;
                                }
                            });
                            mediaPlayer.start();
                            viewHolder.sentPlayButton.setImageResource(R.drawable.baseline_pause_24);
                            if (mediaPlayer.isPlaying()){
                                viewHolder.sentPlayProgress.setVisibility(View.GONE);
                                viewHolder.sentPlayButton.setVisibility(View.VISIBLE);
                                timer = new CountDownTimer(60000,50) {
                                    @Override
                                    public void onTick(long millisUntilFinished) {
                                        viewHolder.sentSeekBar.setProgress((int) (((float) mediaPlayer.getCurrentPosition() / mediaPlayer.getDuration()) * 100));
                                    }

                                    @Override
                                    public void onFinish() {

                                    }
                                };
                                timer.start();
                            }
                            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                @Override
                                public void onCompletion(MediaPlayer mp) {
                                    timer.cancel();
                                    viewHolder.sentPlayButton.setImageResource(R.drawable.baseline_play_arrow_24);
                                    //mp.release();
                                    //mediaPlayer = null;
                                    isPLayed[0] = !isPLayed[0];
                                    //isPrepared[0] = isPrepared[0];
                                }
                            });
                        }
                        else {
                            viewHolder.sentPlayButton.setImageResource(R.drawable.baseline_play_arrow_24);
                            timer.cancel();
                            mediaPlayer.pause();
                            //mediaPlayer.release();
                            //mediaPlayer = null;
                            //isPrepared[0] = !isPrepared[0];
                        }
                    }
                });





            }

            //allow the user to delete the message...
            viewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    viewHolder._sentViewItem.setVisibility(View.VISIBLE);
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("Delete message...");

                    String[] options = {"Delete for me", "Undo Message"};
                    builder.setItems(options, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int position) {
                            if (position == 0){

                                FirebaseDatabase.getInstance().getReference().child("Chat").child(myId+userId)
                                        .child("messages").child(messageData.getMessageId()).removeValue();
                            }
                            else if (position == 1){
                                if (messageData.getFileUrl().length()>1) {

                                    if (messageData.getType().equals("img")) {
                                        viewHolder._sImgDellProgress.setVisibility(View.VISIBLE);
                                    } else if (messageData.getType().equals("aud")) {
                                        viewHolder.sentPlayButton.setVisibility(View.GONE);
                                        viewHolder.sentPlayProgress.setVisibility(View.VISIBLE);
                                    } else if (messageData.getType().equals("pdf")) {
                                        viewHolder.sentFileIcon.setVisibility(View.GONE);
                                        viewHolder.sentFileProgress.setVisibility(View.VISIBLE);
                                    }

                                    StorageReference deleteImg = FirebaseStorage.getInstance().getReferenceFromUrl(messageData.getFileUrl());
                                    deleteImg.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            FirebaseDatabase.getInstance().getReference().child("Chat").child(myId+userId)
                                                    .child("messages").child(messageData.getMessageId()).removeValue();
                                            FirebaseDatabase.getInstance().getReference().child("Chat").child(userId+myId)
                                                    .child("messages").child(messageData.getMessageId()).removeValue();
                                            viewHolder._sImgDellProgress.setVisibility(View.GONE);
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(context, "Network Error", Toast.LENGTH_SHORT).show();
                                            viewHolder._sImgDellProgress.setVisibility(View.GONE);
                                        }
                                    });
                                }else{
                                    FirebaseDatabase.getInstance().getReference().child("Chat").child(myId + userId)
                                            .child("messages").child(messageData.getMessageId()).removeValue()
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Toast.makeText(context, "Network Error", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                    FirebaseDatabase.getInstance().getReference().child("Chat").child(userId+myId)
                                            .child("messages").child(messageData.getMessageId()).removeValue().addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Toast.makeText(context, "Network Error", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                }
                            }

                        }
                    }).setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            viewHolder._sentViewItem.setVisibility(View.GONE);
                        }
                    });
                    // create and show the alert dialog
                    AlertDialog dialog = builder.create();
                    dialog.show();
                    return true;
                }
            });

            if (position == messages.size()-1){
                if (messageData.isSeen()){
                    viewHolder._textSeen.setText("Seen");
                }
                else{
                    viewHolder._textSeen.setText("Delivered");
                }
                viewHolder._textSeen.setVisibility(View.VISIBLE);
            }else{
                viewHolder._textSeen.setVisibility(View.GONE);
            }


        }
        else {

            //show the message of the receiver...
            ReceiverViewHolder viewHolder = (ReceiverViewHolder)  holder;
            viewHolder._receiveViewItem.setVisibility(View.GONE);
            viewHolder._receiveMessageTime.setText(messageData.getTime());

            if (receiverProfileString.length()>1){
                Picasso.get().load(receiverProfileString).placeholder(R.drawable.profile_placeholder).into(viewHolder.receiverProfilePic);
            }

            if (messageData.getType().equals("img") || messageData.getType().equals("txt")) {
                viewHolder.rcvMsgLayout.setVisibility(View.VISIBLE);
                if (messageData.getType().equals("img")) {
                    if (messageData.getFileUrl().length() > 1) {
                        Picasso.get().load(messageData.getFileUrl()).into(viewHolder._receiveImage);
                    }
                    viewHolder._receiveImage.setVisibility(ImageView.VISIBLE);
                    if (messageData.getMessageText().trim().length() < 1) {
                        viewHolder._receiveMessage.setVisibility(TextView.GONE);
                    } else {
                        viewHolder._receiveMessage.setText(messageData.getMessageText().toString().trim());
                    }
                    viewHolder._receiveImage.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            viewHolder._receiveViewItem.setVisibility(View.VISIBLE);
                            AlertDialog.Builder builder = new AlertDialog.Builder(context);
                            builder.setTitle("Delete message...");

                            String[] options = {"Delete for me"};
                            builder.setItems(options, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int position) {
                                    if (position == 0) {
                                        FirebaseDatabase.getInstance().getReference().child("Chat").child(myId + userId)
                                                .child("messages").child(messageData.getMessageId()).removeValue();
                                    }
                                }
                            }).setOnCancelListener(new DialogInterface.OnCancelListener() {
                                @Override
                                public void onCancel(DialogInterface dialog) {
                                    viewHolder._receiveViewItem.setVisibility(View.GONE);
                                }
                            });
                            // create and show the alert dialog
                            AlertDialog dialog = builder.create();
                            dialog.setCancelable(true);
                            dialog.setIcon(R.drawable.app_logo);
                            dialog.show();
                            return false;
                        }
                    });
                    viewHolder._receiveImage.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(v.getContext(), ViewDocumentsActivity.class);
                            Bundle bundle = new Bundle();
                            bundle.putString("tag", "i");
                            bundle.putString("imgUrl", messageData.getFileUrl());
                            intent.putExtras(bundle);
                            v.getContext().startActivity(intent);
                        }
                    });

                }
                else {
                    viewHolder._receiveMessage.setText(message);
                }
            }
            else if (messageData.getType().equals("pdf")){
                viewHolder.rcvFmLayout.setVisibility(View.VISIBLE);
                viewHolder._rcvFileName.setText(messageData.getMessageText());
                viewHolder._rcvFileTime.setText(messageData.getTime());
                viewHolder.rcvFileIcon.setImageResource(R.drawable.baseline_picture_as_pdf_24);;
                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setDataAndType(Uri.parse(messageData.getFileUrl()), "application/pdf");
                            intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                            context.startActivity(intent);
                        } catch (Exception e) {
                            Log.i("PdfViewerError", e.getMessage().toString());
                        }
                    }
                });
            }
            else if (messageData.getType().equals("aud")){
                viewHolder.rcvVmLayout.setVisibility(View.VISIBLE);
                viewHolder._rcvAmTime.setText(messageData.getTime());
                viewHolder._rcvAudioTime.setText(messageData.getMessageText());
                viewHolder.rcvPlayButton.setImageResource(R.drawable.rcv_play_arrow_24);

                viewHolder.rcvSeekBar.setMax(100);
                final boolean[] isPLayed = {false};
                final boolean[] isPrepared = {false};
                mediaPlayer = new MediaPlayer();

                viewHolder.rcvPlayButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        isPLayed[0] = !isPLayed[0];

                        if (isPLayed[0]) {
                            if (!isPrepared[0]) {

                                viewHolder.rcvPlayButton.setVisibility(View.GONE);
                                viewHolder.rcvPlayProgress.setVisibility(View.VISIBLE);
                                isPrepared[0] = true;
                                try {

                                    mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                                    mediaPlayer.setDataSource(messageData.getFileUrl());
                                    //mediaPlayer.seekTo();
                                } catch (IllegalArgumentException e) {
                                    e.printStackTrace();
                                } catch (IllegalStateException e) {
                                    e.printStackTrace();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                try {
                                    mediaPlayer.prepare();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                            viewHolder.rcvSeekBar.setOnTouchListener(new View.OnTouchListener() {
                                @Override
                                public boolean onTouch(View v, MotionEvent event) {
                                    SeekBar seekBar = (SeekBar) v;
                                    int playPosition = (mediaPlayer.getDuration()/100) * seekBar.getProgress();
                                    mediaPlayer.seekTo(playPosition);
                                    return false;
                                }
                            });
                            mediaPlayer.start();
                            viewHolder.rcvPlayButton.setImageResource(R.drawable.rcv_pause_24);
                            if (mediaPlayer.isPlaying()){
                                viewHolder.rcvPlayProgress.setVisibility(View.GONE);
                                viewHolder.rcvPlayButton.setVisibility(View.VISIBLE);
                                timer = new CountDownTimer(60000,50) {
                                    @Override
                                    public void onTick(long millisUntilFinished) {
                                        viewHolder.rcvSeekBar.setProgress((int) (((float) mediaPlayer.getCurrentPosition() / mediaPlayer.getDuration()) * 100));
                                    }

                                    @Override
                                    public void onFinish() {

                                    }
                                };
                                timer.start();
                            }
                            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                @Override
                                public void onCompletion(MediaPlayer mp) {
                                    timer.cancel();
                                    viewHolder.rcvPlayButton.setImageResource(R.drawable.rcv_play_arrow_24);
                                    //mp.release();
                                    //mediaPlayer = null;
                                    isPLayed[0] = !isPLayed[0];
                                    //isPrepared[0] = isPrepared[0];
                                }
                            });
                        }
                        else {
                            viewHolder.rcvPlayButton.setImageResource(R.drawable.rcv_play_arrow_24);
                            timer.cancel();
                            mediaPlayer.pause();
                            //mediaPlayer.release();
                            //mediaPlayer = null;
                            //isPrepared[0] = !isPrepared[0];
                        }
                    }
                });

            }
            viewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    viewHolder._receiveViewItem.setVisibility(View.VISIBLE);
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("Delete message...");

                    String[] options = {"Delete for me"};
                    builder.setItems(options, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int position) {
                            if (position == 0){

                                FirebaseDatabase.getInstance().getReference().child("Chat").child(myId + userId)
                                        .child("messages").child(messageData.getMessageId()).removeValue();
                            }
                        }
                    }).setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            viewHolder._receiveViewItem.setVisibility(View.GONE);
                        }
                    });
                    // create and show the alert dialog
                    AlertDialog dialog = builder.create();
                    dialog.setCancelable(true);
                    dialog.setIcon(R.drawable.app_logo);
                    dialog.show();
                    return false;
                }
            });



            if (position == messages.size()-1){
                if (messageData.isSeen()){
                    viewHolder._isSeen.setText("Seen");
                }
                else{
                    viewHolder._isSeen.setText("Delivered");
                }
                viewHolder._isSeen.setVisibility(View.VISIBLE);
            }else{
                viewHolder._isSeen.setVisibility(View.GONE);
            }

        }
        Log.i("Chat Adapter Error", myId);
        holder.setIsRecyclable(false);

    }


    // this method will return the count of our list...
    @Override
    public int getItemCount() {
        return messages.size();
    }

    public class SentViewHolder extends RecyclerView.ViewHolder {

        // Adapter class for initializing
        // the views of our sent_message_item view...

        private TextView _sentMessage, _sentMessageTime, _textSeen, _sentFileName, _sentFileTime, _sentAudioTime, _sentAmTime;
        private ImageView _sendImage, senderProfilePic, sentFileIcon, sentPlayButton;
        private View _sentViewItem;
        private ProgressBar _sImgDellProgress, sentPlayProgress, sentFileProgress;

        private SeekBar sentSeekBar;
        private LinearLayout sentMsgLayout, sentFmLayout, sentVmLayout;
        public SentViewHolder(@NonNull View itemView) {
            super(itemView);
            _sentMessage = (TextView) itemView.findViewById(R.id._sentMessage);
            _sentMessageTime = (TextView) itemView.findViewById(R.id._sentMessageTime);
            _sendImage = (ImageView) itemView.findViewById(R.id._sendImage);
            _textSeen = (TextView) itemView.findViewById(R.id._textSeen);
            _sentViewItem = (View) itemView.findViewById(R.id._sentViewItem);
            _sImgDellProgress = (ProgressBar) itemView.findViewById(R.id._sImgDellProgress);
            senderProfilePic = (ImageView) itemView.findViewById(R.id.senderProfilePic);
            _sentFileName = (TextView) itemView.findViewById(R.id._sentFileName);
            _sentFileTime = (TextView) itemView.findViewById(R.id._sentFileTime);
            _sentAudioTime = (TextView) itemView.findViewById(R.id._sentAudioTime);
            _sentAmTime = (TextView) itemView.findViewById(R.id._sentAmTime);
            sentFileIcon = (ImageView) itemView.findViewById(R.id.sentFileIcon);
            sentPlayButton = (ImageView) itemView.findViewById(R.id.sentPlayButton);
            sentMsgLayout = (LinearLayout) itemView.findViewById(R.id.sentMsgLayout);
            sentFmLayout = (LinearLayout) itemView.findViewById(R.id.sentFmLayout);
            sentVmLayout = (LinearLayout) itemView.findViewById(R.id.sentVmLayout);
            sentSeekBar = (SeekBar) itemView.findViewById(R.id.sentSeekBar);
            sentPlayProgress = (ProgressBar) itemView.findViewById(R.id.sentPlayProgress);
            sentFileProgress = (ProgressBar) itemView.findViewById(R.id.sentFileProgress);

        }
    }

    public class ReceiverViewHolder extends RecyclerView.ViewHolder {

        // Adapter class for initializing
        // the views of our receive_message_items view...

        private TextView _receiveMessage, _receiveMessageTime, _isSeen, _rcvFileName, _rcvFileTime, _rcvAudioTime, _rcvAmTime;
        private ImageView _receiveImage, receiverProfilePic, rcvFileIcon, rcvPlayButton;
        private View _receiveViewItem;
        private SeekBar rcvSeekBar;
        private ProgressBar _rImgDellProgress, rcvPlayProgress, rcvFileProgress;
        private LinearLayout rcvMsgLayout, rcvFmLayout, rcvVmLayout;

        public ReceiverViewHolder(@NonNull View itemView) {
            super(itemView);
            _receiveMessage = (TextView) itemView.findViewById(R.id._receiveMessage);
            _receiveMessageTime = (TextView) itemView.findViewById(R.id._receiveMessageTime);
            _isSeen = (TextView) itemView.findViewById(R.id._isSeen);
            _receiveImage = (ImageView) itemView.findViewById(R.id._receiveImage);
            _receiveViewItem = (View) itemView.findViewById(R.id._receiveViewItem);
            _rImgDellProgress = (ProgressBar) itemView.findViewById(R.id._rImgDellProgress);
            receiverProfilePic = (ImageView) itemView.findViewById(R.id.receiverProfilePic);
            _rcvFileName = (TextView) itemView.findViewById(R.id._rcvFileName);
            _rcvFileTime = (TextView) itemView.findViewById(R.id._rcvFileTime);
            _rcvAudioTime = (TextView) itemView.findViewById(R.id._rcvAudioTime);
            _rcvAmTime = (TextView) itemView.findViewById(R.id._rcvAmTime);
            rcvFileIcon = (ImageView) itemView.findViewById(R.id.rcvFileIcon);
            rcvPlayButton = (ImageView) itemView.findViewById(R.id.rcvPlayButton);
            rcvSeekBar = (SeekBar) itemView.findViewById(R.id.rcvSeekBar);
            rcvMsgLayout = (LinearLayout) itemView.findViewById(R.id.rcvMsgLayout);
            rcvFmLayout = (LinearLayout) itemView.findViewById(R.id.rcvFmLayout);
            rcvVmLayout = (LinearLayout) itemView.findViewById(R.id.rcvVmLayout);
            rcvFileProgress = (ProgressBar) itemView.findViewById(R.id.rcvFileProgress);
            rcvPlayProgress = (ProgressBar) itemView.findViewById(R.id.rcvPlayProgress);

        }
    }

}
