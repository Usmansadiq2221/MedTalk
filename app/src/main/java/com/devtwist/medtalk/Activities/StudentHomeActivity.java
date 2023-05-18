package com.devtwist.medtalk.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Bundle;

import com.devtwist.medtalk.Adapters.FragmentAdapter;
import com.devtwist.medtalk.R;
import com.devtwist.medtalk.databinding.ActivityStudentHomeBinding;
import com.google.android.material.tabs.TabLayout;

public class StudentHomeActivity extends AppCompatActivity {


    private ActivityStudentHomeBinding binding;

    private String accType = "", loginType = "";

    private FragmentAdapter fragmentAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityStudentHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

//        SharedPreferences preferences = getSharedPreferences("MedTalkData", MODE_PRIVATE);
//        accType = preferences.getString("accType", "");
//        loginType = preferences.getString("loginType", "");


        fragmentAdapter = new FragmentAdapter(this, "Student");
        binding.viewPager.setAdapter(fragmentAdapter);
        binding.viewPager.setUserInputEnabled(false);



        //set the fragment of related to title of tabitem...
        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                binding.viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        //set(point out) the tab item on the basis of current fragment...
        binding.viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                binding.tabLayout.getTabAt(position).select();
                binding.tabLayout.getTabAt(0).setIcon(R.drawable.home_icon);
                binding.tabLayout.getTabAt(1).setIcon(R.drawable.appointment_icon);
                binding.tabLayout.getTabAt(2).setIcon(R.drawable.messege_icon);
                binding.tabLayout.getTabAt(3).setIcon(R.drawable.user_icon);
                if (position == 0) {
                    binding.tabLayout.getTabAt(position).setIcon(R.drawable.select_home_icon);
                } else if (position == 1) {
                    binding.tabLayout.getTabAt(position).setIcon(R.drawable.select_appointment_icon);
                } else if (position == 2) {
                    binding.tabLayout.getTabAt(position).setIcon(R.drawable.select_messege_icon);
                }else {
                    binding.tabLayout.getTabAt(position).setIcon(R.drawable.select_user_icon);
                }
            }
        });

    }
}