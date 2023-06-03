package com.example.manage.Helpers;

import static com.example.manage.Helpers.ChatHelper.ChatConstants.MESSAGES;

import android.util.Pair;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class FirebaseManager {
    private static final String NODE_REQUEST_TYPE = "request_type";
    private final FirebaseDatabaseReferences firebaseDatabaseReferences;

    //    private final MutableLiveData<Contacts> userLiveData = new MutableLiveData<>();
//    private final MutableLiveData<String> lastMessageLiveData = new MutableLiveData<>();

    public FirebaseManager() {
        firebaseDatabaseReferences = new FirebaseDatabaseReferences();
    }

    //    public LiveData<Contacts> getUserLiveData(String userId) {
//        firebaseUtil.getUsersRef().child(userId).addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                Contacts contacts = snapshot.getValue(Contacts.class);
//                if (snapshot.child("userState").hasChild("state")) {
//                    String state = snapshot.child("userState").child("state").getValue(String.class);
//                    if (contacts != null) {
//                        contacts.setState(state);
//                    }
//                }
//                userLiveData.setValue(contacts);
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                Log.e("FirebaseManager", "Failed to load user: " + error.getMessage());
//            }
//        });
//        return userLiveData;
//    }
//
//    public LiveData<String> getLastMessageLiveData(String currentUserId, String userId) {
//        firebaseUtil.getMessagesRef().child(currentUserId).child(userId).limitToLast(1).addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                String lastMessage = "";
//                if (dataSnapshot.exists()) {
//                    for (DataSnapshot messageSnapshot : dataSnapshot.getChildren()) {
//                        String messageType = messageSnapshot.child("type").getValue(String.class);
//                        if ("text".equals(messageType)) {
//                            lastMessage = messageSnapshot.child("message").getValue(String.class);
//                        } else if ("image".equals(messageType)) {
//                            lastMessage = String.valueOf(R.string.photo);
//                        } else {
//                            lastMessage = String.valueOf(R.string.file);
//                        }
//                    }
//                }
//                lastMessageLiveData.setValue(lastMessage);
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                Log.e("FirebaseManager", "Failed to load message: " + error.getMessage());
//            }
//        });
//        return lastMessageLiveData;
//    }

    public void removeContact(String senderUserID, String receiverUserID, OperationCallback callback) {
        DatabaseReference senderRef = firebaseDatabaseReferences.getContactsRef().child(senderUserID).child(receiverUserID);
        DatabaseReference receiverRef = firebaseDatabaseReferences.getContactsRef().child(receiverUserID).child(senderUserID);
        List<Task<Void>> tasks = new ArrayList<>();

        Task<Void> removeSenderContact = senderRef.removeValue();
        tasks.add(removeSenderContact);

        Task<Void> removeReceiverContact = receiverRef.removeValue();
        tasks.add(removeReceiverContact);

        Task<Void> combinedTask = Tasks.whenAll(tasks);

        combinedTask.addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                callback.onSuccess();
            } else {
                callback.onFailure(Objects.requireNonNull(task.getException()));
            }
        });
    }

    public void declineChatRequest(String senderUserID, String receiverUserID, OperationCallback callback) {
        DatabaseReference senderRef = firebaseDatabaseReferences.getChatRequestsRef().child(senderUserID).child(receiverUserID);
        DatabaseReference receiverRef = firebaseDatabaseReferences.getChatRequestsRef().child(receiverUserID).child(senderUserID);
        List<Task<Void>> tasks = new ArrayList<>();

        Task<Void> removeSenderChatRequest = senderRef.removeValue();
        tasks.add(removeSenderChatRequest);

        Task<Void> removeReceiverChatRequest = receiverRef.removeValue();
        tasks.add(removeReceiverChatRequest);

        Task<Void> combinedTask = Tasks.whenAll(tasks);

        combinedTask.addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                callback.onSuccess();
            } else {
                callback.onFailure(Objects.requireNonNull(task.getException()));
            }
        });
    }

    public void acceptChatRequest(String senderUserID, String receiverUserID, OperationCallback callback) {
        List<Task<Void>> tasks = new ArrayList<>();

        Task<Void> task1 = firebaseDatabaseReferences.getContactsRef().child(senderUserID).child(receiverUserID).child("Contacts").setValue("Saved");
        tasks.add(task1);

        Task<Void> task2 = firebaseDatabaseReferences.getContactsRef().child(receiverUserID).child(senderUserID).child("Contacts").setValue("Saved");
        tasks.add(task2);

        Task<Void> task3 = firebaseDatabaseReferences.getChatRequestsRef().child(senderUserID).child(receiverUserID).removeValue();
        tasks.add(task3);

        Task<Void> task4 = firebaseDatabaseReferences.getChatRequestsRef().child(receiverUserID).child(senderUserID).removeValue();
        tasks.add(task4);

        Task<Void> combinedTask = Tasks.whenAll(tasks);

        combinedTask.addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                callback.onSuccess();
            } else {
                callback.onFailure(Objects.requireNonNull(task.getException()));
            }
        });
    }

    public void sendChatRequest(String senderUserID, String receiverUserID, OperationCallback callback) {
        List<Task<Void>> tasks = new ArrayList<>();

        // Task to set "sent" request_type for sender
        Task<Void> task1 = firebaseDatabaseReferences.getChatRequestsRef().child(senderUserID).child(receiverUserID).child(NODE_REQUEST_TYPE).setValue("sent");
        tasks.add(task1);

        // Task to set "received" request_type for receiver
        Task<Void> task2 = firebaseDatabaseReferences.getChatRequestsRef().child(receiverUserID).child(senderUserID).child(NODE_REQUEST_TYPE).setValue("received");
        tasks.add(task2);

        // Task to add notification
        HashMap<String, String> chatNotificationMap = new HashMap<>();
        chatNotificationMap.put("from", senderUserID);
        chatNotificationMap.put("type", "request");
        Task<Void> task3 = firebaseDatabaseReferences.getNotificationsRef().child(receiverUserID).push().setValue(chatNotificationMap);
        tasks.add(task3);

        // Run tasks simultaneously
        Tasks.whenAllComplete(tasks).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                callback.onSuccess();
            } else {
                callback.onFailure(task.getException());
            }
        });
    }

    @NonNull
    public Pair<String, String> getMessageRefs(String senderId, String receiverId) {
        String messageSenderRef = MESSAGES + senderId + "/" + receiverId;
        String messageReceiverRef = MESSAGES + receiverId + "/" + senderId;
        return new Pair<>(messageSenderRef, messageReceiverRef);
    }
    public void updateMessageStatus(String messageId, String status, String messageSenderID, String messageReceiverID) {
        DatabaseReference senderRef = firebaseDatabaseReferences.getMessagesRef()
                .child(messageSenderID)
                .child(messageReceiverID)
                .child(messageId);

        DatabaseReference receiverRef = firebaseDatabaseReferences.getMessagesRef()
                .child(messageReceiverID)
                .child(messageSenderID)
                .child(messageId);

        Map<String, Object> statusUpdate = new HashMap<>();
        statusUpdate.put("status", status);

        senderRef.updateChildren(statusUpdate);
        receiverRef.updateChildren(statusUpdate);
    }

    @NonNull
    public Task<Void> updateFirebaseDatabase(String messageSenderRef, String messageReceiverRef, String messagePushID, Map<String, Object> messageBody) {
        Map<String, Object> messageBodyDetails = new HashMap<>();
        messageBodyDetails.put(messageSenderRef + "/" + messagePushID, messageBody);
        messageBodyDetails.put(messageReceiverRef + "/" + messagePushID, messageBody);

        return firebaseDatabaseReferences.getRootRef().updateChildren(messageBodyDetails);
    }

}
