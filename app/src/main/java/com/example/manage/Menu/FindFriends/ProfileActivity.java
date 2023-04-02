package com.example.manage.Menu.FindFriends;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.manage.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private String receiverUserID, currentState, senderUserID;

    private CircleImageView civProfileImage;
    private TextView tvUsername, tvUserStatus;
    private Button btnSendMessageRequest;

    private DatabaseReference UserRef, ChatRequestRef;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        UserRef = FirebaseDatabase.getInstance().getReference().child("Users");
        mAuth = FirebaseAuth.getInstance();
        ChatRequestRef = FirebaseDatabase.getInstance().getReference().child("Chat Requests");

        receiverUserID = getIntent().getExtras().get("visit_user_id").toString();
        currentState = "new";
        senderUserID = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();

        civProfileImage = findViewById(R.id.civ_profile_user_image);
        tvUsername = findViewById(R.id.tv_profile_username);
        tvUserStatus = findViewById(R.id.tv_profile_user_status);
        btnSendMessageRequest = findViewById(R.id.btn_send_message_request);

        retrieveUserInfo();
    }

    private void retrieveUserInfo() {
        UserRef.child(receiverUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if ((snapshot.exists()) && (snapshot.hasChild("image"))) {
                    String userImage = Objects.requireNonNull(snapshot.child("image").getValue()).toString();
                    String userName = Objects.requireNonNull(snapshot.child("name").getValue()).toString();
                    String userStatus = Objects.requireNonNull(snapshot.child("status").getValue()).toString();

                    Picasso.get().load(userImage).placeholder(R.drawable.user_default_profile_pic).into(civProfileImage);
                    tvUsername.setText(userName);
                    tvUserStatus.setText(userStatus);


                    manageChatRequests();

                } else if (snapshot.exists()) {
                    String userName = Objects.requireNonNull(snapshot.child("name").getValue()).toString();
                    String userStatus = Objects.requireNonNull(snapshot.child("status").getValue()).toString();

                    tvUsername.setText(userName);
                    tvUserStatus.setText(userStatus);

                    manageChatRequests();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void manageChatRequests() {
        ChatRequestRef.child(senderUserID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.hasChild(receiverUserID)) {
                            String request_type = Objects.requireNonNull(snapshot.child(receiverUserID)
                                    .child("request_type").getValue()).toString();
                            if (request_type.equals("sent")) {
                                currentState = "request_sent";
                                btnSendMessageRequest.setText(R.string.cancel_chat_request);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        if (!senderUserID.equals(receiverUserID)) {
            btnSendMessageRequest.setOnClickListener(v -> {
                btnSendMessageRequest.setEnabled(false);

                if (currentState.equals("new")) {
                    sendChatRequest();
                }
                if (currentState.equals("request_sent")) {
                    cancelChatRequest();
                }
            });
        } else {
            // Make it so the user can't send message to himself
            btnSendMessageRequest.setVisibility(View.INVISIBLE);
        }
    }

    private void cancelChatRequest() {
        ChatRequestRef.child(senderUserID).child(receiverUserID)
                .removeValue()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        ChatRequestRef.child(receiverUserID).child(senderUserID)
                                .removeValue()
                                .addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful()) {
                                        btnSendMessageRequest.setEnabled(true);
                                        currentState = "new";
                                        btnSendMessageRequest.setText(R.string.send_message);
                                    }
                                });
                    }
                });
    }

    private void sendChatRequest() {
        ChatRequestRef.child(senderUserID).child(receiverUserID)
                .child("request_type").setValue("sent")
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        ChatRequestRef.child(receiverUserID).child(senderUserID)
                                .child("request_type").setValue("received")
                                .addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful()) {
                                        btnSendMessageRequest.setEnabled(true);
                                        currentState = "request_sent";
                                        btnSendMessageRequest.setText(R.string.cancel_chat_request);
                                    }
                                });
                    }
                });
    }
}