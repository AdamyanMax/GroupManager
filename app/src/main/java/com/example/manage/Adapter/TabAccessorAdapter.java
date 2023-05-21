package com.example.manage.Adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.manage.Chats.ChatsFragment;
import com.example.manage.Groups.GroupsFragment;
import com.example.manage.Requests.RequestsFragment;

public class TabAccessorAdapter extends FragmentStateAdapter {

    public TabAccessorAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new ChatsFragment();
            case 1:
                return new GroupsFragment();
            case 2:
                return new RequestsFragment();
        }
        return new ChatsFragment();
    }

    @Override
    public int getItemCount() {
        return 3;
    }

}
