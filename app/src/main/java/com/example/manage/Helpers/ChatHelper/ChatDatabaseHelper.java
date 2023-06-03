package com.example.manage.Helpers.ChatHelper;

import android.content.Context;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.manage.Adapter.MessagesAdapter.MessagesAdapter;
import com.example.manage.Helpers.FirebaseDatabaseReferences;
import com.example.manage.Module.Messages;
import com.example.manage.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class ChatDatabaseHelper {
    private final FirebaseDatabaseReferences firebaseDatabaseReferences = new FirebaseDatabaseReferences();
    private final Context context;
    private ValueEventListener userLastSeenListener;

    public ChatDatabaseHelper(Context context) {
        this.context = context;
    }

    public void clearChatHistory(String senderUserId,
                                 String receiverUserId,
                                 List<Messages> messagesList,
                                 MessagesAdapter messagesAdapter) {
        firebaseDatabaseReferences.getMessagesRef().child(senderUserId).child(receiverUserId)
                .removeValue()
                .addOnSuccessListener(aVoid -> {
                    // Clear the local message list as well
                    int size = messagesList.size();
                    // Notify the adapter that the items have been removed
                    messagesAdapter.notifyItemRangeRemoved(0, size);
                    messagesList.clear();

                })
                .addOnFailureListener(e -> Log.e("ClearChatHistory", "Failed to clear chat history: " + e.getMessage()));
    }

    public void removeSpecificContact(String messageSenderID, String messageReceiverID) {
        firebaseDatabaseReferences.getContactsRef().child(messageSenderID).child(messageReceiverID).removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                firebaseDatabaseReferences.getContactsRef().child(messageReceiverID).child(messageSenderID).removeValue();
            } else {
                Log.e("removeSpecificContact", "Failed to remove contact: " + Objects.requireNonNull(task.getException()).getMessage());
            }
        });
    }

    public void displayLastSeen(String messageReceiverID, TextView tvUserLastSeen) {
        userLastSeenListener = new ValueEventListener() {

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
                                    lastSeen = context.getResources().getString(R.string.last_seen_yesterday_at) + " " + time;
                                } else if (differenceInDays > 1) {
                                    lastSeen = context.getResources().getString(R.string.last_seen_on) + " " + outputDateFormat.format(lastSeenDate);
                                } else {
                                    lastSeen = context.getResources().getString(R.string.last_seen_at) + " " + time;
                                }

                                tvUserLastSeen.setText(lastSeen);
                            }

                        } catch (ParseException e) {
                            Log.e("displayLastSeen", "onDataChange: " + e);
                            tvUserLastSeen.setText(R.string.date_parsing_failed);
                        }
                    }
                } else {
                    tvUserLastSeen.setText(R.string.offline);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("displayLastSeen", "Database read cancelled: ", error.toException());
                tvUserLastSeen.setText(R.string.user_last_seen_unavailable);
            }
        };
        firebaseDatabaseReferences.getUsersRef().child(messageReceiverID).addValueEventListener(userLastSeenListener);
    }

    public void removeUserLastSeenListener(String messageReceiverID) {
        if (userLastSeenListener != null) {
            firebaseDatabaseReferences.getUsersRef().child(messageReceiverID).removeEventListener(userLastSeenListener);
        }
    }
}
