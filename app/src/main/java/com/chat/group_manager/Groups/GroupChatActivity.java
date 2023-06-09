package com.chat.group_manager.Groups;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.chat.group_manager.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Objects;

public class GroupChatActivity extends AppCompatActivity {
    private ImageButton ibSendMessage;
    private EditText etUserMessage;
    private ScrollView mScrollView;
    private TextView tvDisplayMessage;
    private DatabaseReference UsersRef, GroupNameRef;
    private String currentGroupName, currentUserID, currentUsername;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);
        currentGroupName = getIntent().getExtras().get("groupName").toString();

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        currentUserID = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        GroupNameRef = FirebaseDatabase.getInstance().getReference()
                .child("Groups").child(currentGroupName);

        initializeFields();

        getUserInfo();

        ibSendMessage.setOnClickListener(v -> {
            saveMessageInfoToDatabase();
            etUserMessage.setText("");
            mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        GroupNameRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (snapshot.exists()) {
                    DisplayMessages(snapshot);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (snapshot.exists()) {
                    DisplayMessages(snapshot);
                }
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


    private void initializeFields() {
        Toolbar mToolbar = findViewById(R.id.group_chat_bar_layout);
        setSupportActionBar(mToolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle(currentGroupName);

        ibSendMessage = findViewById(R.id.ib_send_private_message);
        etUserMessage = findViewById(R.id.et_input_private_message);
        mScrollView = findViewById(R.id.scroll_view);
        tvDisplayMessage = findViewById(R.id.tv_group_chat_text_display);
    }

    private void getUserInfo() {
        UsersRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    currentUsername = Objects.requireNonNull(snapshot.child("name")
                            .getValue()).toString();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void saveMessageInfoToDatabase() {
        String message = etUserMessage.getText().toString();
        String messageKey = GroupNameRef.push().getKey();

        // TODO: Reminder in case you want to add a voice message system.
        if (!(TextUtils.isEmpty(message))) {
            Calendar callForDate = Calendar.getInstance();
            DateFormat dateFormat = DateFormat.getDateInstance();
            String currentDate = dateFormat.format(callForDate.getTime());

            Calendar callForTime = Calendar.getInstance();
            DateFormat timeFormat = DateFormat.getTimeInstance(DateFormat.SHORT);
            String currentTime = timeFormat.format(callForTime.getTime());

            HashMap<String, Object> groupMessageKey = new HashMap<>();
            GroupNameRef.updateChildren(groupMessageKey);

            assert messageKey != null;
            DatabaseReference groupMessageKeyRef = GroupNameRef.child(messageKey);

            HashMap<String, Object> messageInfoMap = new HashMap<>();
            messageInfoMap.put("name", currentUsername);
            messageInfoMap.put("message", message);
            messageInfoMap.put("date", currentDate);
            messageInfoMap.put("time", currentTime);
            groupMessageKeyRef.updateChildren(messageInfoMap);
        }
    }


    private void DisplayMessages(@NonNull DataSnapshot snapshot) {

        for (DataSnapshot messageSnapshot : snapshot.getChildren()) {
//            String chatDate = messageSnapshot.getKey();
            String chatMessage = messageSnapshot.child("message").getValue(String.class);
            String chatName = messageSnapshot.child("name").getValue(String.class);
            String chatTime = messageSnapshot.child("time").getValue(String.class);

            tvDisplayMessage.append(chatName + " :\n" + chatMessage + "\n"
                    + chatTime + "\n\n");

            mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
        }
    }

}