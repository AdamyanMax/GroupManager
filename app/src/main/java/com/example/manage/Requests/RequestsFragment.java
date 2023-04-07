package com.example.manage.Requests;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.manage.R;

public class RequestsFragment extends Fragment {
    private View RequestsFragmentView;
    private RecyclerView rvRequestsList;

    public RequestsFragment() {
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
        RequestsFragmentView =  inflater.inflate(R.layout.fragment_requests, container, false);

        rvRequestsList = RequestsFragmentView.findViewById(R.id.rv_chat_requests);
        rvRequestsList.setLayoutManager(new LinearLayoutManager(getContext()));

        return RequestsFragmentView;
    }
}