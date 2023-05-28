package com.example.manage.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.manage.Adapter.GroupCreationAdapter;
import com.example.manage.Helpers.FirebaseDatabaseReferences;
import com.example.manage.Module.Contacts;
import com.example.manage.R;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;
import java.util.Objects;

public class CreateGroupSheetDialogFragment extends BottomSheetDialogFragment {
    private GroupCreationAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.create_group_bottom_sheet_dialog_content, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Set the initial height of the bottom sheet to 60% of the screen height
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        int initialHeight = (int) (displayMetrics.heightPixels * 0.7);

        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = initialHeight;
        view.setLayoutParams(layoutParams);

        // Initialize RecyclerView
        RecyclerView recyclerView = view.findViewById(R.id.rv_group_creation_select_contact_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        String currentUserId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        FirebaseDatabaseReferences firebaseDatabaseReferences = new FirebaseDatabaseReferences();
        FirebaseRecyclerOptions<Contacts> options =
                new FirebaseRecyclerOptions.Builder<Contacts>()
                        .setQuery(firebaseDatabaseReferences.getContactsRef().child(currentUserId), Contacts.class)
                        .build();
        // Initialize GroupCreationAdapter and set it as the adapter for the RecyclerView
        adapter = new GroupCreationAdapter(options);
        recyclerView.setAdapter(adapter);

        FloatingActionButton fabCreateGroup = view.findViewById(R.id.fab_create_group);
        fabCreateGroup.setOnClickListener(v -> {
            List<String> selectedUsers = adapter.getSelectedUsers();
            String selectedUsersText = TextUtils.join(", ", selectedUsers);
            Toast.makeText(getContext(), "Selected users: " + selectedUsersText, Toast.LENGTH_LONG).show();
            dismiss();
        });
    }


    @Override
    public void onStart() {
        super.onStart();
        adapter.startListening();

        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        }

        View bottomSheetInternal = getDialog().findViewById(com.google.android.material.R.id.design_bottom_sheet);
        if (bottomSheetInternal != null) {
            BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheetInternal);
            behavior.setPeekHeight((int) (getScreenHeight() * 0.7));
            behavior.setFitToContents(false);
            behavior.setHalfExpandedRatio(0.7f);
            behavior.setExpandedOffset(0);
            behavior.setState(BottomSheetBehavior.STATE_HALF_EXPANDED);

            // Add a bottom sheet callback to detect the state change
            behavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
                @Override
                public void onStateChanged(@NonNull View bottomSheet, int newState) {
                    if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                        ViewGroup.LayoutParams layoutParams = bottomSheet.getLayoutParams();
                        layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
                        bottomSheet.setLayoutParams(layoutParams);
                    }
                }

                @Override
                public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                    // Do nothing
                }
            });
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
    }


    private int getScreenHeight() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        if (getActivity() != null) {
            getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        }
        return displayMetrics.heightPixels;
    }


}
