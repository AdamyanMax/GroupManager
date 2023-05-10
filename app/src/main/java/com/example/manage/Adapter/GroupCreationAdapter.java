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
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class GroupCreationAdapter extends FirebaseRecyclerAdapter<Contacts, GroupCreationAdapter.GroupCreationViewHolder> {

    private final FirebaseUtil firebaseUtil = new FirebaseUtil();
    private final List<String> selectedUsers = new ArrayList<>();


    public GroupCreationAdapter(@NonNull FirebaseRecyclerOptions<Contacts> options) {
        super(options);
    }


    @Override
    protected void onBindViewHolder(@NonNull GroupCreationViewHolder holder, int position, @NonNull Contacts model) {
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

                    holder.cbSelectUsers.setVisibility(View.VISIBLE);
                    holder.cbSelectUsers.setOnCheckedChangeListener((buttonView, isChecked) -> {
                        String userId = Objects.requireNonNull(snapshot.child("uid").getValue()).toString();
                        if (isChecked) {
                            if (!selectedUsers.contains(userId)) {
                                selectedUsers.add(userId);
                            }
                        } else {
                            selectedUsers.remove(userId);
                        }
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
    public GroupCreationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_display_layout, parent, false);
        return new GroupCreationViewHolder(view);
    }

    public List<String> getSelectedUsers() {
        return selectedUsers;
    }


    public static class GroupCreationViewHolder extends RecyclerView.ViewHolder {
        TextView tvUserName, tvUserStatus;
        CircleImageView civProfileImage, civOnlineIcon;

        MaterialCheckBox cbSelectUsers;

        public GroupCreationViewHolder(@NonNull View itemView) {
            super(itemView);

            tvUserName = itemView.findViewById(R.id.tv_display_username);
            tvUserStatus = itemView.findViewById(R.id.tv_display_user_status);
            civProfileImage = itemView.findViewById(R.id.civ_display_profile_image);
            civOnlineIcon = itemView.findViewById(R.id.civ_display_online);
            cbSelectUsers = itemView.findViewById(R.id.cb_select_members);

        }
    }

}
