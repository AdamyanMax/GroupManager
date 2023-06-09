package com.chat.group_manager.Contacts;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chat.group_manager.Adapter.ContactsAdapter;
import com.chat.group_manager.Helpers.FirebaseDatabaseReferences;
import com.chat.group_manager.Module.Contacts;
import com.chat.group_manager.R;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;

import java.util.Objects;

public class ContactsFragment extends Fragment {
    private final FirebaseDatabaseReferences firebaseDatabaseReferences = new FirebaseDatabaseReferences();
    private RecyclerView rvContactList;
    private DatabaseReference ContactsUserIdRef;

    public ContactsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View contactView = inflater.inflate(R.layout.fragment_contacts, container, false);

        rvContactList = contactView.findViewById(R.id.rvContactList);
        rvContactList.setLayoutManager(new LinearLayoutManager(getContext()));

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        String currentUserID = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        ContactsUserIdRef = firebaseDatabaseReferences.getContactsRef().child(currentUserID);

        return contactView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Contacts> options =
                new FirebaseRecyclerOptions.Builder<Contacts>()
                        .setQuery(ContactsUserIdRef, Contacts.class)
                        .build();
        ContactsAdapter adapter = new ContactsAdapter(options);
        rvContactList.setAdapter(adapter);
        adapter.startListening();
    }


}