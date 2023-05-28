package com.example.manage.Chats;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.manage.Helpers.FirebaseDatabaseReferences;
import com.example.manage.Helpers.ProgressBar.ProgressBarHandler;
import com.example.manage.Module.Contacts;
import com.example.manage.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatsFragment extends Fragment{
    private final FirebaseDatabaseReferences firebaseDatabaseReferences = new FirebaseDatabaseReferences();
    private RecyclerView rvChatList;
    private DatabaseReference ChatsUserIdRef;
    private FirebaseAuth mAuth;
    private ProgressBarHandler progressBarHandler;
    private FirebaseRecyclerAdapter<Contacts, ChatsViewHolder> adapter;
    private String currentUserID;

    public ChatsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View vPrivateChats = inflater.inflate(R.layout.fragment_chats, container, false);
        progressBarHandler = new ProgressBarHandler(vPrivateChats);

        rvChatList = vPrivateChats.findViewById(R.id.rv_private_chats);
        rvChatList.setLayoutManager(new LinearLayoutManager(getContext()));

        mAuth = FirebaseAuth.getInstance();
        String currentUserID = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();

        ChatsUserIdRef = firebaseDatabaseReferences.getContactsRef().child(currentUserID);

        return vPrivateChats;
    }

    @Override
    public void onStart() {
        super.onStart();

        progressBarHandler.show();

        FirebaseRecyclerOptions<Contacts> options = new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(ChatsUserIdRef, Contacts.class)
                .build();
        adapter = new FirebaseRecyclerAdapter<Contacts, ChatsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull ChatsViewHolder holder, int position, @NonNull Contacts model) {
                final String userIDs = getRef(position).getKey();
                final String[] profileImage = {"default_image"};

                assert userIDs != null;
                firebaseDatabaseReferences.getUsersRef().child(userIDs).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            if (snapshot.child("userState").hasChild("state")) {
                                String state = Objects.requireNonNull(snapshot.child("userState").child("state").getValue()).toString();

                                if (state.equals("online")) {
                                    holder.civOnlineIcon.setVisibility(View.VISIBLE);
                                } else if (state.equals("offline")) {

                                    holder.civOnlineIcon.setVisibility(View.INVISIBLE);
                                }

                            } else {
                                holder.civOnlineIcon.setVisibility(View.INVISIBLE);
                            }

                            if (snapshot.hasChild("image")) {
                                profileImage[0] = Objects.requireNonNull(snapshot.child("image").getValue()).toString();
                                Picasso.get().load(profileImage[0]).into(holder.civProfileImage);
                            }
                            final String name = Objects.requireNonNull(snapshot.child("name").getValue()).toString();
                            final String status = Objects.requireNonNull(snapshot.child("status").getValue()).toString();

                            holder.tvUsername.setText(name);

                            getLastMessage(userIDs, holder.tvUserLastMessage);

                            holder.itemView.setOnClickListener(v -> {
                                Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                chatIntent.putExtra("visit_user_id", userIDs);
                                chatIntent.putExtra("visit_username", name);
                                chatIntent.putExtra("visit_image", profileImage[0]);
                                chatIntent.putExtra("visit_user_status", status);
                                startActivity(chatIntent);
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }

            @NonNull
            @Override
            public ChatsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_display_layout, parent, false);
                return new ChatsViewHolder(view);
            }

            @Override
            public void onDataChanged() {
                // When data has loaded, hide the progress bar
                progressBarHandler.hide();
            }
        };

        rvChatList.setAdapter(adapter);
        adapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    private void getLastMessage(String userId, TextView tvUserLastStatus) {
        if (mAuth.getCurrentUser() != null) {
            currentUserID = mAuth.getCurrentUser().getUid();
        } else {
            // handle the case where no user is signed in
            Log.d("TAG", "No user is currently signed in");
        }

        firebaseDatabaseReferences.getMessagesRef().child(currentUserID).child(userId).limitToLast(1).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (isAdded()) {
                    if (dataSnapshot.exists()) {
                        for (DataSnapshot messageSnapshot : dataSnapshot.getChildren()) {
                            String messageType = Objects.requireNonNull(messageSnapshot.child("type").getValue()).toString();
                            String lastMessage;

                            if (messageType.equals("text")) {
                                lastMessage = Objects.requireNonNull(messageSnapshot.child("message").getValue()).toString();
                            } else if (messageType.equals("image")) {
                                lastMessage = getResources().getString(R.string.photo);
                                tvUserLastStatus.setTextColor(ContextCompat.getColor(tvUserLastStatus.getContext(), R.color.colorPrimaryDark));
                            } else {
                                lastMessage = getResources().getString(R.string.file);
                                tvUserLastStatus.setTextColor(ContextCompat.getColor(tvUserLastStatus.getContext(), R.color.colorPrimaryDark));
                            }

                            tvUserLastStatus.setText(lastMessage);
                        }
                    } else {
                        tvUserLastStatus.setText(R.string.no_messages_yet);
                        tvUserLastStatus.setTextColor(ContextCompat.getColor(tvUserLastStatus.getContext(), R.color.grey));
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    public static class ChatsViewHolder extends RecyclerView.ViewHolder {
        CircleImageView civProfileImage, civOnlineIcon;
        TextView tvUserLastMessage, tvUsername;

        public ChatsViewHolder(@NonNull View itemView) {
            super(itemView);

            civProfileImage = itemView.findViewById(R.id.civ_display_profile_image);
            tvUsername = itemView.findViewById(R.id.tv_display_username);
            tvUserLastMessage = itemView.findViewById(R.id.tv_display_user_status);
            civOnlineIcon = itemView.findViewById(R.id.civ_display_online);
        }
    }
}