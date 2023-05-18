package com.devtwist.medtalk.Adapters;

import android.content.Context;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.devtwist.medtalk.Models.BlockListData;
import com.devtwist.medtalk.Models.DoctorData;
import com.devtwist.medtalk.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class BlockedListAdapter extends RecyclerView.Adapter<BlockedListAdapter.BlockListViewHolder>{

    //for storing context of the activity passed from Call Fragment...
    private Context context;

    //for storing the list of calls data...
    private ArrayList<BlockListData> blockSellerList;

    //for storing the ids of the current users...
    private String myId, docId;
    private boolean isOpened;

    //initializing the variables and list in constructor passed from CallFragment...
    public BlockedListAdapter(Context context, ArrayList<BlockListData> blockSellerList) {
        this.context = context;
        this.blockSellerList = blockSellerList;
        isOpened = false;
    }


    // We are inflating the call_list_items
    // inside on Create View Holder method...
    @NonNull
    @Override
    public BlockListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.block_list_items, parent, false);
        return new BlockListViewHolder(view);
    }

    // Inside on bind view holder we will
    // set data to item of Recycler View...

    @Override
    public void onBindViewHolder(@NonNull BlockListViewHolder holder, int position) {
        BlockListData model = blockSellerList.get(position);
        docId = model.getUserId();
        myId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseDatabase.getInstance().getReference().child("Users").child("Doctor")
                .child(docId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        DoctorData userdata = snapshot.getValue(DoctorData.class);
                        holder._blockUsernameItem.setText(userdata.getUsername());
                        if (userdata.getProfileUrl().length()>1) {
                            Picasso.get().load(userdata.getProfileUrl()).placeholder(R.drawable.enter_profile_icon).into(holder._blockListProfileItem);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


        isOpened = false;

        //allow the user to delete the current item when user long press the itemview...
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (!isOpened) {
                    isOpened = true;
                    holder._blockListLayout.animate().translationXBy(-125).setDuration(500);
                    new CountDownTimer(500, 500) {
                        @Override
                        public void onTick(long millisUntilFinished) {

                        }

                        @Override
                        public void onFinish() {
                            holder._blockDeleteItem.setVisibility(View.VISIBLE);
                        }
                    }.start();
                    new CountDownTimer(3500, 1000) {
                        @Override
                        public void onTick(long millisUntilFinished) {

                        }

                        @Override
                        public void onFinish() {
                            holder._blockDeleteItem.setVisibility(View.GONE);
                            holder._blockListLayout.animate().translationXBy(125).setDuration(500);
                            isOpened = false;

                        }
                    }.start();

                    //automaticaly hide the delete icon if user not delete the current item...
                    holder._blockDeleteItem.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            new CountDownTimer(500, 500) {
                                @Override
                                public void onTick(long millisUntilFinished) {
                                    holder._blockDeleteItem.setVisibility(View.GONE);
                                    holder._blockListLayout.animate().translationXBy(2000).setDuration(500);
                                }

                                @Override
                                public void onFinish() {
                                    FirebaseDatabase.getInstance().getReference().child("BlockList").child(myId)
                                            .child(model.getUserId()).removeValue();
                                }
                            }.start();
                            isOpened = false;
                        }
                    });
                }
                return true;
            }
        });
    }


    // this method will return the count of our list...
    @Override
    public int getItemCount() {
        return blockSellerList.size();
    }

    public class BlockListViewHolder extends RecyclerView.ViewHolder{

        // Adapter class for initializing
        // the views of our call_list_items view...
        private ImageView _blockListProfileItem, _blockDeleteItem;
        private TextView _blockUsernameItem;
        private LinearLayout _blockListLayout;
        public BlockListViewHolder(@NonNull View itemView) {
            super(itemView);
            _blockListProfileItem = (ImageView) itemView.findViewById(R.id._blockListProfileItem);
            _blockUsernameItem = (TextView) itemView.findViewById(R.id._blockUsernameItem);
            _blockDeleteItem = (ImageView) itemView.findViewById(R.id._blockDeleteItem);
            _blockListLayout = (LinearLayout) itemView.findViewById(R.id._blockListLayout);
        }
    }
}
