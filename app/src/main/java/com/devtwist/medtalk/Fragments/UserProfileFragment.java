package com.devtwist.medtalk.Fragments;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cooltechworks.views.shimmer.ShimmerRecyclerView;
import com.devtwist.medtalk.Adapters.ComponentAdapter;
import com.devtwist.medtalk.Models.PatientData;
import com.devtwist.medtalk.Models.StudentData;
import com.devtwist.medtalk.R;
import com.devtwist.medtalk.databinding.FragmentHomeBinding;
import com.devtwist.medtalk.databinding.FragmentUserProfileBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;


public class UserProfileFragment extends Fragment {


    private FragmentUserProfileBinding binding;
    private Context context;
    private ComponentAdapter componentAdapter;

    private int[] componentImageList = {R.drawable.profile_icon, R.drawable.edit_profile_icon, R.drawable.history_icon,
            R.drawable.feedback_icon, R.drawable.setting_icon, R.drawable.logout_icon};

    // adding strings in array list for showing list of categories in _categoryView(RecyclerView)...
    private String[] componentNameList = {"Personal Details", "Edit Personal Details", "History",
            "Feedbacks", "Setting", "Log Out"};

    private String accType, myId;


    public UserProfileFragment() {
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
        binding = FragmentUserProfileBinding.inflate(inflater,container,false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        context = view.getContext();

        SharedPreferences preferences = context.getSharedPreferences("MedTalkData", MODE_PRIVATE);
        accType = preferences.getString("accType", "");

        myId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseDatabase.getInstance().getReference().child("Users").child(accType)
                .child(myId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        StudentData userdata = snapshot.getValue(StudentData.class);
                        Picasso.get().load(userdata.getProfileUrl()).into(binding.profileimage);
                        binding.username.setText(userdata.getUsername());
                        binding.username.setBackgroundResource(R.color.myDarkColor);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        ShimmerRecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(context, 2);
        binding.componentView.setLayoutManager(mLayoutManager);

        componentAdapter = new ComponentAdapter(context, componentImageList,componentNameList);
        binding.componentView.setAdapter(componentAdapter);


    }
}