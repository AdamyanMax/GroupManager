package com.example.manage.Menu.FindFriends;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.manage.R;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {
    private static final String TAG = "ProfileActivity";

    private static final String CURRENT_STATE_NEW = "new";
    private static final String CURRENT_STATE_REQUEST_SENT = "request_sent";
    private static final String CURRENT_STATE_FRIENDS = "friends";
    private static final String CURRENT_STATE_REQUEST_RECEIVED = "request_received";

    private String receiverUserID, currentState, senderUserID;

    private CircleImageView civProfileImage;
    private TextView tvUsername, tvUserStatus;
    private Button btnSendMessageRequest, btnDeclineRequest;

    private DatabaseReference UserRef, ChatRequestRef, ContactsRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        UserRef = FirebaseDatabase.getInstance().getReference().child("Users");
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        ChatRequestRef = FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        ContactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts");


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
        ChatRequestRef.child(senderUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.hasChild(receiverUserID)) {
                    String request_type = Objects.requireNonNull(snapshot.child(receiverUserID).child("request_type").getValue()).toString();
                    if (request_type.equals("sent")) {
                        currentState = CURRENT_STATE_REQUEST_SENT;
                        btnSendMessageRequest.setText(R.string.cancel_chat_request);
                        btnSendMessageRequest.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_cross, 0, 0, 0);
                    } else if (request_type.equals("received")) {
                        currentState = CURRENT_STATE_REQUEST_RECEIVED;
                        btnSendMessageRequest.setText(R.string.accept_chat_request);
                        btnSendMessageRequest.setBackgroundResource(R.drawable.profile_buttons);
                        btnSendMessageRequest.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_check, 0, 0, 0);

                        btnDeclineRequest.setVisibility(View.VISIBLE);
                        btnDeclineRequest.setEnabled(true);

                        btnDeclineRequest.setOnClickListener(v -> cancelChatRequest());
                    }
                } else {
                    ContactsRef.child(senderUserID)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.hasChild(receiverUserID)) {
                                        currentState = CURRENT_STATE_FRIENDS;
                                        btnSendMessageRequest.setText(R.string.remove_this_contact);
                                        btnSendMessageRequest.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_ban, 0, 0, 0);
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
        ContactsRef.child(senderUserID).child(receiverUserID).removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                ContactsRef.child(receiverUserID).child(senderUserID).removeValue().addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()) {
                        btnSendMessageRequest.setEnabled(true);
                        currentState = CURRENT_STATE_NEW;
                        btnSendMessageRequest.setText(R.string.send_message);
                        btnSendMessageRequest.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_send_message, 0, 0, 0);
                        btnSendMessageRequest.setBackgroundResource(R.drawable.profile_buttons_end);

                        btnDeclineRequest.setVisibility(View.INVISIBLE);
                        btnDeclineRequest.setEnabled(false);

                    }
                });
            }
        });
    }

    //    private void acceptChatRequest() {
//        ContactsRef.child(senderUserID).child(receiverUserID)
//                .child("Contacts").setValue("Saved")
//                .addOnCompleteListener(task -> {
//                    if (task.isSuccessful()) {
//                        ContactsRef.child(receiverUserID).child(senderUserID)
//                                .child("Contacts").setValue("Saved")
//                                .addOnCompleteListener(task1 -> {
//                                    if (task.isSuccessful()) {
//                                        ChatRequestRef.child(senderUserID).child(receiverUserID)
//                                                .removeValue()
//                                                .addOnCompleteListener(task2 -> {
//                                                    if (task2.isSuccessful()) {
//                                                        ChatRequestRef.child(receiverUserID).child(senderUserID)
//                                                                .removeValue()
//                                                                .addOnCompleteListener(task3 -> {
//                                                                    btnSendMessageRequest.setEnabled(true);
//                                                                    currentState = CURRENT_STATE_FRIENDS;
//                                                                    btnSendMessageRequest.setText(R.string.remove_this_contact);
//                                                                    btnSendMessageRequest.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_ban, 0, 0, 0);
//                                                                    btnSendMessageRequest.setBackgroundResource(R.drawable.profile_buttons_end);
//
//                                                                    btnDeclineRequest.setVisibility(View.INVISIBLE);
//                                                                    btnDeclineRequest.setEnabled(false);
//                                                                });
//                                                    }
//                                                });
//                                    }
//                                });
//                    }
//                });
//    }
    private void acceptChatRequest() {
        List<Task<Void>> tasks = new ArrayList<>();

        Task<Void> task1 = ContactsRef.child(senderUserID).child(receiverUserID)
                .child("Contacts").setValue("Saved")
                .addOnFailureListener(e -> {
                    // Handle error
                });
        tasks.add(task1);

        Task<Void> task2 = ContactsRef.child(receiverUserID).child(senderUserID)
                .child("Contacts").setValue("Saved")
                .addOnFailureListener(e -> {
                    // Handle error
                });
        tasks.add(task2);

        Task<Void> task3 = ChatRequestRef.child(senderUserID).child(receiverUserID)
                .removeValue()
                .addOnFailureListener(e -> {
                    // Handle error
                });
        tasks.add(task3);

        Task<Void> task4 = ChatRequestRef.child(receiverUserID).child(senderUserID)
                .removeValue()
                .addOnFailureListener(e -> {
                    // Handle error
                });
        tasks.add(task4);

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Accepting request...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        Tasks.whenAllComplete(tasks).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // All tasks were successful, hide the loading bar
                progressDialog.dismiss();

                // Update UI
                onAcceptSuccess();
            } else {
                // At least one task failed, hide the loading bar and show an error message
                progressDialog.dismiss();
                Log.e(TAG, "Error accepting chat request: ", task.getException());
            }
        });
    }

    private void onAcceptSuccess() {
        btnSendMessageRequest.setEnabled(true);
        currentState = CURRENT_STATE_FRIENDS;
        btnSendMessageRequest.setText(R.string.remove_this_contact);
        btnSendMessageRequest.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_ban, 0, 0, 0);
        btnSendMessageRequest.setBackgroundResource(R.drawable.profile_buttons_end);

        btnDeclineRequest.setVisibility(View.INVISIBLE);
        btnDeclineRequest.setEnabled(false);
    }

    private void cancelChatRequest() {
        ChatRequestRef.child(senderUserID).child(receiverUserID).removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                ChatRequestRef.child(receiverUserID).child(senderUserID).removeValue().addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()) {
                        btnSendMessageRequest.setEnabled(true);
                        currentState = CURRENT_STATE_NEW;
                        btnSendMessageRequest.setText(R.string.send_message);
                        btnSendMessageRequest.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_send_message, 0, 0, 0);
                        btnSendMessageRequest.setBackgroundResource(R.drawable.profile_buttons_end);

                        btnDeclineRequest.setVisibility(View.INVISIBLE);
                        btnDeclineRequest.setEnabled(false);

                    }
                });
            }
        });
    }

    private void sendChatRequest() {
        ChatRequestRef.child(senderUserID).child(receiverUserID).child("request_type").setValue("sent").addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                ChatRequestRef.child(receiverUserID).child(senderUserID).child("request_type").setValue("received").addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()) {
                        btnSendMessageRequest.setEnabled(true);
                        currentState = CURRENT_STATE_REQUEST_SENT;
                        btnSendMessageRequest.setText(R.string.cancel_chat_request);
                        btnSendMessageRequest.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_cross, 0, 0, 0);
                    }
                });
            }
        });
    }
}