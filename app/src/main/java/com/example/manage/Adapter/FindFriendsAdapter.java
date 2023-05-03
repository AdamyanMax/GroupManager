package com.example.manage.Adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.manage.Data.Contacts;
import com.example.manage.Menu.FindFriends.ProfileActivity;
import com.example.manage.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


public class FindFriendsAdapter extends FirebaseRecyclerAdapter<Contacts, FindFriendsAdapter.FindFriendsViewHolder> {

    public FindFriendsAdapter(@NonNull FirebaseRecyclerOptions<Contacts> options) {
        super(options);
    }

    @NonNull
    @Override
    public FindFriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_display_layout, parent, false);
        return new FindFriendsViewHolder(view);
    }

    @Override
    protected void onBindViewHolder(@NonNull FindFriendsViewHolder holder, int position, @NonNull Contacts model) {
        holder.name.setText(model.getName());
        holder.status.setText(model.getStatus());
        Picasso.get().load(model.getImage()).placeholder(R.drawable.user_default_profile_pic).into(holder.profileImage);

        holder.itemView.setOnClickListener(v -> {
            String visit_user_id = getRef(position).getKey();

            Intent profileIntent = new Intent(holder.itemView.getContext(), ProfileActivity.class);
            profileIntent.putExtra("visit_user_id", visit_user_id);
            holder.itemView.getContext().startActivity(profileIntent);
        });
    }

    public static class FindFriendsViewHolder extends RecyclerView.ViewHolder {
        TextView name, status;
        CircleImageView profileImage;

        public FindFriendsViewHolder(@NonNull View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.tv_display_username);
            status = itemView.findViewById(R.id.tv_display_user_status);
            profileImage = itemView.findViewById(R.id.civ_display_profile_image);
        }
    }
}
