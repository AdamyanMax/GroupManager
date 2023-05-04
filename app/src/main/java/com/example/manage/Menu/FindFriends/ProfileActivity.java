package com.example.manage.Menu.FindFriends;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.manage.Helpers.FirebaseUtil;
import com.example.manage.Helpers.ProgressBarManager;
import com.example.manage.R;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {
    private static final String TAG = "ProfileActivity";

    private static final String CURRENT_STATE_NEW = "new";
    private static final String CURRENT_STATE_REQUEST_SENT = "request_sent";
    private static final String CURRENT_STATE_FRIENDS = "friends";
    private static final String CURRENT_STATE_REQUEST_RECEIVED = "request_received";
    private static final String NODE_REQUEST_TYPE = "request_type";
    private final FirebaseUtil firebaseUtil = new FirebaseUtil();
    private String receiverUserID, currentState, senderUserID;
    private CircleImageView civProfileImage;
    private TextView tvUsername, tvUserStatus;
    private MaterialButton btnSendMessageRequest, btnDeclineRequest;
    private ProgressBarManager progressBarManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        progressBarManager = new ProgressBarManager(this);

        receiverUserID = getIntent().getExtras().get("visit_user_id").toString();
        currentState = CURRENT_STATE_NEW;
        senderUserID = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();

        civProfileImage = findViewById(R.id.civ_profile_user_image);
        tvUsername = findViewById(R.id.tv_profile_username);
        tvUserStatus = findViewById(R.id.tv_profile_user_status);
        btnSendMessageRequest = findViewById(R.id.btn_send_message_request);
        btnDeclineRequest = findViewById(R.id.btn_decline_message_request);

        retrieveUserInfo();
    }

    private void retrieveUserInfo() {
        firebaseUtil.getUsersRef().child(receiverUserID).addValueEventListener(new ValueEventListener() {
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
        firebaseUtil.getChatRequestsRef().child(senderUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.hasChild(receiverUserID)) {
                    String request_type = Objects.requireNonNull(snapshot.child(receiverUserID).child(NODE_REQUEST_TYPE).getValue()).toString();
                    if (request_type.equals("sent")) {
                        currentState = CURRENT_STATE_REQUEST_SENT;
                        btnSendMessageRequest.setText(R.string.cancel_chat_request);
                        btnSendMessageRequest.setIcon(ContextCompat.getDrawable(ProfileActivity.this, R.drawable.ic_cross));
                    } else if (request_type.equals("received")) {
                        currentState = CURRENT_STATE_REQUEST_RECEIVED;
                        btnSendMessageRequest.setText(R.string.accept_chat_request);
                        btnSendMessageRequest.setIcon(ContextCompat.getDrawable(ProfileActivity.this, R.drawable.ic_check));

                        btnDeclineRequest.setVisibility(View.VISIBLE);
                        btnDeclineRequest.setEnabled(true);

                        btnDeclineRequest.setOnClickListener(v -> cancelChatRequest());
                    }
                } else {
                    firebaseUtil.getContactsRef().child(senderUserID).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.hasChild(receiverUserID)) {
                                currentState = CURRENT_STATE_FRIENDS;
                                btnSendMessageRequest.setText(R.string.remove_this_contact);
                                btnSendMessageRequest.setIcon(ContextCompat.getDrawable(ProfileActivity.this, R.drawable.ic_ban));
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        if (!senderUserID.equals(receiverUserID)) {
            btnSendMessageRequest.setOnClickListener(v -> {
                btnSendMessageRequest.setEnabled(false);

                if (currentState.equals(CURRENT_STATE_NEW)) {
                    sendChatRequest();
                }
                if (currentState.equals(CURRENT_STATE_REQUEST_SENT)) {
                    cancelChatRequest();
                }
                if (currentState.equals(CURRENT_STATE_REQUEST_RECEIVED)) {
                    acceptChatRequest();
                }
                if (currentState.equals(CURRENT_STATE_FRIENDS)) {
                    removeSpecificContact();
                }
            });
        } else {
            // Make it so the user can't send message to himself
            btnSendMessageRequest.setVisibility(View.INVISIBLE);
        }
    }

    private void removeSpecificContact() {
        firebaseUtil.getContactsRef().child(senderUserID).child(receiverUserID).removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                firebaseUtil.getContactsRef().child(receiverUserID).child(senderUserID).removeValue().addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()) {
                        btnSendMessageRequest.setEnabled(true);
                        currentState = CURRENT_STATE_NEW;
                        btnSendMessageRequest.setText(R.string.send_message);
                        btnSendMessageRequest.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_send_message));
//                        btnSendMessageRequest.setBackgroundResource(R.drawable.bg_profile_buttons_end);

                        btnDeclineRequest.setVisibility(View.INVISIBLE);
                        btnDeclineRequest.setEnabled(false);

                    }
                });
            }
        });
    }

    private void acceptChatRequest() {
        List<Task<Void>> tasks = new ArrayList<>();

        Task<Void> task1 = firebaseUtil.getContactsRef().child(senderUserID).child(receiverUserID).child("Contacts").setValue("Saved").addOnFailureListener(e -> {
            // Handle error
        });
        tasks.add(task1);

        Task<Void> task2 = firebaseUtil.getContactsRef().child(receiverUserID).child(senderUserID).child("Contacts").setValue("Saved").addOnFailureListener(e -> {
            // Handle error
        });
        tasks.add(task2);

        Task<Void> task3 = firebaseUtil.getChatRequestsRef().child(senderUserID).child(receiverUserID).removeValue().addOnFailureListener(e -> {
            // Handle error
        });
        tasks.add(task3);

        Task<Void> task4 = firebaseUtil.getChatRequestsRef().child(receiverUserID).child(senderUserID).removeValue().addOnFailureListener(e -> {
            // Handle error
        });
        tasks.add(task4);

        progressBarManager.show("Accepting request...");

        Tasks.whenAllComplete(tasks).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // All tasks were successful, hide the loading bar
                progressBarManager.hide();

                // Update UI
                onAcceptSuccess();
            } else {
                // At least one task failed, hide the loading bar and show an error message
                progressBarManager.hide();
                Log.e(TAG, "Error accepting chat request: ", task.getException());
            }
        });
    }

    private void onAcceptSuccess() {
        btnSendMessageRequest.setEnabled(true);
        currentState = CURRENT_STATE_FRIENDS;
        btnSendMessageRequest.setText(R.string.remove_this_contact);
        btnSendMessageRequest.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_ban));

        btnDeclineRequest.setVisibility(View.INVISIBLE);
        btnDeclineRequest.setEnabled(false);
    }

    private void cancelChatRequest() {
        firebaseUtil.getChatRequestsRef().child(senderUserID).child(receiverUserID).removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                firebaseUtil.getChatRequestsRef().child(receiverUserID).child(senderUserID).removeValue().addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()) {
                        btnSendMessageRequest.setEnabled(true);
                        currentState = CURRENT_STATE_NEW;
                        btnSendMessageRequest.setText(R.string.send_message);
                        btnSendMessageRequest.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_send_message));

                        btnDeclineRequest.setVisibility(View.INVISIBLE);
                        btnDeclineRequest.setEnabled(false);

                    }
                });
            }
        });
    }

    private void sendChatRequest() {
        firebaseUtil.getChatRequestsRef().child(senderUserID).child(receiverUserID).child(NODE_REQUEST_TYPE).setValue("sent").addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                firebaseUtil.getChatRequestsRef().child(receiverUserID).child(senderUserID).child(NODE_REQUEST_TYPE).setValue("received").addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()) {
                        HashMap<String, String> chatNotificationMap = new HashMap<>();
                        chatNotificationMap.put("from", senderUserID);
                        chatNotificationMap.put("type", "request");

                        firebaseUtil.getNotificationsRef().child(receiverUserID).push().setValue(chatNotificationMap).addOnCompleteListener(task2 -> {
                            if (task2.isSuccessful()) {
                                btnSendMessageRequest.setEnabled(true);
                                currentState = CURRENT_STATE_REQUEST_SENT;
                                btnSendMessageRequest.setText(R.string.cancel_chat_request);
                                btnSendMessageRequest.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_cross));

                            }
                        });


                    }
                });
            }
        });
    }
}