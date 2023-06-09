package com.chat.group_manager.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.recyclerview.widget.RecyclerView;

import com.chat.group_manager.Helpers.FirebaseDatabaseReferences;
import com.chat.group_manager.Module.Contacts;
import com.chat.group_manager.R;
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

    private final FirebaseDatabaseReferences firebaseDatabaseReferences = new FirebaseDatabaseReferences();
    private final List<String> selectedUsers = new ArrayList<>();


    public GroupCreationAdapter(@NonNull FirebaseRecyclerOptions<Contacts> options) {
        super(options);
    }


    @Override
    protected void onBindViewHolder(@NonNull GroupCreationViewHolder holder, int position, @NonNull Contacts model) {
        String userIDs = getRef(position).getKey();

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

                    // Change the constraints to fix the issue with the not eclipsed status
                    ConstraintSet constraintSet = new ConstraintSet();
                    constraintSet.clone(holder.layout);

                    constraintSet.connect(R.id.ll_display_name_status, ConstraintSet.END, R.id.cb_select_members, ConstraintSet.START, 0);

                    constraintSet.applyTo(holder.layout);

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
        LinearLayout llNameStatus;
        MaterialCheckBox cbSelectUsers;
        ConstraintLayout layout;

        public GroupCreationViewHolder(@NonNull View itemView) {
            super(itemView);

            tvUserName = itemView.findViewById(R.id.tv_display_username);
            tvUserStatus = itemView.findViewById(R.id.tv_display_user_status);
            civProfileImage = itemView.findViewById(R.id.civ_display_profile_image);
            civOnlineIcon = itemView.findViewById(R.id.civ_display_online);
            cbSelectUsers = itemView.findViewById(R.id.cb_select_members);
            llNameStatus = itemView.findViewById(R.id.ll_display_name_status);
            layout = itemView.findViewById(R.id.cl_user_display_layout);
        }
    }

}
