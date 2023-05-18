package com.example.manage.Helpers;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class FirebaseManager {
    private final FirebaseUtil firebaseUtil;
    private static final String NODE_REQUEST_TYPE = "request_type";

    public FirebaseManager() {
        firebaseUtil = new FirebaseUtil();
    }

    public void removeChatData(String senderUserID, String receiverUserID, OperationCallback callback) {
        DatabaseReference senderRef = firebaseUtil.getChatRequestsRef().child(senderUserID).child(receiverUserID);
        DatabaseReference receiverRef = firebaseUtil.getChatRequestsRef().child(receiverUserID).child(senderUserID);
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

        Task<Void> task1 = firebaseUtil.getContactsRef().child(senderUserID).child(receiverUserID).child("Contacts").setValue("Saved");
        tasks.add(task1);

        Task<Void> task2 = firebaseUtil.getContactsRef().child(receiverUserID).child(senderUserID).child("Contacts").setValue("Saved");
        tasks.add(task2);

        Task<Void> task3 = firebaseUtil.getChatRequestsRef().child(senderUserID).child(receiverUserID).removeValue();
        tasks.add(task3);

        Task<Void> task4 = firebaseUtil.getChatRequestsRef().child(receiverUserID).child(senderUserID).removeValue();
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
        Task<Void> task1 = firebaseUtil.getChatRequestsRef().child(senderUserID).child(receiverUserID).child(NODE_REQUEST_TYPE).setValue("sent");
        tasks.add(task1);

        // Task to set "received" request_type for receiver
        Task<Void> task2 = firebaseUtil.getChatRequestsRef().child(receiverUserID).child(senderUserID).child(NODE_REQUEST_TYPE).setValue("received");
        tasks.add(task2);

        // Task to add notification
        HashMap<String, String> chatNotificationMap = new HashMap<>();
        chatNotificationMap.put("from", senderUserID);
        chatNotificationMap.put("type", "request");
        Task<Void> task3 = firebaseUtil.getNotificationsRef().child(receiverUserID).push().setValue(chatNotificationMap);
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

}
