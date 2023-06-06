package com.example.manage.Adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.manage.Menu.FindFriends.ProfileActivity;
import com.example.manage.Module.Contacts;
import com.example.manage.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


public class FindFriendsAdapter extends FirebaseRecyclerAdapter<Contacts, FindFriendsAdapter.FindFriendsViewHolder> {
    private String filterType;

    public FindFriendsAdapter(@NonNull FirebaseRecyclerOptions<Contacts> options, String filterType) {
        super(options);
        this.filterType = filterType;
    }

    public void changeFilterType(String newFilterType) {
        this.filterType = newFilterType;
        notifyItemRangeChanged(0, getItemCount());
    }

    @NonNull
    @Override
    public FindFriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_display_layout, parent, false);
        return new FindFriendsViewHolder(view);
    }

    @Override
    protected void onBindViewHolder(@NonNull FindFriendsViewHolder holder, int position, @NonNull Contacts model) {
        if (filterType.equals("username")) {
            holder.name.setText(model.getUsername());
        } else {
            holder.name.setText(model.getName());
        }
        holder.status.setText(model.getStatus());
        Picasso.get().load(model.getImage()).placeholder(R.drawable.user_default_profile_pic).error(R.drawable.user_default_profile_pic).into(holder.profileImage);

        String visit_user_id = getRef(position).getKey();
        holder.itemView.setTag(visit_user_id);
        holder.itemView.setOnClickListener(v -> {
            String profile_visit_user_id = (String) v.getTag();
            Intent profileIntent = new Intent(v.getContext(), ProfileActivity.class);
            profileIntent.putExtra("profile_visit_user_id", profile_visit_user_id);
            v.getContext().startActivity(profileIntent);
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
