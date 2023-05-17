package com.example.manage.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.manage.Helpers.FirebaseUtil;
import com.example.manage.Module.Contacts;
import com.example.manage.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class ContactsAdapter extends FirebaseRecyclerAdapter<Contacts, ContactsAdapter.ContactsViewHolder> {

    private final FirebaseUtil firebaseUtil = new FirebaseUtil();

    public ContactsAdapter(@NonNull FirebaseRecyclerOptions<Contacts> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull ContactsViewHolder holder, int position, @NonNull Contacts model) {
        String userIDs = getRef(position).getKey();

        assert userIDs != null;
        firebaseUtil.getUsersRef().child(userIDs).addValueEventListener(new ValueEventListener() {
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
