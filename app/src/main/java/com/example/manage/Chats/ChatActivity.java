package com.example.manage.Chats;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.MenuInflater;
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
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.slidingpanelayout.widget.SlidingPaneLayout;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.example.manage.Adapter.MessagesAdapter;
import com.example.manage.Chats.Profile.FilesFragment;
import com.example.manage.Chats.Profile.ImagesFragment;
import com.example.manage.Helpers.FirebaseUtil;
import com.example.manage.Helpers.NavigateUtil;
import com.example.manage.Helpers.ProgressBar.TextProgressBarController;
import com.example.manage.Module.Messages;
import com.example.manage.R;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
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
    // TODO: Remember user's scroll position and save it
    private static final int GALLERY_REQUEST_CODE = 1001;
    private static final int FILE_REQUEST_CODE = 2;
    private static final String MESSAGES = "Messages/";
    private static final String DATE_FORMAT = "MMM dd, yyyy";
    private static final String TIME_FORMAT = "HH:mm aa";
    private static final int BYTES_IN_KB = 1024;


    private final List<Messages> messagesList = new ArrayList<>();
    private final FirebaseUtil firebaseUtil = new FirebaseUtil();
    private TextProgressBarController progressBarController;
    private PopupWindow popupWindow;
    private String messageReceiverID, messageSenderID, saveCurrentTime, saveCurrentDate;
    private TextView tvUsername, tvUserLastSeen, tvChatProfileUsername, tvChatProfileUserStatus;
    private CircleImageView civProfileImage, civChatProfileUserImage;
    private ImageButton ibSendMessage, ibSendFile;
    private EditText etMessageInput;
    private MessagesAdapter messagesAdapter;
    private RecyclerView rvUserMessagesList;
    private DatabaseReference userMessageKeyRef;
    private ConstraintLayout slidingPane;
    private SlidingPaneLayout slidingPaneLayout;
    private ChildEventListener childEventListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        messageSenderID = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();

        messageReceiverID = getIntent().getExtras().get("visit_user_id").toString();
        String messageReceiverName = getIntent().getExtras().get("visit_username").toString();
        String messageReceiverStatus = getIntent().getExtras().get("visit_user_status").toString();
        String messageReceiverImage = getIntent().getExtras().get("visit_image").toString();

        initializeControllers();

        tvUsername.setText(messageReceiverName);
        Picasso.get().load(messageReceiverImage).placeholder(R.drawable.user_default_profile_pic).into(civProfileImage);

        tvChatProfileUsername.setText(messageReceiverName);
        tvChatProfileUserStatus.setText(messageReceiverStatus);
        Picasso.get().load(messageReceiverImage).placeholder(R.drawable.user_default_profile_pic).into(civChatProfileUserImage);

        toggleButtonsBasedOnEditTextContent(etMessageInput, ibSendFile, ibSendMessage);

        displayLastSeen();

        ImageButton moreOptionsButton = findViewById(R.id.ib_custom_more_options);
        moreOptionsButton.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(ChatActivity.this, v);
            MenuInflater inflater = popup.getMenuInflater();
            inflater.inflate(R.menu.private_chat_menu, popup.getMenu());

            popup.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                if (id == R.id.delete_user) {
                    // Handle user deletion here
                    new MaterialAlertDialogBuilder(this)
                            .setTitle(R.string.delete_contact_alert_dialog)
                            .setMessage(getString(R.string.are_you_sure_you_want_to_delete) + messageReceiverName + getString(R.string.from_the_chat_list))
                            .setNegativeButton(R.string.cancel, (dialogInterface, i) -> {
                                // Dismiss the dialog
                                dialogInterface.dismiss();
                            })
                            .setPositiveButton(R.string.delete, (dialogInterface, i) -> {
                                //
                                NavigateUtil.toMainActivity(getApplicationContext());
                                // Remove the contact
                                removeSpecificContact();
                            })
                            .show();
                    return true;
                } else if (id == R.id.clear_chat) {
                    // Handle chat clearing here
                    new MaterialAlertDialogBuilder(ChatActivity.this)
                            .setTitle(R.string.clear_chat_history)
                            .setMessage(R.string.clear_chat_history_confirmation)
                            .setNegativeButton(R.string.cancel, (dialogInterface, i) -> {
                                // Dismiss the dialog
                                dialogInterface.dismiss();
                            })
                            .setPositiveButton(R.string.clear, (dialogInterface, i) -> {
                                // Clear chat history
                                clearChatHistory(messageSenderID, messageReceiverID);
                            })
                            .show();

                    popup.dismiss();

                    return true;
                }

                return false;
            });

            popup.show();
        });

        ibSendFile.setOnClickListener(this::showExpandableMenu);
        ibSendMessage.setOnClickListener(v -> uploadAndSendTextMessage());

        civProfileImage.setOnClickListener(v -> {
            if (!slidingPaneLayout.isOpen()) {
                slidingPaneLayout.openPane();
            }
        });

        configureSlidingPane();

        ViewPager2 viewPager = findViewById(R.id.view_pager);
        TabLayout tabLayout = findViewById(R.id.tab_layout);

        setupTabLayoutForProfile(viewPager, tabLayout);
    }

    private void initializeControllers() {
        Toolbar chatToolBar = findViewById(R.id.chat_toolbar);
        setSupportActionBar(chatToolBar);

        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);

        // Initialize the toolbar at the top
        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionBarView = layoutInflater.inflate(R.layout.custom_chat_bar, null);
        actionBar.setCustomView(actionBarView, new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        tvUsername = findViewById(R.id.tv_custom_username);
        tvUserLastSeen = findViewById(R.id.tv_custom_last_seen);
        civProfileImage = findViewById(R.id.civ_custom_profile);

        civChatProfileUserImage = findViewById(R.id.civ_chat_profile_user_image);
        tvChatProfileUsername = findViewById(R.id.tv_chat_profile_username);
        tvChatProfileUserStatus = findViewById(R.id.tv_chat_profile_user_status);

        ibSendMessage = findViewById(R.id.ib_send_private_message);
        ibSendFile = findViewById(R.id.ib_send_file);

        etMessageInput = findViewById(R.id.et_input_private_message);

        progressBarController = new TextProgressBarController(this);
        userMessageKeyRef = firebaseUtil.getMessagesRef().child(messageSenderID).child(messageReceiverID).push();

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

        slidingPaneLayout = findViewById(R.id.sliding_pane_layout);
        slidingPane = findViewById(R.id.sliding_pane);
    }

    private void setupTabLayoutForProfile(@NonNull ViewPager2 viewPager, TabLayout tabLayout) {
        FragmentStateAdapter adapter = new FragmentStateAdapter(getSupportFragmentManager(), getLifecycle()) {
            @NonNull
            @Override
            public Fragment createFragment(int position) {
                if (position == 1) {
                    return new FilesFragment();
                }
                return new ImagesFragment();
            }

            @Override
            public int getItemCount() {
                return 2;
            }
        };
        viewPager.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("Images");
                    break;
                case 1:
                    tab.setText("Files");
                    break;
            }
        }).attach();
    }

    private void configureSlidingPane() {
        int transparentColor = ContextCompat.getColor(this, android.R.color.transparent);
        slidingPaneLayout.setBackgroundColor(transparentColor);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        int screenWidth = displayMetrics.widthPixels;
        int slidingPaneWidth = (int) (screenWidth * 0.875);

        ViewGroup.LayoutParams layoutParams = slidingPane.getLayoutParams();
        layoutParams.width = slidingPaneWidth;
        slidingPane.setLayoutParams(layoutParams);
    }

    @Override
    protected void onStart() {
        super.onStart();
        childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Messages messages = snapshot.getValue(Messages.class);

                if (messages != null) {
                    messages.setMessage_id(snapshot.getKey());

                    messagesList.add(messages);

                    // TODO: Use something else instead of notifyDataSetChanged
                    messagesAdapter.notifyDataSetChanged();

                    rvUserMessagesList.smoothScrollToPosition(Objects.requireNonNull(rvUserMessagesList.getAdapter()).getItemCount());
                }
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
        };

        firebaseUtil.getMessagesRef().child(messageSenderID).child(messageReceiverID).addChildEventListener(childEventListener);
        messagesList.clear();
    }

    private void clearChatHistory(String senderUserId, String receiverUserId) {
        firebaseUtil.getMessagesRef().child(senderUserId).child(receiverUserId)
                .removeValue()
                .addOnSuccessListener(aVoid -> {
                    // Clear the local message list as well
                    int size = messagesList.size();
                    messagesList.clear();
                    // Notify the adapter that the items have been removed
                    messagesAdapter.notifyItemRangeRemoved(0, size);
                })
                .addOnFailureListener(e -> Log.e("ClearChatHistory", "Failed to clear chat history: " + e.getMessage()));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        messagesList.clear();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (childEventListener != null) {
            firebaseUtil.getMessagesRef().child(messageSenderID).child(messageReceiverID).removeEventListener(childEventListener);
            childEventListener = null;
        }
    }

    // This method is used to remove the contact from both the users' contact list
    private void removeSpecificContact() {
        firebaseUtil.getContactsRef().child(messageSenderID).child(messageReceiverID).removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                firebaseUtil.getContactsRef().child(messageReceiverID).child(messageSenderID).removeValue();
            }
        });
    }

    // This method is used to toggle the visibility of the send button based on the content of the EditText
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

    // This method is used to show the expandable menu when the user clicks on the menu button
    private void showExpandableMenu(@NonNull View view) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View customView = inflater.inflate(R.layout.expandable_menu_layout, null);

        GridLayout menuLayout = customView.findViewById(R.id.expandable_menu_layout);
        createMenuItem(menuLayout, R.drawable.ic_image, getString(R.string.gallery), v -> {
            popupWindow.dismiss();
            openGallery();
        });

        createMenuItem(menuLayout, R.drawable.ic_file, getString(R.string.file), v -> {
            popupWindow.dismiss();
            openFilePicker();
        });
        createMenuItem(menuLayout, R.drawable.ic_cam, getString(R.string.camera), v -> {
            // handle camera option
        });

        // Measure the view fully.
        customView.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));

        popupWindow = new PopupWindow(customView, ViewGroup.LayoutParams.MATCH_PARENT, customView.getMeasuredHeight(), true);
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        popupWindow.showAsDropDown(view, 0, -view.getHeight());

        Animation scaleTranslateAnimation = AnimationUtils.loadAnimation(this, R.anim.scale_translate);
        menuLayout.startAnimation(scaleTranslateAnimation);
    }

    // This method is used to create a menu item for the expandable menu
    private void createMenuItem(GridLayout menuLayout, @DrawableRes int iconRes, String text, View.OnClickListener onClickListener) {
        View menuItemView = LayoutInflater.from(this).inflate(R.layout.menu_attach_item_layout, menuLayout, false);

        ShapeableImageView imageView = menuItemView.findViewById(R.id.menu_item_icon);
        imageView.setImageResource(iconRes);

        TextView textView = menuItemView.findViewById(R.id.menu_item_text);
        textView.setText(text);

        menuItemView.setOnClickListener(onClickListener);

        menuLayout.addView(menuItemView);
    }

    // This method is used to display the last seen status of the user
    private void displayLastSeen() {
        firebaseUtil.getUsersRef().child(messageReceiverID).addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child("userState").hasChild("state")) {
                    String state = Objects.requireNonNull(snapshot.child("userState").child("state").getValue()).toString();
                    String date = Objects.requireNonNull(snapshot.child("userState").child("date").getValue()).toString();
                    String time = Objects.requireNonNull(snapshot.child("userState").child("time").getValue()).toString();

                    if (state.equals("online")) {
                        tvUserLastSeen.setText(R.string.online);
                    } else if (state.equals("offline")) {
                        SimpleDateFormat inputDateFormat = new SimpleDateFormat("MM dd yyyy", Locale.getDefault());
                        SimpleDateFormat outputDateFormat = new SimpleDateFormat("MMMM dd", Locale.getDefault());
                        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());

                        try {
                            Date lastSeenDate = inputDateFormat.parse(date);
                            Date lastSeenTime = timeFormat.parse(time);
                            Date currentDate = new Date();

                            if (lastSeenDate != null && lastSeenTime != null) {
                                long lastSeenTimestamp = lastSeenDate.getTime() + lastSeenTime.getTime();
                                long currentTime = currentDate.getTime();

                                // Calculate the difference in milliseconds
                                long difference = currentTime - lastSeenTimestamp;

                                // Convert the difference to days
                                long differenceInDays = TimeUnit.MILLISECONDS.toDays(difference);

                                String lastSeen;

                                if (differenceInDays == 1) {
                                    lastSeen = getResources().getString(R.string.last_seen_yesterday_at) + " " + time;
                                } else if (differenceInDays > 1) {
                                    lastSeen = getResources().getString(R.string.last_seen_on) + " " + outputDateFormat.format(lastSeenDate);
                                } else {
                                    lastSeen = getResources().getString(R.string.last_seen_at) + " " + time;
                                }

                                tvUserLastSeen.setText(lastSeen);
                            }

                        } catch (ParseException e) {
                            Log.e("displayLastSeen", "onDataChange: " + e);
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

    public String getFileName(@NonNull Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int columnIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (columnIndex != -1) {
                        result = cursor.getString(columnIndex);
                    }
                }
            }
        }

        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    public String getFileSize(Uri uri) {
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        if (cursor == null) {
            return "0 B";
        }

        int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
        if (sizeIndex == -1) {
            cursor.close();
            return "0 B";
        }

        cursor.moveToFirst();
        long size = cursor.getLong(sizeIndex);
        cursor.close();

        String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int unitIndex;
        for (unitIndex = 0; unitIndex < units.length - 1 && size >= BYTES_IN_KB; unitIndex++) {
            size /= BYTES_IN_KB;
        }

        return size + " " + units[unitIndex];
    }

    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, GALLERY_REQUEST_CODE);
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(Intent.createChooser(intent, "Select a File"), FILE_REQUEST_CODE);
    }

    @NonNull
    private String getFormattedDate(String pattern, Date date) {
        return new SimpleDateFormat(pattern, Locale.getDefault()).format(date);
    }

    @NonNull
    private Pair<String, String> getMessageRefs(String senderId, String receiverId) {
        String messageSenderRef = MESSAGES + senderId + "/" + receiverId;
        String messageReceiverRef = MESSAGES + receiverId + "/" + senderId;
        return new Pair<>(messageSenderRef, messageReceiverRef);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            Uri fileUri = data.getData();
            uploadAndSendImageMessage(fileUri);
        } else if (requestCode == FILE_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            Uri fileUri = data.getData();
            String fileName = getFileName(fileUri);
            String fileSize = getFileSize(fileUri);
            uploadAndSendFileMessage(fileUri, fileName, fileSize);
        }
    }

    @NonNull
    private Map<String, Object> createMessageBody(String messageType, String content, String senderId, String receiverId, String messageId, String time, String date) {

        Map<String, Object> messageBody = new HashMap<>();
        messageBody.put("message", content);
        messageBody.put("type", messageType);
        messageBody.put("from", senderId);
        messageBody.put("to", receiverId);
        messageBody.put("message_id", messageId);
        messageBody.put("time", time);
        messageBody.put("date", date);

        return messageBody;
    }

    @NonNull
    private Task<Void> updateFirebaseDatabase(String messageSenderRef, String messageReceiverRef, String messagePushID, Map<String, Object> messageBody) {
        Map<String, Object> messageBodyDetails = new HashMap<>();
        messageBodyDetails.put(messageSenderRef + "/" + messagePushID, messageBody);
        messageBodyDetails.put(messageReceiverRef + "/" + messagePushID, messageBody);

        return firebaseUtil.getRootRef().updateChildren(messageBodyDetails);
    }

    private void uploadAndSendTextMessage() {
        String messageText = etMessageInput.getText().toString();

        if (!TextUtils.isEmpty(messageText)) {
            Pair<String, String> messageRefs = getMessageRefs(messageSenderID, messageReceiverID);
            String messageSenderRef = messageRefs.first;
            String messageReceiverRef = messageRefs.second;

            String messagePushID = firebaseUtil.getMessagesRef().child(messageSenderID).child(messageReceiverID).push().getKey();

            if (messagePushID == null) {
                // handle the error
                Log.e("uploadAndSendTextMessage", "Error creating unique key for the message");
                return;
            }

            Map<String, Object> messageTextBody = createMessageBody("text",
                    messageText,
                    messageSenderID,
                    messageReceiverID,
                    messagePushID,
                    saveCurrentTime,
                    saveCurrentDate);
            updateFirebaseDatabase(messageSenderRef,
                    messageReceiverRef,
                    messagePushID,
                    messageTextBody
            ).addOnSuccessListener(aVoid -> etMessageInput.setText(""))
                    .addOnFailureListener(e -> Log.e("uploadAndSendTextMessage", e + ""));

        }
    }

    private void uploadAndSendImageMessage(Uri imageUri) {
        progressBarController.show("Sending the image...");

        Pair<String, String> messageRefs = getMessageRefs(messageSenderID, messageReceiverID);
        String messageSenderRef = messageRefs.first;
        String messageReceiverRef = messageRefs.second;

        String messagePushID = firebaseUtil.getMessagesRef().child(messageSenderID).child(messageReceiverID).push().getKey();

        if (messagePushID == null) {
            // handle the error
            Log.e("uploadAndSendImageMessage", "Error creating unique key for the message");
            return;
        }

        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("images");
        StorageReference imagePath = storageReference.child(messagePushID + ".jpg");

        imagePath.putFile(imageUri).continueWithTask(task -> {
            if (!task.isSuccessful()) {
                Log.e("uploadAndSendImageMessage", "uploadAndSendImageMessage: " + Objects.requireNonNull(task.getException()).getMessage());
            }
            return imagePath.getDownloadUrl();
        }).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Uri downloadUrl = task.getResult();
                assert downloadUrl != null;

                String time = getFormattedDate(TIME_FORMAT, new Date());
                String date = getFormattedDate(DATE_FORMAT, new Date());
                Map<String, Object> messageImageBody = createMessageBody("image",
                        downloadUrl.toString(),
                        messageSenderID,
                        messageReceiverID,
                        messagePushID,
                        time,
                        date);

                updateFirebaseDatabase(messageSenderRef,
                        messageReceiverRef,
                        messagePushID,
                        messageImageBody).addOnSuccessListener(aVoid -> etMessageInput.setText(""))
                        .addOnFailureListener(e -> Log.e("uploadAndSendImageMessage", e + ""));

                progressBarController.hide();
            } else {
                Toast.makeText(ChatActivity.this, "Error: " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                progressBarController.hide();
            }
        });
    }

    private void uploadAndSendFileMessage(Uri fileUri, String fileName, String fileSize) {
        progressBarController.show("Sending the file...");

        Pair<String, String> messageRefs = getMessageRefs(messageSenderID, messageReceiverID);
        String messageSenderRef = messageRefs.first;
        String messageReceiverRef = messageRefs.second;

        userMessageKeyRef = firebaseUtil.getMessagesRef().child(messageSenderID).child(messageReceiverID).push();
        String messagePushID = userMessageKeyRef.getKey();

        if (messagePushID == null) {
            // handle the error
            Log.e("uploadAndSendFileMessage", "Error creating unique key for the message");
            return;
        }

        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("files");
        StorageReference filePath = storageReference.child(messagePushID + "_" + fileName);

        filePath.putFile(fileUri).continueWithTask(task -> {
            if (!task.isSuccessful()) {
                Log.e("uploadAndSendFileMessage", "uploadAndSendFileMessage: " + Objects.requireNonNull(task.getException()).getMessage());
            }
            return filePath.getDownloadUrl();
        }).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Uri downloadUrl = task.getResult();
                assert downloadUrl != null;

                String time = getFormattedDate(TIME_FORMAT, new Date());
                String date = getFormattedDate(DATE_FORMAT, new Date());
                Map<String, Object> messageFileBody = createMessageBody("file",
                        downloadUrl.toString(),
                        messageSenderID,
                        messageReceiverID,
                        messagePushID,
                        time,
                        date);

                messageFileBody.put("fileName", fileName);
                messageFileBody.put("fileSize", String.valueOf(fileSize));

                updateFirebaseDatabase(messageSenderRef,
                        messageReceiverRef,
                        messagePushID,
                        messageFileBody).addOnSuccessListener(aVoid -> etMessageInput.setText(""))
                        .addOnFailureListener(e -> Log.e("uploadAndSendFileMessage", e + ""));

                progressBarController.hide();
            } else {
                Toast.makeText(ChatActivity.this, "Error: " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                progressBarController.hide();
            }
        });
    }
}