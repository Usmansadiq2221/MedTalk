package com.devtwist.medtalk.Adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.devtwist.medtalk.Fragments.AppointmentFragment;
import com.devtwist.medtalk.Fragments.ChatListFragment;
import com.devtwist.medtalk.Fragments.HomeFragment;
import com.devtwist.medtalk.Fragments.UserProfileFragment;

public class FragmentAdapter extends FragmentStateAdapter {

    private String accType = "";

    public FragmentAdapter(@NonNull FragmentActivity fragmentActivity, String accType) {
        super(fragmentActivity);
        this.accType = accType;
    }


    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (accType.equals("Doctor")){
            switch (position){
                case 0:
                    return new AppointmentFragment();
                case 1:
                    return new ChatListFragment();
                case 2:
                    return new UserProfileFragment();
                default:
                    return new AppointmentFragment();
            }
        }
        else {
            switch (position) {
                case 0:
                    return new HomeFragment();
                case 1:
                    return new AppointmentFragment();
                case 2:
                    return new ChatListFragment();
                case 3:
                    return new UserProfileFragment();
                default:
                    return new HomeFragment();
            }
        }
    }

    @Override
    public int getItemCount() {
        int count = 0;
        if (accType.equals("Doctor")){
            count = 3;
        }
        else {
            count = 4;
        }
        return count;
    }
}
