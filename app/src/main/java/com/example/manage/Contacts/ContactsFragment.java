package com.example.manage.Contacts;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.manage.Data.Contacts;
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

public class ContactsFragment extends Fragment {
    private RecyclerView rvContactList;

    private DatabaseReference ContactsRef, UsersRef;

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
        ContactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserID);
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        return contactView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Contacts> options =
                new FirebaseRecyclerOptions.Builder<Contacts>()
                        .setQuery(ContactsRef, Contacts.class)
                        .build();
        final FirebaseRecyclerAdapter<Contacts, ContactsViewHolder> adapter =
                new FirebaseRecyclerAdapter<Contacts, ContactsViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull ContactsViewHolder holder, int position, @NonNull Contacts model) {
                        String userIDs = getRef(position).getKey();

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
                                        String userImage = Objects.requireNonNull(snapshot.child("image").getValue()).toString();
                                        String profileName = Objects.requireNonNull(snapshot.child("name").getValue()).toString();
                                        String profileStatus = Objects.requireNonNull(snapshot.child("status").getValue()).toString();

                                        holder.tvUserName.setText(profileName);
                                        holder.tvUserStatus.setText(profileStatus);
                                        Picasso.get().load(userImage).placeholder(R.drawable.user_default_profile_pic).into(holder.civProfileImage);
                                    } else {
                                        String profileName = Objects.requireNonNull(snapshot.child("name").getValue()).toString();
                                        String profileStatus = Objects.requireNonNull(snapshot.child("status").getValue()).toString();

                                        holder.tvUserName.setText(profileName);
                                        holder.tvUserStatus.setText(profileStatus);
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }

                    @NonNull
                    @Override
                    public ContactsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_display_layout, parent, false);
                        return new ContactsViewHolder(view);
                    }
                };
        rvContactList.setAdapter(adapter);
        adapter.startListening();
    }

    public static class ContactsViewHolder extends RecyclerView.ViewHolder {
        TextView tvUserName, tvUserStatus;
        CircleImageView civProfileImage, civOnlineIcon;

        public ContactsViewHolder(@NonNull View itemView) {
            super(itemView);

            tvUserName = itemView.findViewById(R.id.tv_display_username);
            tvUserStatus = itemView.findViewById(R.id.tv_display_user_status);
            civProfileImage = itemView.findViewById(R.id.civ_display_profile_image);
            civOnlineIcon = itemView.findViewById(R.id.civ_display_online);
        }
    }
}