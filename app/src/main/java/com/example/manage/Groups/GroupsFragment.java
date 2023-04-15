package com.example.manage.Groups;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.manage.Adapter.GroupsAdapter;
import com.example.manage.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


public class GroupsFragment extends Fragment {

    private View vGroupFragment;
    private final ArrayList<String> listOfGroups = new ArrayList<>();
    private GroupsAdapter groupsAdapter;
    private DatabaseReference GroupRef;

    public GroupsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        vGroupFragment = inflater.inflate(R.layout.fragment_groups, container, false);

        GroupRef = FirebaseDatabase.getInstance().getReference().child("Groups");

        initializeFields();

        retrieveAndDisplayGroups();

        return vGroupFragment;
    }


    private void retrieveAndDisplayGroups() {
        GroupRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<String> newListOfGroups = new ArrayList<>();

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    newListOfGroups.add(dataSnapshot.getKey());
                }

                groupsAdapter.updateData(newListOfGroups);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void initializeFields() {
        RecyclerView recyclerView = vGroupFragment.findViewById(R.id.rvGroups);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        groupsAdapter = new GroupsAdapter(listOfGroups);
        recyclerView.setAdapter(groupsAdapter);

        groupsAdapter.setOnItemClickListener(position -> {
            String currentGroupName = listOfGroups.get(position);

            Intent groupChatIntent = new Intent(getContext(), GroupChatActivity.class);
            groupChatIntent.putExtra("groupName", currentGroupName);
            startActivity(groupChatIntent);
        });
    }


}
