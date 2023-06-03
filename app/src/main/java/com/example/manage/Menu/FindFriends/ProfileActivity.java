package com.example.manage.Menu.FindFriends;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.manage.Helpers.FirebaseManager;
import com.example.manage.Helpers.FirebaseDatabaseReferences;
import com.example.manage.Helpers.OperationCallback;
import com.example.manage.Helpers.ProgressBar.TextProgressBarController;
import com.example.manage.R;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {
    private static final String TAG = "ProfileActivity";

    private static final String CURRENT_STATE_NEW = "new";
    private static final String CURRENT_STATE_REQUEST_SENT = "request_sent";
    private static final String CURRENT_STATE_FRIENDS = "friends";
    private static final String CURRENT_STATE_REQUEST_RECEIVED = "request_received";
    private static final String NODE_REQUEST_TYPE = "request_type";
    private final FirebaseDatabaseReferences firebaseDatabaseReferences = new FirebaseDatabaseReferences();
    private String receiverUserID, currentState, senderUserID;
    private CircleImageView civProfileImage;
    private TextView tvUsername, tvUserStatus;
    private MaterialButton btnSendMessageRequest, btnDeclineRequest;
    private TextProgressBarController progressBarController;
    private FirebaseManager firebaseManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        progressBarController = new TextProgressBarController(this);
        firebaseManager = new FirebaseManager();

        receiverUserID = getIntent().getExtras().get("profile_visit_user_id").toString();
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
        firebaseDatabaseReferences.getUsersRef().child(receiverUserID).addValueEventListener(new ValueEventListener() {
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
        firebaseDatabaseReferences.getChatRequestsRef().child(senderUserID).addValueEventListener(new ValueEventListener() {
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
                    firebaseDatabaseReferences.getContactsRef().child(senderUserID).addListenerForSingleValueEvent(new ValueEventListener() {
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
        firebaseManager.removeContact(senderUserID, receiverUserID, new OperationCallback() {
            @Override
            public void onSuccess() {
                btnSendMessageRequest.setEnabled(true);
                currentState = CURRENT_STATE_NEW;
                btnSendMessageRequest.setText(R.string.send_message);
                btnSendMessageRequest.setIcon(ContextCompat.getDrawable(ProfileActivity.this, R.drawable.ic_send_message));
                btnDeclineRequest.setVisibility(View.INVISIBLE);
                btnDeclineRequest.setEnabled(false);
            }

            @Override
            public void onFailure(Exception error) {
                Log.e(TAG, "Error removing contact: " + error);
            }
        });
    }

    private void acceptChatRequest() {
        progressBarController.show("Accepting request...");

        firebaseManager.acceptChatRequest(senderUserID, receiverUserID, new OperationCallback() {
            @Override
            public void onSuccess() {
                progressBarController.hide();
                onAcceptSuccess();
            }

            @Override
            public void onFailure(Exception error) {
                progressBarController.hide();
                Log.e(TAG, "Error accepting chat request: ", error);
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
        firebaseManager.declineChatRequest(senderUserID, receiverUserID, new OperationCallback() {
            @Override
            public void onSuccess() {
                btnSendMessageRequest.setEnabled(true);
                currentState = CURRENT_STATE_NEW;
                btnSendMessageRequest.setText(R.string.send_message);
                btnSendMessageRequest.setIcon(ContextCompat.getDrawable(ProfileActivity.this, R.drawable.ic_send_message));
                btnDeclineRequest.setVisibility(View.INVISIBLE);
                btnDeclineRequest.setEnabled(false);
            }

            @Override
            public void onFailure(Exception error) {
                // Handle the error
                Log.e(TAG, "Error canceling chat request: ", error);
            }
        });
    }

    private void sendChatRequest() {
        firebaseManager.sendChatRequest(senderUserID, receiverUserID, new OperationCallback() {
            @Override
            public void onSuccess() {
                btnSendMessageRequest.setEnabled(true);
                currentState = CURRENT_STATE_REQUEST_SENT;
                btnSendMessageRequest.setText(R.string.cancel_chat_request);
                btnSendMessageRequest.setIcon(ContextCompat.getDrawable(ProfileActivity.this, R.drawable.ic_cross));
            }

            @Override
            public void onFailure(Exception error) {
                // Handle the error
                Log.e(TAG, "Error sending chat request: ", error);
            }
        });
    }
}
