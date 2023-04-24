package com.example.manage.Chats;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.manage.Adapter.MessagesAdapter;
import com.example.manage.R;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {
    private static final int GALLERY_REQUEST_CODE = 1001;

    private final List<Messages> messagesList = new ArrayList<>();
    private PopupWindow popupWindow;
    private String messageReceiverID, messageSenderID;
    private TextView tvUsername, tvUserLastSeen;
    private CircleImageView civProfileImage;
    private ImageButton ibSendMessage, ibSendFile;
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

        toggleButtonsBasedOnEditTextContent(etMessageInput, ibSendFile, ibSendMessage);


        ibSendMessage.setOnClickListener(v -> uploadAndSendTextMessage());

        displayLastSeen();

        ibSendFile.setOnClickListener(this::showExpandableMenu);
    }


    private void toggleButtonsBasedOnEditTextContent(@NonNull EditText etMessageInput, ImageButton ibSendFile, ImageButton ibSendMessage) {
        etMessageInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 0) {
                    // No text in EditText
                    ibSendFile.setVisibility(View.VISIBLE);
                    ibSendMessage.setVisibility(View.GONE);
                } else {
                    // Text is present in EditText
                    ibSendFile.setVisibility(View.GONE);
                    ibSendMessage.setVisibility(View.VISIBLE);
                }
            }
        });
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
        ibSendFile = findViewById(R.id.ib_send_file);

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

    private void showExpandableMenu(@NonNull View view) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View customView = inflater.inflate(R.layout.expandable_menu_layout, null);

        int popupWidth = ViewGroup.LayoutParams.MATCH_PARENT;

        popupWindow = new PopupWindow(customView, popupWidth, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        GridLayout menuLayout = customView.findViewById(R.id.expandable_menu_layout);
        createMenuItem(menuLayout, R.drawable.ic_image, getString(R.string.gallery), v -> {
            popupWindow.dismiss();
            openGallery();
        });

        createMenuItem(menuLayout, R.drawable.ic_file, getString(R.string.file), v -> {

        });
        createMenuItem(menuLayout, R.drawable.ic_cam, getString(R.string.camera), v -> {

        });

        // Find the message_input_container view
        View messageInputContainer = findViewById(R.id.message_input_container);

        // Wait for the message_input_container view to be measured
        messageInputContainer.post(() -> {
            // Get the height of the message_input_container in pixels
            int messageInputContainerHeight = messageInputContainer.getHeight();

            // Convert the height to dp
            float messageInputContainerHeightInDp = messageInputContainerHeight / getResources().getDisplayMetrics().density;

            int extraSpacing = 36;
            // Add a little extra spacing above the message_input_container
            float offsetInDp = messageInputContainerHeightInDp + extraSpacing;

            // Calculate the additional offset in pixels
            int offsetInPx = (int) (offsetInDp * getResources().getDisplayMetrics().density);

            int[] location = new int[2];
            view.getLocationOnScreen(location);

            popupWindow.showAtLocation(view, Gravity.NO_GRAVITY, location[0], location[1] - view.getHeight() - popupWindow.getHeight() - offsetInPx);
        });

        Animation scaleTranslateAnimation = AnimationUtils.loadAnimation(this, R.anim.scale_translate);
        menuLayout.startAnimation(scaleTranslateAnimation);
    }

    private void createMenuItem(GridLayout menuLayout, @DrawableRes int iconRes, String text, View.OnClickListener onClickListener) {
        View menuItemView = LayoutInflater.from(this).inflate(R.layout.menu_attach_item_layout, menuLayout, false);

        ShapeableImageView imageView = menuItemView.findViewById(R.id.menu_item_icon);
        imageView.setImageResource(iconRes);

        TextView textView = menuItemView.findViewById(R.id.menu_item_text);
        textView.setText(text);

        menuItemView.setOnClickListener(onClickListener);

        menuLayout.addView(menuItemView);
    }

    private void displayLastSeen() {
        RootRef.child("Users").child(messageReceiverID).addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child("userState").hasChild("state")) {
                    String state = Objects.requireNonNull(snapshot.child("userState").child("state").getValue()).toString();
                    String date = Objects.requireNonNull(snapshot.child("userState").child("date").getValue()).toString();
                    String time = Objects.requireNonNull(snapshot.child("userState").child("time").getValue()).toString();

                    if (state.equals("online")) {
                        tvUserLastSeen.setText(R.string.online);
                    } else if (state.equals("offline")) {
                        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());

                        try {
                            Date lastSeenDate = dateFormat.parse(date);
                            Date lastSeenTime = timeFormat.parse(time);
                            Date currentDate = new Date();

                            if (lastSeenDate != null && lastSeenTime != null) {
                                long lastSeenTimestamp = lastSeenDate.getTime() + lastSeenTime.getTime();
                                long currentTime = currentDate.getTime();

                                // Calculate the difference in milliseconds
                                long difference = currentTime - lastSeenTimestamp;

                                // Convert the difference to hours
                                long differenceInHours = TimeUnit.MILLISECONDS.toHours(difference);

                                String lastSeen;

                                if (differenceInHours >= 24) {
                                    lastSeen = getResources().getString(R.string.last_seen_on) + " " + date;
                                } else {
                                    lastSeen = getResources().getString(R.string.last_seen_at) + " " + time;
                                }

                                tvUserLastSeen.setText(lastSeen);
                            }

                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
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

                // Notify the adapter about the data set change.
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

    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, GALLERY_REQUEST_CODE);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            Uri fileUri = data.getData();
            uploadAndSendImageMessage(fileUri);
        }
    }

    private void uploadAndSendTextMessage() {
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

    private void uploadAndSendImageMessage(Uri fileUri) {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Sending the image...");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        String messageSenderRef = "Messages/" + messageSenderID + "/" + messageReceiverID;
        String messageReceiverRef = "Messages/" + messageReceiverID + "/" + messageSenderID;

        DatabaseReference userMessageKeyRef = FirebaseDatabase.getInstance().getReference().child("Messages").child(messageSenderID).child(messageReceiverID).push();
        String messagePushID = userMessageKeyRef.getKey();

        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("images");
        StorageReference filePath = storageReference.child(messagePushID + ".jpg");

        filePath.putFile(fileUri).continueWithTask(task -> {
            if (!task.isSuccessful()) {
                throw Objects.requireNonNull(task.getException());
            }
            return filePath.getDownloadUrl();
        }).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Uri downloadUrl = task.getResult();
                assert downloadUrl != null;

                Map<String, Object> messageImageBody = new HashMap<>();
                messageImageBody.put("message", downloadUrl.toString());
                messageImageBody.put("name", fileUri.getLastPathSegment());
                messageImageBody.put("type", "image");
                messageImageBody.put("from", messageSenderID);
                messageImageBody.put("to", messageReceiverID);
                messageImageBody.put("message_id", messagePushID);
                messageImageBody.put("time", new SimpleDateFormat("HH:mm aa", Locale.getDefault()).format(new Date()));
                messageImageBody.put("date", new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(new Date()));

                Map<String, Object> messageBodyDetails = new HashMap<>();
                messageBodyDetails.put(messageSenderRef + "/" + messagePushID, messageImageBody);
                messageBodyDetails.put(messageReceiverRef + "/" + messagePushID, messageImageBody);

                FirebaseDatabase.getInstance().getReference().updateChildren(messageBodyDetails).addOnCompleteListener(task1 -> {
                    if (!(task1.isSuccessful())) {
                        Toast.makeText(ChatActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                    }
                    progressDialog.dismiss();
                    etMessageInput.setText("");
                });
            } else {
                Toast.makeText(ChatActivity.this, "Error: " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }
        });
    }
}