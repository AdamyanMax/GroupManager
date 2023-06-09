package com.chat.group_manager.Requests;

import android.os.Bundle;
import android.util.Log;
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

import com.chat.group_manager.Helpers.FirebaseManager;
import com.chat.group_manager.Helpers.FirebaseDatabaseReferences;
import com.chat.group_manager.Helpers.OperationCallback;
import com.chat.group_manager.Helpers.ProgressBar.LinearProgressBarHandler;
import com.chat.group_manager.Module.Contacts;
import com.chat.group_manager.R;
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

public class RequestsFragment extends Fragment {
    private static final String TAG = "RequestsFragment";
    private final FirebaseDatabaseReferences firebaseDatabaseReferences = new FirebaseDatabaseReferences();
    private final FirebaseManager firebaseManager = new FirebaseManager();
    private RecyclerView rvRequestsList;
    private String currentUserID;
    private FirebaseRecyclerAdapter<Contacts, RequestViewHolder> adapter;
    private LinearProgressBarHandler linearProgressBarHandler;

    public RequestsFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_requests, container, false);
        linearProgressBarHandler = new LinearProgressBarHandler(view);
        rvRequestsList = view.findViewById(R.id.rv_chat_requests);
        rvRequestsList.setLayoutManager(new LinearLayoutManager(getContext()));

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        currentUserID = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        linearProgressBarHandler.show();

        FirebaseRecyclerOptions<Contacts> options = new FirebaseRecyclerOptions.Builder<Contacts>().setQuery(firebaseDatabaseReferences.getChatRequestsRef().child(currentUserID), Contacts.class).build();
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
                                holder.btnCancel.setVisibility(View.GONE);

                                holder.btnAccept.setVisibility(View.VISIBLE);
                                holder.btnDecline.setVisibility(View.VISIBLE);
                                assert list_user_id != null;
                                firebaseDatabaseReferences.getUsersRef().child(list_user_id).addValueEventListener(new ValueEventListener() {
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

                                        holder.btnAccept.setOnClickListener(v -> firebaseManager.acceptChatRequest(currentUserID, list_user_id, new OperationCallback() {
                                            @Override
                                            public void onSuccess() {
                                                Toast.makeText(getContext(), R.string.contact_saved, Toast.LENGTH_SHORT).show();
                                            }

                                            @Override
                                            public void onFailure(Exception error) {
                                                // Handle the error
                                                Log.e(TAG, "onFailure: " + error);
                                            }
                                        }));

                                        holder.btnDecline.setOnClickListener(v -> firebaseManager.declineChatRequest(currentUserID, list_user_id, new OperationCallback() {
                                            @Override
                                            public void onSuccess() {
                                                Toast.makeText(getContext(), R.string.chat_request_declined, Toast.LENGTH_SHORT).show();
                                            }

                                            @Override
                                            public void onFailure(Exception error) {
                                                // Handle the error
                                                Log.e(TAG, "onFailure: " + error);
                                            }
                                        }));
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        Log.e(TAG, "onCancelled: " + error);
                                    }
                                });
                            } else if (type.equals("sent")) {
                                holder.btnCancel.setVisibility(View.VISIBLE);

                                holder.btnAccept.setVisibility(View.GONE);
                                holder.btnDecline.setVisibility(View.GONE);

                                assert list_user_id != null;
                                firebaseDatabaseReferences.getUsersRef().child(list_user_id).addValueEventListener(new ValueEventListener() {
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

                                        holder.btnCancel.setOnClickListener(v -> firebaseManager.declineChatRequest(currentUserID, list_user_id, new OperationCallback() {
                                            @Override
                                            public void onSuccess() {
                                                Toast.makeText(getContext(), "Chat request canceled", Toast.LENGTH_SHORT).show();
                                            }

                                            @Override
                                            public void onFailure(Exception error) {
                                                // Handle the error
                                                Log.e(TAG, "onFailure: " + error);
                                            }
                                        }));
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        Log.e(TAG, "onCancelled: " + error);
                                    }
                                });
                            }

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "onCancelled: " + error);
                    }
                });
            }

            @Override
            public void onDataChanged() {
                super.onDataChanged();
                linearProgressBarHandler.hide();
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
        Button btnAccept, btnDecline, btnCancel;

        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);

            tvUserName = itemView.findViewById(R.id.tv_display_username);
            tvUserStatus = itemView.findViewById(R.id.tv_display_user_status);
            civProfileImage = itemView.findViewById(R.id.civ_display_profile_image);
            btnAccept = itemView.findViewById(R.id.btn_request_accept);
            btnDecline = itemView.findViewById(R.id.btn_request_decline);
            btnCancel = itemView.findViewById(R.id.btn_request_cancel);
        }
    }
}
