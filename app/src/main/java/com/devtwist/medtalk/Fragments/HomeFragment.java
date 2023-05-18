package com.devtwist.medtalk.Fragments;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.devtwist.medtalk.Activities.AllCategoriesActivity;
import com.devtwist.medtalk.Activities.NotificationActivity;
import com.devtwist.medtalk.Activities.SearchDoctorActivity;
import com.devtwist.medtalk.Adapters.AutoSliderAdapter;
import com.devtwist.medtalk.Adapters.CategoryAdapter;
import com.devtwist.medtalk.Adapters.DoctorAdapter;
import com.devtwist.medtalk.Models.DoctorData;
import com.devtwist.medtalk.Models.PatientData;
import com.devtwist.medtalk.R;
import com.devtwist.medtalk.databinding.FragmentHomeBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.type.DateTime;
import com.smarteist.autoimageslider.SliderView;
import com.squareup.picasso.Picasso;

import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;


public class HomeFragment extends Fragment {


    private FragmentHomeBinding binding;
    private Context mContext;
    private int[] imagesList = {R.drawable.slider_first, R.drawable.slider_sec, R.drawable.slider_third};
    private DatabaseReference reference;
    //used to load images in _postImageSlider...
    private AutoSliderAdapter autoSliderAdapter;
    private String accType = "", userId = "";

    private RecyclerView _categoryView;
    private CategoryAdapter categoryAdapter;

    private ArrayList<DoctorData> docList;

    // used for showing data in _prePostView(recyclerview)
    private DoctorAdapter doctorAdapter;

    private String myId = "";


    private int[] categoryImaegList = {R.drawable.cardiologist, R.drawable.dermatologist,R.drawable.dentist,
                R.drawable.neurologist, R.drawable.orthopedist, R.drawable.oncologist,
                R.drawable.psychologist, R.drawable.surgeon, R.drawable.urologist};

    // adding strings in array list for showing list of categories in _categoryView(RecyclerView)...
    private String[] categoryTextList = {"Cardiologist", "Dermatologist", "Dentist",
                "Neurologist", "Orthopedist", "Oncologist",
                "Psychologist", "Surgeon", "Urologist"};

    Intent intent;

    public HomeFragment() {
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
        binding = FragmentHomeBinding.inflate(inflater,container,false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mContext = view.getContext();


        SharedPreferences preferences = mContext.getSharedPreferences("MedTalkData", MODE_PRIVATE);
        accType = preferences.getString("accType", "");

        Date date = new Date();   // given date
        Calendar calendar = GregorianCalendar.getInstance(); // creates a new calendar instance
        calendar.setTime(date);   // assigns calendar to given date
        int hour = calendar.get(Calendar.HOUR_OF_DAY); // gets hour in 24h format

        if (hour<=12 && hour>5){
            binding.wish.setText("Good Morning");
        }
        else if (hour>=12 && hour<17){
            binding.wish.setText("Good Afternoon");
        }
        else if (hour >= 17 && hour < 21) {
            binding.wish.setText("Good Evening");
        }
        else {
            binding.wish.setText("Good Night");
        }


        myId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // passing this array list inside our adapter class.
        autoSliderAdapter = new AutoSliderAdapter(mContext, imagesList);

        // below method is used to set auto cycle direction in left to
        // right direction you can change according to requirement.
        binding.postImageSlider.setAutoCycleDirection(SliderView.LAYOUT_DIRECTION_LTR);

        // below method is used to
        // setadapter to sliderview.
        binding.postImageSlider.setSliderAdapter(autoSliderAdapter);


        // below method is use to set
        // scroll time in seconds.
        binding.postImageSlider.setScrollTimeInSec(3);

        // to set it scrollable automatically
        // we use below method.
        binding.postImageSlider.setAutoCycle(true);
        binding.postImageSlider.startAutoCycle();

        binding.notifications.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mContext.startActivity(new Intent(mContext, NotificationActivity.class));
            }
        });

        binding.seeAllCat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mContext.startActivity(new Intent(mContext, AllCategoriesActivity.class));
            }
        });

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid().toString();

        reference = FirebaseDatabase.getInstance().getReference().child("Users");

        reference.child(accType).child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                PatientData patientData = snapshot.getValue(PatientData.class);
                binding.username.setText(patientData.getUsername());
                binding.username.setBackgroundResource(R.drawable.teal_round_square_fill);
                if (patientData.getProfileUrl().length()>1) {
                    Picasso.get().load(patientData.getProfileUrl()).placeholder(R.drawable.enter_profile_icon).into(binding.profileimage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //set up layout manager for _categoryView(RecyclerView to load category data...
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mContext);
        linearLayoutManager.setOrientation(RecyclerView.HORIZONTAL);
        binding.categoryView.setLayoutManager(linearLayoutManager);

        //initializing Category Adapter class and also pass parameters to its constructor to load lists in _categoryView...
        categoryAdapter = new CategoryAdapter(mContext, categoryImaegList,categoryTextList);
        binding.categoryView.setAdapter(categoryAdapter);
        initBottomDots(categoryAdapter.getItemCount(), binding.categoryView);



        binding.searchDoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDoctorActivity();
            }
        });

        binding.seeAllDoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDoctorActivity();
            }
        });

        LinearLayoutManager docLayoutManager = new LinearLayoutManager(mContext);
        docLayoutManager.setReverseLayout(true);
        docLayoutManager.setStackFromEnd(true);



        binding.popularDocView.setLayoutManager(docLayoutManager);
        docList = new ArrayList<>();
        doctorAdapter = new DoctorAdapter(mContext, docList, myId);
        binding.popularDocView.setAdapter(doctorAdapter);
        binding.popularDocView.showShimmerAdapter();

        readDoctorList();

    }

    private void initBottomDots(int itemCount, RecyclerView recyclerView) {
        LinearLayout linearLayout = getBottomdotsLayout(itemCount);
        recyclerView.setOnScrollChangeListener(new View.OnScrollChangeListener() {
            @Override
            public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                int firstVisible = ((LinearLayoutManager)recyclerView.getLayoutManager()).findFirstVisibleItemPosition();
                int lastVisible = ((LinearLayoutManager)recyclerView.getLayoutManager()).findLastVisibleItemPosition();
                int total = 0;
                for (int i=firstVisible; i <lastVisible; i++){
                    total++;
                }
                transitionDots(linearLayout, lastVisible, total);
            }
        });
    }

    private void transitionDots(LinearLayout linearLayout, int lastVisibleIndex, int totalItems) {
        for (int i=0; i< linearLayout.getChildCount(); i++){
            if (linearLayout.getChildAt(i) instanceof TextView){
                linearLayout.getChildAt(i).setBackgroundResource(R.drawable.indicator_1);
            }
        }

        //set dots on position for all visible items...
        for (int j=0; j<=totalItems; j++){
            if (lastVisibleIndex>=0){
                linearLayout.getChildAt(lastVisibleIndex).setBackgroundResource(R.drawable.indicator_2);
                lastVisibleIndex--;
            }
        }
    }

    private LinearLayout getBottomdotsLayout(int itemCount) {

        for (int i=0; i<itemCount; i++){
            TextView textView = new TextView(mContext);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(20,20);
            params.setMargins(3,3,3,3);
            textView.setLayoutParams(params);
            textView.setBackgroundResource(R.drawable.indicator_1);
            binding.dotsLinearLayout.addView(textView);
        }
        return binding.dotsLinearLayout;
    }

//    private LinearLayout getBottomdotsLayout(int itemCount) {
//        LinearLayout linearLayout = new LinearLayout(mContext);
//        linearLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
//        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
//        linearLayout.setGravity(Gravity.CENTER);
//        for (int i=0; i<itemCount; i++){
//            TextView textView = new TextView(mContext);
//            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(15,4);
//            params.setMargins(3,2,3,2);
//            textView.setLayoutParams(params);
//            textView.setBackgroundResource(R.drawable.indicator_1);
//            linearLayout.addView(textView);
//        }
//        binding.homeLayout.addView(linearLayout);
//        return linearLayout;
//    }


    private void openDoctorActivity() {
        intent = new Intent(mContext, SearchDoctorActivity.class);
        intent.putExtra("searchType", "simple");
        startActivity(intent);
    }

    private void readDoctorList() {
        FirebaseDatabase.getInstance().getReference().child("Users")
                .child("Doctor").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        docList.clear();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                            DoctorData doctorData = dataSnapshot.getValue(DoctorData.class);
                            docList.add(doctorData);
                        }
                        docList.sort(Comparator.comparing(DoctorData::getRating));
                        doctorAdapter.notifyDataSetChanged();
                        binding.popularDocView.hideShimmerAdapter();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }


}