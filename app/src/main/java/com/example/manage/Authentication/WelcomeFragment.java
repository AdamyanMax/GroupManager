package com.example.manage.Authentication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.manage.R;

public class WelcomeFragment extends Fragment {

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_welcome, container, false);
        Button buttonNavigateToAuth = root.findViewById(R.id.btn_navigate_to_auth);
        buttonNavigateToAuth.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.action_welcomeFragment_to_authFragment));
        return root;
    }
}
