package com.devtwist.medtalk.Fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.devtwist.medtalk.Adapters.ChatListAdapter;
import com.devtwist.medtalk.Models.ChatListData;
import com.devtwist.medtalk.R;
import com.devtwist.medtalk.databinding.FragmentChatListBinding;
import com.devtwist.medtalk.databinding.FragmentHomeBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Comparator;


public class ChatListFragment extends Fragment {


    private FragmentChatListBinding binding;
    private Context mContext;

    private ArrayList<ChatListData> chatArrayList;

    // used to load the data from array list in (RecyclerView)...
    private ChatListAdapter chatListAdapter;

    //used to store the id of the current user...
    private String myId;

    public ChatListFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentChatListBinding.inflate(inflater,container,false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mContext = view.getContext();

        myId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // set upo the layout manager for _searchPreChats...
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mContext);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        binding.searchPreChats.setLayoutManager(linearLayoutManager);

        chatArrayList = new ArrayList<>();

        // initializing the ChatListAdapter and also pass the parameters to its constructor...
        chatListAdapter = new ChatListAdapter(mContext, chatArrayList, myId);
        binding.searchPreChats.setAdapter(chatListAdapter);
        binding.searchPreChats.showShimmerAdapter();

        //get the input value to search the chat...
        binding.searchChat.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                readChatList();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        readChatList();



    }

    private void readChatList(){
        try {
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            database.getReference().child("ChatList").child(myId).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    chatArrayList.clear();
                    if (binding.searchChat.getText().length()>0) {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            ChatListData chatListData = dataSnapshot.getValue(ChatListData.class);
                            if (chatListData.getUsername().toLowerCase()
                                    .contains(binding.searchChat.getText().toString().toLowerCase())) {
                                chatArrayList.add(chatListData);
                            }
                        }
                    }
                    else {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            ChatListData chatListData = dataSnapshot.getValue(ChatListData.class);
                            chatArrayList.add(chatListData);
                        }
                    }
                    chatArrayList.sort(Comparator.comparing(ChatListData::getTimeStamp));
                    binding.searchPreChats.hideShimmerAdapter();
                    chatListAdapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Log.i("ChatLstError", e.getMessage().toString());
        }
    }



}