package com.example.manage.Chats;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.manage.Adapter.MessagesAdapter;
import com.example.manage.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {
    private final List<Messages> messagesList = new ArrayList<>();
    private String messageReceiverID, messageSenderID;
    private TextView tvUsername, tvUserLastSeen;
    private CircleImageView civProfileImage;
    private ImageButton ibSendMessage;
    private EditText etMessageInput;
    private DatabaseReference RootRef;
    private MessagesAdapter messagesAdapter;
    private RecyclerView rvUserMessagesList;
    private String saveCurrentTime, saveCurrentDate;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        messageSenderID = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        RootRef = FirebaseDatabase.getInstance().getReference();

        messageReceiverID = getIntent().getExtras().get("visit_user_id").toString();
        String messageReceiverName = getIntent().getExtras().get("visit_username").toString();
        String messageReceiverImage = getIntent().getExtras().get("visit_image").toString();


        initializeControllers();

        tvUsername.setText(messageReceiverName);
        Picasso.get().load(messageReceiverImage).placeholder(R.drawable.user_default_profile_pic).into(civProfileImage);

        ibSendMessage.setOnClickListener(v -> sendMessage());

        displayLastSeen();
    }

    private void initializeControllers() {
        Toolbar chatToolBar = findViewById(R.id.chat_toolbar);
        setSupportActionBar(chatToolBar);

        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);

        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionBarView = layoutInflater.inflate(R.layout.custom_chat_bar, null);
        actionBar.setCustomView(actionBarView, new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        tvUsername = findViewById(R.id.tv_custom_username);
        tvUserLastSeen = findViewById(R.id.tv_custom_last_seen);
        civProfileImage = findViewById(R.id.civ_custom_profile);

        ibSendMessage = findViewById(R.id.ib_send_private_message);

        etMessageInput = findViewById(R.id.et_input_private_message);

        messagesAdapter = new MessagesAdapter(messagesList);
        rvUserMessagesList = findViewById(R.id.rv_private_chat_list_of_messages);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        rvUserMessagesList.setLayoutManager(linearLayoutManager);
        rvUserMessagesList.setAdapter(messagesAdapter);

        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat currentDate = new SimpleDateFormat("MM dd yyyy", Locale.getDefault());
        saveCurrentDate = currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        saveCurrentTime = currentTime.format(calendar.getTime());
    }

    private void displayLastSeen() {
        RootRef.child("Users").child(messageReceiverID)
                .addValueEventListener(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.child("userState").hasChild("state")) {
                            String state = Objects.requireNonNull(snapshot.child("userState").child("state").getValue()).toString();
//                            String date = Objects.requireNonNull(snapshot.child("userState").child("date").getValue()).toString();
                            String time = Objects.requireNonNull(snapshot.child("userState").child("time").getValue()).toString();

                            if (state.equals("online")) {
                                tvUserLastSeen.setText(R.string.online);
                            } else if (state.equals("offline")) {

                                // TODO: Add a check if the user hasn't been online for more than 24 hours than show the date
                                String lastSeen = getResources().getString(R.string.last_seen_at) + " " + time;
                                tvUserLastSeen.setText(lastSeen);
                            }

                        } else {
                            tvUserLastSeen.setText(R.string.offline);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }


    @Override
    protected void onStart() {
        super.onStart();
        RootRef.child("Messages").child(messageSenderID).child(messageReceiverID).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Messages messages = snapshot.getValue(Messages.class);

                messagesList.add(messages);

                messagesAdapter.notifyDataSetChanged();

                rvUserMessagesList.smoothScrollToPosition(Objects.requireNonNull(rvUserMessagesList.getAdapter()).getItemCount());
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        messagesList.clear();
    }

    @Override
    protected void onPause() {
        super.onPause();
        messagesList.clear();
    }

    private void sendMessage() {
        String messageText = etMessageInput.getText().toString();

        if (!(TextUtils.isEmpty(messageText))) {
            String messageSenderRef = "Messages/" + messageSenderID + "/" + messageReceiverID;
            String messageReceiverRef = "Messages/" + messageReceiverID + "/" + messageSenderID;

            DatabaseReference userMessageKeyRef = RootRef.child("Messages").child(messageSenderID).child(messageSenderID).push();

            String messagePushID = userMessageKeyRef.getKey();

            Map<String, Object> messageTextBody = new HashMap<>();
            messageTextBody.put("message", messageText);
            messageTextBody.put("type", "text");
            messageTextBody.put("from", messageSenderID);
            messageTextBody.put("to", messageReceiverID);
            messageTextBody.put("message_id", messagePushID);
            messageTextBody.put("time", saveCurrentTime);
            messageTextBody.put("date", saveCurrentDate);

            Map<String, Object> messageBodyDetails = new HashMap<>();
            messageBodyDetails.put(messageSenderRef + "/" + messagePushID, messageTextBody);
            messageBodyDetails.put(messageReceiverRef + "/" + messagePushID, messageTextBody);

            RootRef.updateChildren(messageBodyDetails).addOnCompleteListener(task -> {
                if (!(task.isSuccessful())) {
                    Toast.makeText(ChatActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                }
                etMessageInput.setText("");
            });
        }
    }
}