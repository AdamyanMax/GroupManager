package com.example.manage.Helpers.MessagesHelper;


import static com.example.manage.Helpers.ChatHelper.ChatConstants.DATE_FORMAT;
import static com.example.manage.Helpers.ChatHelper.ChatConstants.TIME_FORMAT;

import android.app.Activity;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.widget.EditText;

import androidx.annotation.NonNull;

import com.example.manage.Helpers.FirebaseDatabaseReferences;
import com.example.manage.Helpers.FirebaseManager;
import com.example.manage.Helpers.ProgressBar.TextProgressBarController;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Date;
import java.util.Map;
import java.util.Objects;

public class MessageUploader {
    private final FirebaseDatabaseReferences firebaseDatabaseReferences = new FirebaseDatabaseReferences();
    private final FirebaseManager firebaseManager = new FirebaseManager();
    private final DateHelper dateHelper = new DateHelper();
    private final MessageFactory messageFactory = new MessageFactory();
    private TextProgressBarController progressBarController;

    public void uploadAndSendTextMessage(String messageSenderID,
                                         String messageReceiverID,
                                         @NonNull EditText etMessageInput,
                                         String saveCurrentTime,
                                         String saveCurrentDate) {
        String messageText = etMessageInput.getText().toString();

        if (!TextUtils.isEmpty(messageText)) {
            Pair<String, String> messageRefs = firebaseManager.getMessageRefs(messageSenderID, messageReceiverID);
            String messageSenderRef = messageRefs.first;
            String messageReceiverRef = messageRefs.second;

            String messagePushID = firebaseDatabaseReferences.getMessagesRef().child(messageSenderID).child(messageReceiverID).push().getKey();

            if (messagePushID == null) {
                // handle the error
                Log.e("uploadAndSendTextMessage", "Error creating unique key for the message");
                return;
            }

            Map<String, Object> messageTextBody = messageFactory.createMessageBody("text",
                    messageText,
                    messageSenderID,
                    messageReceiverID,
                    messagePushID,
                    saveCurrentTime,
                    saveCurrentDate);
            firebaseManager.updateFirebaseDatabase(messageSenderRef,
                            messageReceiverRef,
                            messagePushID,
                            messageTextBody
                    ).addOnSuccessListener(aVoid -> {
                        etMessageInput.setText("");
                        firebaseManager.updateFirebaseDatabase(messageSenderRef,
                                        messageReceiverRef,
                                        messagePushID,
                                        messageTextBody
                                ).addOnSuccessListener(task -> {
                                    etMessageInput.setText("");
                                    firebaseManager.updateMessageStatus(messagePushID, "delivered", messageSenderID, messageReceiverID);
                                })
                                .addOnFailureListener(e -> Log.e("uploadAndSendTextMessage", e.getMessage()));
                    })
                    .addOnFailureListener(e -> Log.e("uploadAndSendTextMessage", e.getMessage()));

        }
    }

    public void uploadAndSendImageMessage(Uri imageUri,
                                          Activity activity,
                                          String messageSenderID,
                                          String messageReceiverID) {
        progressBarController = new TextProgressBarController(activity);
        progressBarController.show("Sending the image...");

        Pair<String, String> messageRefs = firebaseManager.getMessageRefs(messageSenderID, messageReceiverID);
        String messageSenderRef = messageRefs.first;
        String messageReceiverRef = messageRefs.second;

        String messagePushID = firebaseDatabaseReferences.getMessagesRef().child(messageSenderID).child(messageReceiverID).push().getKey();

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

                String time = dateHelper.getFormattedDate(TIME_FORMAT, new Date());
                String date = dateHelper.getFormattedDate(DATE_FORMAT, new Date());
                Map<String, Object> messageImageBody = messageFactory.createMessageBody("image",
                        downloadUrl.toString(),
                        messageSenderID,
                        messageReceiverID,
                        messagePushID,
                        time,
                        date);

                firebaseManager.updateFirebaseDatabase(messageSenderRef,
                                messageReceiverRef,
                                messagePushID,
                                messageImageBody)
                        .addOnFailureListener(e -> Log.e("uploadAndSendImageMessage", e + ""));

                progressBarController.hide();
            } else {
                // Log an error message
                progressBarController.hide();
            }
        });
        imagePath.putFile(imageUri).addOnFailureListener(exception -> {
            // Handle unsuccessful uploads
            Log.e("Image Upload Error", exception.getMessage());
        }).continueWithTask(task -> {
            if (!task.isSuccessful()) {
                Log.e("uploadAndSendImageMessage", "uploadAndSendImageMessage: " + Objects.requireNonNull(task.getException()).getMessage());
            }
            return imagePath.getDownloadUrl();
        });

    }

    public void uploadAndSendFileMessage(Uri fileUri,
                                         String fileName,
                                         String fileSize,
                                         Activity activity,
                                         String messageSenderID,
                                         String messageReceiverID) {
        progressBarController = new TextProgressBarController(activity);
        progressBarController.show("Sending the file...");

        Pair<String, String> messageRefs = firebaseManager.getMessageRefs(messageSenderID, messageReceiverID);
        String messageSenderRef = messageRefs.first;
        String messageReceiverRef = messageRefs.second;

        DatabaseReference userMessageKeyRef = firebaseDatabaseReferences.getMessagesRef().child(messageSenderID).child(messageReceiverID).push();
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

                String time = dateHelper.getFormattedDate(TIME_FORMAT, new Date());
                String date = dateHelper.getFormattedDate(DATE_FORMAT, new Date());
                Map<String, Object> messageFileBody = messageFactory.createMessageBody("file",
                        downloadUrl.toString(),
                        messageSenderID,
                        messageReceiverID,
                        messagePushID,
                        time,
                        date);

                messageFileBody.put("fileName", fileName);
                messageFileBody.put("fileSize", String.valueOf(fileSize));

                firebaseManager.updateFirebaseDatabase(messageSenderRef,
                                messageReceiverRef,
                                messagePushID,
                                messageFileBody)
                        .addOnFailureListener(e -> Log.e("uploadAndSendFileMessage", e + ""));

                progressBarController.hide();
            } else {
                Log.e("uploadAndSendFileMessage", Objects.requireNonNull(task.getException()).getMessage());
                progressBarController.hide();
            }
        });
    }

}
