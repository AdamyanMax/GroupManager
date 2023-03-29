package com.example.manage.Adapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.manage.Chats.ChatsFragment;
import com.example.manage.Contacts.ContactsFragment;
import com.example.manage.Groups.GroupsFragment;

public class TabAccessorAdapter extends FragmentStateAdapter {
    private final String[] titles = new String[]{"Chats", "Groups", "Contacts"};

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
                return new ContactsFragment();
        }
        return new ChatsFragment();
    }

    @Override
    public int getItemCount() {
        return titles.length;
    }

    @Nullable
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "Chats";
            case 1:
                return "Groups";
            case 2:
                return "Contacts";
            default:
                return null;
        }
    }

}
