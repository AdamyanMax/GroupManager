package com.example.manage.Chats;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.manage.Contacts.Contacts;
import com.example.manage.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
public class ChatsFragment extends Fragment {
    private RecyclerView rvChatList;
    private DatabaseReference ChatsRef, UsersRef;

    public ChatsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View vPrivateChats = inflater.inflate(R.layout.fragment_chats, container, false);

        rvChatList = vPrivateChats.findViewById(R.id.rv_private_chats);
        rvChatList.setLayoutManager(new LinearLayoutManager(getContext()));

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        String currentUserID = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();

        ChatsRef = FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserID);
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        return vPrivateChats;
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseRecyclerOptions<Contacts> options = new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(ChatsRef, Contacts.class)
                .build();
        FirebaseRecyclerAdapter<Contacts, ChatsViewHolder> adapter =
                new FirebaseRecyclerAdapter<Contacts, ChatsViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull ChatsViewHolder holder, int position, @NonNull Contacts model) {
                        final String userIDs = getRef(position).getKey();
                        final String[] profileImage = {"default_image"};

                        assert userIDs != null;
                        UsersRef.child(userIDs).addValueEventListener(new ValueEventListener() {
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

                                    holder.tvUsername.setText(name);
                                    // TODO: Replace with the last message
                                    holder.tvUserStatus.setText(R.string.last_seen_at);

                                    holder.itemView.setOnClickListener(v -> {
                                        Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                        chatIntent.putExtra("visit_user_id", userIDs);
                                        chatIntent.putExtra("visit_username", name);
                                        chatIntent.putExtra("visit_image", profileImage[0]);
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
                };

        rvChatList.setAdapter(adapter);
        adapter.startListening();
    }

    public static class ChatsViewHolder extends RecyclerView.ViewHolder {
        CircleImageView civProfileImage, civOnlineIcon;
        TextView tvUserStatus, tvUsername;

        public ChatsViewHolder(@NonNull View itemView) {
            super(itemView);

            civProfileImage = itemView.findViewById(R.id.civ_display_profile_image);
            tvUsername = itemView.findViewById(R.id.tv_display_username);
            tvUserStatus = itemView.findViewById(R.id.tv_display_user_status);
            civOnlineIcon = itemView.findViewById(R.id.civ_display_online);
        }
    }
}