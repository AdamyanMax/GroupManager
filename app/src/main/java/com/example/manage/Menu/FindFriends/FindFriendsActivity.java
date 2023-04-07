package com.example.manage.Menu.FindFriends;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.manage.Contacts.Contacts;
import com.example.manage.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class FindFriendsActivity extends AppCompatActivity {

    private RecyclerView rvFindFriends;
    private DatabaseReference UsersReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_firends);

        UsersReference = FirebaseDatabase.getInstance().getReference().child("Users");

        rvFindFriends = findViewById(R.id.rv_find_friends);
        rvFindFriends.setLayoutManager(new LinearLayoutManager(this));

        Toolbar mToolbar = findViewById(R.id.find_friends_toolbar);
        setSupportActionBar(mToolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Find Friends");


    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Contacts> options = new FirebaseRecyclerOptions.Builder<Contacts>().setQuery(UsersReference, Contacts.class).build();

        FirebaseRecyclerAdapter<Contacts, FindFriendsViewHolder> adapter = new FirebaseRecyclerAdapter<Contacts, FindFriendsViewHolder>(options) {

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

                    Intent profileIntent = new Intent(FindFriendsActivity.this, ProfileActivity.class);
                    profileIntent.putExtra("visit_user_id", visit_user_id);
                    startActivity(profileIntent);
                });
            }
        };

        rvFindFriends.setAdapter(adapter);

        adapter.startListening();
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