package com.chat.group_manager.Chats.Profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.chat.group_manager.R;

public class FilesFragment extends Fragment {

    public FilesFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        // Initialize your views here...

        return inflater.inflate(R.layout.fragment_files, container, false);
    }
}
