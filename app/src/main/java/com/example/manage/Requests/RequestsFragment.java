package com.example.manage.Requests;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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

public class RequestsFragment extends Fragment {
    private RecyclerView rvRequestsList;
    private DatabaseReference ChatRequestsRef, UsersRef, ContactsRef;
    private FirebaseAuth mAuth;
    private String currentUserID;
    private View RequestsFragmentView;
    private FirebaseRecyclerAdapter<Contacts, RequestViewHolder> adapter;

    public RequestsFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        RequestsFragmentView = inflater.inflate(R.layout.fragment_requests, container, false);

        rvRequestsList = RequestsFragmentView.findViewById(R.id.rv_chat_requests);
        rvRequestsList.setLayoutManager(new LinearLayoutManager(getContext()));

        mAuth = FirebaseAuth.getInstance();
        currentUserID = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();

        ChatRequestsRef = FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        ContactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts");

        return RequestsFragmentView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Contacts> options = new FirebaseRecyclerOptions.Builder<Contacts>().setQuery(ChatRequestsRef.child(currentUserID), Contacts.class).build();

        adapter = new FirebaseRecyclerAdapter<Contacts, RequestViewHolder>(options) {
            @NonNull
            @Override
            public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_display_layout, parent, false);
                return new RequestViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull RequestViewHolder holder, int position, @NonNull Contacts model) {
                holder.itemView.findViewById(R.id.ll_accept_decline_layout).setVisibility(View.VISIBLE);

                final String list_user_id = getRef(position).getKey();

                DatabaseReference getTypeRef = getRef(position).child("request_type").getRef();

                getTypeRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            String type = Objects.requireNonNull(snapshot.getValue()).toString();
                            if (type.equals("received")) {
                                assert list_user_id != null;
                                UsersRef.child(list_user_id).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if (snapshot.hasChild("image")) {

                                            final String requestProfileImage = Objects.requireNonNull(snapshot.child("image").getValue()).toString();
                                            Picasso.get().load(requestProfileImage).into(holder.civProfileImage);
                                        }
                                        final String requestUsername = Objects.requireNonNull(snapshot.child("name").getValue()).toString();
                                        final String requestUserStatus = Objects.requireNonNull(snapshot.child("status").getValue()).toString();

                                        holder.tvUserName.setText(requestUsername);
                                        holder.tvUserStatus.setText(requestUserStatus);

                                        holder.btnAccept.setOnClickListener(v -> ContactsRef.child(list_user_id).child(currentUserID).child("Contact").setValue("Saved").addOnCompleteListener(task -> {
                                            if (task.isSuccessful()) {
                                                ContactsRef.child(currentUserID).child(list_user_id).child("Contact").setValue("Saved").addOnCompleteListener(task1 -> {
                                                    if (task1.isSuccessful()) {
                                                        ChatRequestsRef.child(list_user_id).child(currentUserID).removeValue().addOnCompleteListener(task2 -> {
                                                            if (task2.isSuccessful()) {
                                                                ChatRequestsRef.child(currentUserID).child(list_user_id).removeValue().addOnCompleteListener(task3 -> Toast.makeText(getContext(), "Contact saved", Toast.LENGTH_SHORT).show());
                                                            }
                                                        });
                                                    }
                                                });
                                            }
                                        }));

                                        holder.btnDecline.setOnClickListener(v -> ChatRequestsRef.child(list_user_id).child(currentUserID).removeValue().addOnCompleteListener(task -> {
                                            if (task.isSuccessful()) {
                                                ChatRequestsRef.child(currentUserID).child(list_user_id).removeValue().addOnCompleteListener(task1 -> Toast.makeText(getContext(), "Contact deleted", Toast.LENGTH_SHORT).show());
                                            }
                                        }));
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        };
        rvRequestsList.setAdapter(adapter);
        adapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    public static class RequestViewHolder extends RecyclerView.ViewHolder {

        TextView tvUserName, tvUserStatus;
        CircleImageView civProfileImage;
        Button btnAccept, btnDecline;

        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);

            tvUserName = itemView.findViewById(R.id.tv_display_username);
            tvUserStatus = itemView.findViewById(R.id.tv_display_user_status);
            civProfileImage = itemView.findViewById(R.id.civ_display_profile_image);
            btnAccept = itemView.findViewById(R.id.btn_request_accept);
            btnDecline = itemView.findViewById(R.id.btn_request_decline);
        }
    }
}