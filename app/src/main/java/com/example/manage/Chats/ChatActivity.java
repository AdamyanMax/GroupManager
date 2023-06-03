package com.example.manage.Chats;

import static com.example.manage.Helpers.ChatHelper.ChatConstants.FILE_REQUEST_CODE;
import static com.example.manage.Helpers.ChatHelper.ChatConstants.GALLERY_REQUEST_CODE;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.slidingpanelayout.widget.SlidingPaneLayout;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.example.manage.Adapter.MessagesAdapter.MessagesAdapter;
import com.example.manage.Chats.Profile.FilesFragment;
import com.example.manage.Chats.Profile.ImagesFragment;
import com.example.manage.Helpers.ChatHelper.ChatDatabaseHelper;
import com.example.manage.Helpers.ChatHelper.ChatFileHelper;
import com.example.manage.Helpers.ChatHelper.ChatUIHelper;
import com.example.manage.Helpers.FirebaseDatabaseReferences;
import com.example.manage.Helpers.FirebaseManager;
import com.example.manage.Helpers.MessagesHelper.MessageUploader;
import com.example.manage.Helpers.NavigateUtil;
import com.example.manage.Helpers.ProgressBar.TextProgressBarController;
import com.example.manage.Module.Messages;
import com.example.manage.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {
    // TODO: Remember user's scroll position and save it
    private final ChatDatabaseHelper chatDatabaseHelper = new ChatDatabaseHelper(this);
    private final ChatFileHelper chatFileHelper = new ChatFileHelper(this);
    private final ChatUIHelper chatUIHelper = new ChatUIHelper(this, this);

    private final MessageUploader messageUploader = new MessageUploader();
    private final FirebaseManager firebaseManager = new FirebaseManager();


    private final List<Messages> messagesList = new ArrayList<>();
    private final FirebaseDatabaseReferences firebaseDatabaseReferences = new FirebaseDatabaseReferences();
    private String messageReceiverID, messageSenderID, saveCurrentTime, saveCurrentDate;
    private TextView tvUsername, tvUserLastSeen, tvChatProfileUsername, tvChatProfileUserStatus;
    private CircleImageView civProfileImage, civChatProfileUserImage;
    private ImageButton ibSendMessage, ibSendFile;
    private EditText etMessageInput;
    private MessagesAdapter messagesAdapter;
    private RecyclerView rvUserMessagesList;
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

        chatUIHelper.toggleButtonsBasedOnEditTextContent(etMessageInput, ibSendFile, ibSendMessage);

        chatDatabaseHelper.displayLastSeen(messageReceiverID, tvUserLastSeen);

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
                                NavigateUtil.toMainActivity(getApplicationContext());
                                // Remove the contact
                                chatDatabaseHelper.removeSpecificContact(messageSenderID, messageReceiverID);
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
                                chatDatabaseHelper.clearChatHistory(messageSenderID,
                                        messageReceiverID,
                                        messagesList,
                                        messagesAdapter);
                            })
                            .show();

                    popup.dismiss();

                    return true;
                }

                return false;
            });

            popup.show();
        });

        ibSendFile.setOnClickListener(chatUIHelper::showExpandableMenu);
        ibSendMessage.setOnClickListener(v -> messageUploader.uploadAndSendTextMessage(messageSenderID,
                messageReceiverID,
                etMessageInput,
                saveCurrentTime,
                saveCurrentDate));

        civProfileImage.setOnClickListener(v -> {
            if (slidingPaneLayout.isOpen()) {
                slidingPaneLayout.closePane();
            } else {
                slidingPaneLayout.openPane();
            }
        });

        chatUIHelper.configureSlidingPane(slidingPaneLayout, slidingPane);

        ViewPager2 viewPager = findViewById(R.id.view_pager);
        TabLayout tabLayout = findViewById(R.id.tab_layout);

        setupTabLayoutForProfile(viewPager, tabLayout, this);
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
                    rvUserMessagesList.smoothScrollToPosition(Objects.requireNonNull(rvUserMessagesList.getAdapter()).getItemCount());

                    // TODO: Replace notifyDataSetChanged with notifyItemInserted
//                    int newMessagePosition = messagesList.size() - 1;
//                    messagesAdapter.notifyItemInserted(newMessagePosition);
                    messagesAdapter.notifyDataSetChanged();

                    if (!messageSenderID.equals(messages.getFrom())) {
                        // This means you are the receiver of the message.
                        // Now update the status to delivered if it was sent
                        if (messages.getStatus().equals("sent")) {
                            firebaseManager.updateMessageStatus(messages.getMessage_id(),
                                    "delivered",
                                    messageSenderID,
                                    messageReceiverID);
                        }
                        // Check if the message is from the other user and its status is "delivered"
                        else if (messages.getStatus().equals("delivered")) {
                            // Update the status to "seen"
                            firebaseManager.updateMessageStatus(messages.getMessage_id(),
                                    "seen",
                                    messageSenderID,
                                    messageReceiverID);
                        }
                    }
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (snapshot.hasChild("status")) {
                    String updatedStatus = snapshot.child("status").getValue(String.class);
                    // Message Id
                    String messageId = snapshot.getKey();

                    for (int i = 0; i < messagesList.size(); i++) {
                        Messages message = messagesList.get(i);
                        if (message.getMessage_id().equals(messageId)) {
                            message.setStatus(updatedStatus);
                            // Refresh the RecyclerView
                            messagesAdapter.notifyItemChanged(i);
                            break;
                        }
                    }
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
        };

        firebaseDatabaseReferences.getMessagesRef().child(messageReceiverID).child(messageSenderID).addChildEventListener(childEventListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (childEventListener != null) {
            firebaseDatabaseReferences.getMessagesRef().child(messageSenderID).child(messageReceiverID).removeEventListener(childEventListener);
            firebaseDatabaseReferences.getMessagesRef().child(messageReceiverID).child(messageSenderID).removeEventListener(childEventListener);
            childEventListener = null;
        }
        messagesList.clear(); // Clear the list here when activity is no longer visible
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        messagesList.clear(); // Clear the list here as a failsafe
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

        TextProgressBarController progressBarController = new TextProgressBarController(this);

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            messageUploader.uploadAndSendImageMessage(imageUri, this, messageSenderID, messageReceiverID);
        } else if (requestCode == FILE_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            Uri fileUri = data.getData();
            String fileName = chatFileHelper.getFileName(fileUri);
            String fileSize = chatFileHelper.getFileSize(fileUri);
            messageUploader.uploadAndSendFileMessage(fileUri, fileName, fileSize, this, messageSenderID, messageReceiverID);
        }
    }

    private void setupTabLayoutForProfile(@NonNull ViewPager2 viewPager, TabLayout tabLayout, @NonNull FragmentActivity fragmentActivity) {
        FragmentStateAdapter adapter = new FragmentStateAdapter(fragmentActivity.getSupportFragmentManager(), fragmentActivity.getLifecycle()) {
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
}