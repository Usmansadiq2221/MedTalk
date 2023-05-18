package com.devtwist.medtalk.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.devtwist.medtalk.Activities.ChatActivity;
import com.devtwist.medtalk.Models.ChatListData;
import com.devtwist.medtalk.Models.DoctorData;
import com.devtwist.medtalk.Models.MessageData;
import com.devtwist.medtalk.Models.PatientData;
import com.devtwist.medtalk.Models.StudentData;
import com.devtwist.medtalk.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ChatListViewHolder>{

    //for storing context of the activity passed from Chat Fragment...
    private Context context;

    //used to open the ChatActivity...
    private Intent intent;

    //used to pass values to the ChatActivity...
    private Bundle bundle;

    //for storing the list of chats data...
    private ArrayList<ChatListData> chatListUser;

    //for storing the ids of the current users...
    private String myId;
    private boolean isOpened;
    private CountDownTimer timer;
    private float tVal;

    //initializing the variables and list in constructor passed from ChatFragment...
    public ChatListAdapter(Context context, ArrayList<ChatListData> chatListUser, String myId) {
        this.context = context;
        this.chatListUser = chatListUser;
        this.myId = myId;
        isOpened = false;
        tVal = 100;

    }

    // We are inflating the chat_list_items
    // inside on Create View Holder method...
    @NonNull
    @Override
    public ChatListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_list_items, parent, false);
        return new ChatListViewHolder(view);
    }

    // Inside on bind view holder we will
    // set data to item of Recycler View...
    @Override
    public void onBindViewHolder(@NonNull ChatListViewHolder holder, int position) {
        ChatListData model = chatListUser.get(position);
        String userId = model.getUserId();
        Log.i("UserId", userId);
        holder._userChatLayout.setTranslationX(0);
        holder._chatDeleteItem.setVisibility(View.GONE);
        if (model.getMsgType().equals("img")){
            holder._lMImageItem.setVisibility(View.VISIBLE);
            holder._lMImageItem.setImageResource(R.drawable.baseline_photo_24);
            holder._lastMessageItem.setText("Photo");
        }
        else if (model.getMsgType().equals("txt")){
            holder._lMImageItem.setVisibility(View.GONE);
            holder._lastMessageItem.setText(model.getLastMessage());
        }
        else if (model.getMsgType().equals("pdf")) {
            holder._lMImageItem.setImageResource(R.drawable.baseline_picture_as_pdf_24);
            holder._lMImageItem.setVisibility(View.VISIBLE);
            holder._lastMessageItem.setText("Pdf File");
        }
        else if (model.getMsgType().equals("aud")) {
            holder._lMImageItem.setImageResource(R.drawable.baseline_mic_24_green);
            holder._lMImageItem.setVisibility(View.VISIBLE);
            holder._lastMessageItem.setText("Voice");
        }
//        FirebaseDatabase.getInstance().getReference().child("Users")
//                .child(userId).addValueEventListener(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(@NonNull DataSnapshot snapshot) {
//                        Userdata userdata = snapshot.getValue(Userdata.class);
//                        if (userdata.getProfileUrl().length()>1) {
//                            Picasso.get().load(userdata.getProfileUrl()).placeholder(R.drawable.enter_profile_icon).into(holder._chatListProfileItem);
//                        }
//                        holder._chatListUsernameItem.setText(userdata.getUsername());
//                        holder.itemView.setOnClickListener(new View.OnClickListener() {
//                            @Override
//                            public void onClick(View v) {
//                                intent = new Intent(v.getContext(), ChatActivity.class);
//                                bundle = new Bundle();
//                                bundle.putString("myId", myId);
//                                bundle.putString("userId", userdata.getUserId());
//                                bundle.putString("username", userdata.getUsername());
//                                bundle.putString("profileUri", userdata.getProfileUrl());
//                                bundle.putString("token", userdata.getToken());
//                                intent.putExtras(bundle);
//                                v.getContext().startActivity(intent);
//                            }
//                        });
//                        FirebaseDatabase.getInstance().getReference("Chat")
//                                .child(model.getUserId()+myId).child("messages")
//                                .addValueEventListener(new ValueEventListener() {
//                                    @Override
//                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
//                                        for(DataSnapshot dataSnapshot: snapshot.getChildren()){
//                                            MessageData messageData = dataSnapshot.getValue(MessageData.class);
//                                            if(messageData.getSenderId().equals(model.getUserId())){
//                                                holder._viewed.setVisibility(View.VISIBLE);
//                                                if (messageData.isSeen()){
//                                                    holder._viewed.setVisibility(View.GONE);
//                                                }else{
//                                                    holder._viewed.setVisibility(View.VISIBLE);
//                                                }
//                                            }
//                                        }
//                                    }
//
//                                    @Override
//                                    public void onCancelled(@NonNull DatabaseError error) {
//
//                                    }
//                                });
//
//
//                        isOpened = false;
//                        tVal = 100;
//
//                        //allow the user to delete the current item when user long press the itemview...
//                        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
//                            @Override
//                            public boolean onLongClick(View v) {
//                                if (!isOpened) {
//                                    isOpened = true;
//                                    holder._userChatLayout.animate().translationXBy(-125).setDuration(500);
//                                    new CountDownTimer(500, 500) {
//                                        @Override
//                                        public void onTick(long millisUntilFinished) {
//
//                                        }
//
//                                        @Override
//                                        public void onFinish() {
//                                            holder._chatDeleteItem.setVisibility(View.VISIBLE);
//                                        }
//                                    }.start();
//
//                                    timer = new CountDownTimer(4000, 1000) {
//                                        @Override
//                                        public void onTick(long millisUntilFinished) {
//
//                                        }
//
//                                        @Override
//                                        public void onFinish() {
//                                            holder._chatDeleteItem.setVisibility(View.GONE);
//                                            tVal = 0;
//                                            holder._userChatLayout.animate().translationXBy(125).setDuration(500);
//                                            isOpened = false;
//                                        }
//                                    };
//                                    timer.start();
//
//                                    //used to dele the data of selected chat...
//                                    holder._chatDeleteItem.setOnClickListener(new View.OnClickListener() {
//                                        @Override
//                                        public void onClick(View v) {
//                                            new AlertDialog.Builder(context)
//                                                    .setTitle("Delete this chat")
//                                                    .setIcon(R.drawable.app_icon)
//                                                    .setMessage("Are you sure you want to delete this chat?")
//
//                                                    // Specifying a listener allows you to take an action before dismissing the dialog.
//                                                    // The dialog is automatically dismissed when a dialog button is clicked.
//                                                    .setPositiveButton("Delete Chat", new DialogInterface.OnClickListener() {
//                                                        public void onClick(DialogInterface dialog, int which) {
//                                                            // Continue with delete operation
//                                                            new CountDownTimer(500, 500) {
//                                                                @Override
//                                                                public void onTick(long millisUntilFinished) {
//                                                                    holder._chatDeleteItem.setVisibility(View.GONE);
//                                                                    holder._userChatLayout.animate().translationXBy(2000).setDuration(500);
//                                                                }
//
//                                                                @Override
//                                                                public void onFinish() {
//                                                                    FirebaseDatabase.getInstance().getReference().child("ChatList").child(myId)
//                                                                            .child(userId).removeValue();
//                                                                    FirebaseDatabase.getInstance().getReference().child("Chat")
//                                                                            .child(myId + userId).removeValue();
//                                                                }
//                                                            }.start();
//
//
//                                                            isOpened = false;
//
//                                                        }
//                                                    }).setOnCancelListener(new DialogInterface.OnCancelListener() {
//                                                        @Override
//                                                        public void onCancel(DialogInterface dialog) {
//                                                            isOpened = false;
//                                                        }
//                                                    }).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
//                                                        @Override
//                                                        public void onClick(DialogInterface dialog, int which) {
//                                                            isOpened = false;
//                                                        }
//                                                    }).setCancelable(true)
//                                                    .show();
//                                        }
//                                    });
//                                }
//                                return true;
//                            }
//                        });
//
//                    }
//
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError error) {
//
//                    }
//                });

        if (model.getUserType().equals("Doctor")){
            FirebaseDatabase.getInstance().getReference().child("Users").child("Doctor")
                    .child(userId).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            DoctorData userdata = snapshot.getValue(DoctorData.class);
                            if (userdata.getProfileUrl().length()>1) {
                                Picasso.get().load(userdata.getProfileUrl()).placeholder(R.drawable.enter_profile_icon).into(holder._chatListProfileItem);
                            }
                            holder._chatListUsernameItem.setText(userdata.getUsername());
                            holder.itemView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    intent = new Intent(v.getContext(), ChatActivity.class);
                                    bundle = new Bundle();
                                    bundle.putString("myId", myId);
                                    bundle.putString("userId", userdata.getUserId());
                                    bundle.putString("username", userdata.getUsername());
                                    bundle.putString("profileUri", userdata.getProfileUrl());
                                    bundle.putString("token", userdata.getToken());
                                    bundle.putString("rType", model.getUserType());
                                    intent.putExtras(bundle);
                                    v.getContext().startActivity(intent);
                                }
                            });
                            FirebaseDatabase.getInstance().getReference("Chat")
                                    .child(model.getUserId()+myId).child("messages")
                                    .addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            for(DataSnapshot dataSnapshot: snapshot.getChildren()){
                                                MessageData messageData = dataSnapshot.getValue(MessageData.class);
                                                if(messageData.getSenderId().equals(model.getUserId())){
                                                    holder._viewed.setVisibility(View.VISIBLE);
                                                    if (messageData.isSeen()){
                                                        holder._viewed.setVisibility(View.GONE);
                                                    }else{
                                                        holder._viewed.setVisibility(View.VISIBLE);
                                                    }
                                                }
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });


                            isOpened = false;
                            tVal = 100;

                            //allow the user to delete the current item when user long press the itemview...
                            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                                @Override
                                public boolean onLongClick(View v) {
                                    if (!isOpened) {
                                        isOpened = true;
                                        holder._userChatLayout.animate().translationXBy(-120).setDuration(500);
                                        new CountDownTimer(500, 500) {
                                            @Override
                                            public void onTick(long millisUntilFinished) {

                                            }

                                            @Override
                                            public void onFinish() {
                                                holder._chatDeleteItem.setVisibility(View.VISIBLE);
                                            }
                                        }.start();

                                        timer = new CountDownTimer(4000, 1000) {
                                            @Override
                                            public void onTick(long millisUntilFinished) {

                                            }

                                            @Override
                                            public void onFinish() {
                                                holder._chatDeleteItem.setVisibility(View.GONE);
                                                tVal = 0;
                                                holder._userChatLayout.animate().translationXBy(120).setDuration(500);
                                                isOpened = false;
                                            }
                                        };
                                        timer.start();

                                        //used to dele the data of selected chat...
                                        holder._chatDeleteItem.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                new AlertDialog.Builder(context)
                                                        .setTitle("Delete this chat")
                                                        .setIcon(R.drawable.app_logo)
                                                        .setMessage("Are you sure you want to delete this chat?")

                                                        // Specifying a listener allows you to take an action before dismissing the dialog.
                                                        // The dialog is automatically dismissed when a dialog button is clicked.
                                                        .setPositiveButton("Delete Chat", new DialogInterface.OnClickListener() {
                                                            public void onClick(DialogInterface dialog, int which) {
                                                                // Continue with delete operation
                                                                new CountDownTimer(500, 500) {
                                                                    @Override
                                                                    public void onTick(long millisUntilFinished) {
                                                                        holder._chatDeleteItem.setVisibility(View.GONE);
                                                                        holder._userChatLayout.animate().translationXBy(2000).setDuration(500);
                                                                    }

                                                                    @Override
                                                                    public void onFinish() {
                                                                        FirebaseDatabase.getInstance().getReference().child("ChatList").child(myId)
                                                                                .child(userId).removeValue();
                                                                        FirebaseDatabase.getInstance().getReference().child("Chat")
                                                                                .child(myId + userId).removeValue();
                                                                    }
                                                                }.start();


                                                                isOpened = false;

                                                            }
                                                        }).setOnCancelListener(new DialogInterface.OnCancelListener() {
                                                            @Override
                                                            public void onCancel(DialogInterface dialog) {
                                                                isOpened = false;
                                                            }
                                                        }).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialog, int which) {
                                                                isOpened = false;
                                                            }
                                                        }).setCancelable(true)
                                                        .show();
                                            }
                                        });
                                    }
                                    return true;
                                }
                            });

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
        }
        else if (model.getUserType().equals("Patient")){
            FirebaseDatabase.getInstance().getReference().child("Users").child("Patient")
                    .child(userId).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            PatientData userdata = snapshot.getValue(PatientData.class);
                            if (userdata.getProfileUrl().length()>1) {
                                Picasso.get().load(userdata.getProfileUrl()).placeholder(R.drawable.enter_profile_icon).into(holder._chatListProfileItem);
                            }
                            holder._chatListUsernameItem.setText(userdata.getUsername());
                            holder.itemView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    intent = new Intent(v.getContext(), ChatActivity.class);
                                    bundle = new Bundle();
                                    bundle.putString("myId", myId);
                                    bundle.putString("userId", userdata.getUserId());
                                    bundle.putString("username", userdata.getUsername());
                                    bundle.putString("profileUri", userdata.getProfileUrl());
                                    bundle.putString("token", userdata.getToken());
                                    bundle.putString("rType", model.getUserType());
                                    intent.putExtras(bundle);
                                    v.getContext().startActivity(intent);
                                }
                            });
                            FirebaseDatabase.getInstance().getReference("Chat")
                                    .child(model.getUserId()+myId).child("messages")
                                    .addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            for(DataSnapshot dataSnapshot: snapshot.getChildren()){
                                                MessageData messageData = dataSnapshot.getValue(MessageData.class);
                                                if(messageData.getSenderId().equals(model.getUserId())){
                                                    holder._viewed.setVisibility(View.VISIBLE);
                                                    if (messageData.isSeen()){
                                                        holder._viewed.setVisibility(View.GONE);
                                                    }else{
                                                        holder._viewed.setVisibility(View.VISIBLE);
                                                    }
                                                }
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });


                            isOpened = false;
                            tVal = 100;

                            //allow the user to delete the current item when user long press the itemview...
                            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                                @Override
                                public boolean onLongClick(View v) {
                                    if (!isOpened) {
                                        isOpened = true;
                                        holder._userChatLayout.animate().translationXBy(-120).setDuration(500);
                                        new CountDownTimer(500, 500) {
                                            @Override
                                            public void onTick(long millisUntilFinished) {

                                            }

                                            @Override
                                            public void onFinish() {
                                                holder._chatDeleteItem.setVisibility(View.VISIBLE);
                                            }
                                        }.start();

                                        timer = new CountDownTimer(4000, 1000) {
                                            @Override
                                            public void onTick(long millisUntilFinished) {

                                            }

                                            @Override
                                            public void onFinish() {
                                                holder._chatDeleteItem.setVisibility(View.GONE);
                                                tVal = 0;
                                                holder._userChatLayout.animate().translationXBy(120).setDuration(500);
                                                isOpened = false;
                                            }
                                        };
                                        timer.start();

                                        //used to dele the data of selected chat...
                                        holder._chatDeleteItem.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                new AlertDialog.Builder(context)
                                                        .setTitle("Delete this chat")
                                                        .setIcon(R.drawable.app_logo)
                                                        .setMessage("Are you sure you want to delete this chat?")

                                                        // Specifying a listener allows you to take an action before dismissing the dialog.
                                                        // The dialog is automatically dismissed when a dialog button is clicked.
                                                        .setPositiveButton("Delete Chat", new DialogInterface.OnClickListener() {
                                                            public void onClick(DialogInterface dialog, int which) {
                                                                // Continue with delete operation
                                                                new CountDownTimer(500, 500) {
                                                                    @Override
                                                                    public void onTick(long millisUntilFinished) {
                                                                        holder._chatDeleteItem.setVisibility(View.GONE);
                                                                        holder._userChatLayout.animate().translationXBy(2000).setDuration(500);
                                                                    }

                                                                    @Override
                                                                    public void onFinish() {
                                                                        FirebaseDatabase.getInstance().getReference().child("ChatList").child(myId)
                                                                                .child(userId).removeValue();
                                                                        FirebaseDatabase.getInstance().getReference().child("Chat")
                                                                                .child(myId + userId).removeValue();
                                                                    }
                                                                }.start();


                                                                isOpened = false;

                                                            }
                                                        }).setOnCancelListener(new DialogInterface.OnCancelListener() {
                                                            @Override
                                                            public void onCancel(DialogInterface dialog) {
                                                                isOpened = false;
                                                            }
                                                        }).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialog, int which) {
                                                                isOpened = false;
                                                            }
                                                        }).setCancelable(true)
                                                        .show();
                                            }
                                        });
                                    }
                                    return true;
                                }
                            });

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
        }
        else if (model.getUserType().equals("Student")) {
            FirebaseDatabase.getInstance().getReference().child("Users")
                    .child(userId).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            StudentData userdata = snapshot.getValue(StudentData.class);
                            if (userdata.getProfileUrl().length()>1) {
                                Picasso.get().load(userdata.getProfileUrl()).placeholder(R.drawable.enter_profile_icon).into(holder._chatListProfileItem);
                            }
                            holder._chatListUsernameItem.setText(userdata.getUsername());
                            holder.itemView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    intent = new Intent(v.getContext(), ChatActivity.class);
                                    bundle = new Bundle();
                                    bundle.putString("myId", myId);
                                    bundle.putString("userId", userdata.getUserId());
                                    bundle.putString("username", userdata.getUsername());
                                    bundle.putString("profileUri", userdata.getProfileUrl());
                                    bundle.putString("token", userdata.getToken());
                                    bundle.putString("rType", model.getUserType());
                                    intent.putExtras(bundle);
                                    v.getContext().startActivity(intent);
                                }
                            });
                            FirebaseDatabase.getInstance().getReference("Chat")
                                    .child(model.getUserId()+myId).child("messages")
                                    .addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            for(DataSnapshot dataSnapshot: snapshot.getChildren()){
                                                MessageData messageData = dataSnapshot.getValue(MessageData.class);
                                                if(messageData.getSenderId().equals(model.getUserId())){
                                                    holder._viewed.setVisibility(View.VISIBLE);
                                                    if (messageData.isSeen()){
                                                        holder._viewed.setVisibility(View.GONE);
                                                    }else{
                                                        holder._viewed.setVisibility(View.VISIBLE);
                                                    }
                                                }
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });


                            isOpened = false;
                            tVal = 100;

                            //allow the user to delete the current item when user long press the itemview...
                            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                                @Override
                                public boolean onLongClick(View v) {
                                    if (!isOpened) {
                                        isOpened = true;
                                        holder._userChatLayout.animate().translationXBy(-120).setDuration(500);
                                        new CountDownTimer(500, 500) {
                                            @Override
                                            public void onTick(long millisUntilFinished) {

                                            }

                                            @Override
                                            public void onFinish() {
                                                holder._chatDeleteItem.setVisibility(View.VISIBLE);
                                            }
                                        }.start();

                                        timer = new CountDownTimer(4000, 1000) {
                                            @Override
                                            public void onTick(long millisUntilFinished) {

                                            }

                                            @Override
                                            public void onFinish() {
                                                holder._chatDeleteItem.setVisibility(View.GONE);
                                                tVal = 0;
                                                holder._userChatLayout.animate().translationXBy(120).setDuration(500);
                                                isOpened = false;
                                            }
                                        };
                                        timer.start();

                                        //used to dele the data of selected chat...
                                        holder._chatDeleteItem.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                new AlertDialog.Builder(context)
                                                        .setTitle("Delete this chat")
                                                        .setIcon(R.drawable.app_logo)
                                                        .setMessage("Are you sure you want to delete this chat?")

                                                        // Specifying a listener allows you to take an action before dismissing the dialog.
                                                        // The dialog is automatically dismissed when a dialog button is clicked.
                                                        .setPositiveButton("Delete Chat", new DialogInterface.OnClickListener() {
                                                            public void onClick(DialogInterface dialog, int which) {
                                                                // Continue with delete operation
                                                                new CountDownTimer(500, 500) {
                                                                    @Override
                                                                    public void onTick(long millisUntilFinished) {
                                                                        holder._chatDeleteItem.setVisibility(View.GONE);
                                                                        holder._userChatLayout.animate().translationXBy(2000).setDuration(500);
                                                                    }

                                                                    @Override
                                                                    public void onFinish() {
                                                                        FirebaseDatabase.getInstance().getReference().child("ChatList").child(myId)
                                                                                .child(userId).removeValue();
                                                                        FirebaseDatabase.getInstance().getReference().child("Chat")
                                                                                .child(myId + userId).removeValue();
                                                                    }
                                                                }.start();


                                                                isOpened = false;

                                                            }
                                                        }).setOnCancelListener(new DialogInterface.OnCancelListener() {
                                                            @Override
                                                            public void onCancel(DialogInterface dialog) {
                                                                isOpened = false;
                                                            }
                                                        }).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialog, int which) {
                                                                isOpened = false;
                                                            }
                                                        }).setCancelable(true)
                                                        .show();
                                            }
                                        });
                                    }
                                    return true;
                                }
                            });

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
        }

    }

    // this method will return the count of our list...
    @Override
    public int getItemCount() {
        return chatListUser.size();
    }


    public class ChatListViewHolder extends RecyclerView.ViewHolder{

        // Adapter class for initializing
        // the views of our chat_list_items view...

        private ImageView _chatListProfileItem,_viewed, _chatDeleteItem, _lMImageItem;
        private TextView _chatListUsernameItem, _lastMessageItem;
        private LinearLayout _userChatLayout;

        public ChatListViewHolder(@NonNull View itemView) {
            super(itemView);

            _chatListProfileItem = (ImageView) itemView.findViewById(R.id._listProfileItem);
            _viewed = (ImageView) itemView.findViewById(R.id._viewed);
            _chatListUsernameItem = (TextView) itemView.findViewById(R.id._listUsernameItem);
            _chatDeleteItem = (ImageView) itemView.findViewById(R.id._chatDeleteItem);
            _userChatLayout = (LinearLayout) itemView.findViewById(R.id._userChatLayout);
            _lMImageItem = (ImageView) itemView.findViewById(R.id._lMImageItem);
            _lastMessageItem = (TextView) itemView.findViewById(R.id._lastMessageItem);

        }
    }
}
