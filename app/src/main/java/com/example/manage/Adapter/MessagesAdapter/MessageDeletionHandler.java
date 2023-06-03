//package com.example.manage.Adapter.MessagesAdapter;
//
//import android.content.Context;
//import android.util.Log;
//
//import androidx.annotation.NonNull;
//import androidx.appcompat.app.AlertDialog;
//
//import com.example.manage.Helpers.FirebaseDatabaseReferences;
//import com.example.manage.Module.Messages;
//import com.example.manage.R;
//import com.google.android.material.dialog.MaterialAlertDialogBuilder;
//
//import java.util.List;
//
//public class MessageDeletionHandler {
//    public static void showDeleteDialog(Context context,
//                                        boolean isSender,
//                                        int position,
//                                        List<Messages> userMessagesList,
//                                        FirebaseDatabaseReferences firebaseDatabaseReferences,
//                                        MessagesAdapter adapter) {
//        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);
//        builder.setTitle(R.string.delete_message);
//
//        String[] options;
//        if (isSender) {
//            options = new String[]{context.getString(R.string.delete_for_everyone), context.getString(R.string.delete_for_me), context.getString(R.string.cancel)};
//        } else {
//            options = new String[]{context.getString(R.string.delete_for_me), context.getString(R.string.cancel)};
//        }
//
//        builder.setItems(options, (dialog, which) -> {
//            if (isSender) {
//                switch (which) {
//                    case 0: // Delete for everyone
//                        deleteForEveryone(position, userMessagesList, firebaseDatabaseReferences, adapter);
//                        break;
//                    case 1: // Delete for me
//                        deleteForMeSender(position,  userMessagesList, firebaseDatabaseReferences, adapter);
//                        break;
//                    case 2: // Cancel
//                        break;
//                }
//            } else {
//                if (which == 0) {
//                    // Delete for me (when not sender)
//                    deleteForMeReceiver(position, userMessagesList, firebaseDatabaseReferences, adapter);
//                }
//            }
//        });
//
//        AlertDialog alertDialog = builder.create();
//        alertDialog.show();
//    }
//
//    // TODO: Also delete from the firebase storage for image and file type messages
//    public static void deleteForEveryone(final int position,
//                                         @NonNull List<Messages> userMessagesList,
//                                         @NonNull FirebaseDatabaseReferences firebaseDatabaseReferences,
//                                         MessagesAdapter adapter) {
//        // Delete for the receiver
//        firebaseDatabaseReferences
//                .getMessagesRef()
//                .child(userMessagesList.get(position).getTo())
//                .child(userMessagesList.get(position).getFrom())
//                .child(userMessagesList.get(position).getMessage_id())
//                .removeValue()
//                .addOnSuccessListener(aVoid -> {
//                    // Delete for the sender
//                    firebaseDatabaseReferences
//                            .getMessagesRef()
//                            .child(userMessagesList.get(position).getFrom())
//                            .child(userMessagesList.get(position).getTo())
//                            .child(userMessagesList.get(position).getMessage_id())
//                            .removeValue()
//                            .addOnSuccessListener(aVoid2 -> {
//                                if (position < userMessagesList.size()) {
//                                    userMessagesList.remove(position);
//                                    adapter.removeItem(position);
//                                }
//                            })
//                            .addOnFailureListener(e -> Log.e("DeleteForEveryone", "Failed to delete message for sender: " + e.getMessage()));
//                })
//                .addOnFailureListener(e -> Log.e("DeleteForEveryone", "Failed to delete message for receiver: " + e.getMessage()));
//    }
//
//    public static void deleteForMeReceiver(final int position,
//                                           @NonNull List<Messages> userMessagesList,
//                                           @NonNull FirebaseDatabaseReferences firebaseDatabaseReferences,) {
//        firebaseDatabaseReferences
//                .getMessagesRef()
//                .child(userMessagesList.get(position).getTo())
//                .child(userMessagesList.get(position).getFrom())
//                .child(userMessagesList.get(position).getMessage_id())
//                .removeValue()
//                .addOnSuccessListener(aVoid -> {
//                    if (position < userMessagesList.size()) {
//                        userMessagesList.remove(position);
//                        adapter.removeItem(position);
//                    }
//                })
//                .addOnFailureListener(e -> Log.e("DeleteForMeReceiver", "Failed to delete message: " + e.getMessage()));
//    }
//
//    public static void deleteForMeSender(final int position,
//                                         @NonNull List<Messages> userMessagesList,
//                                         @NonNull FirebaseDatabaseReferences firebaseDatabaseReferences,
//                                         MessagesAdapter adapter) {
//        firebaseDatabaseReferences
//                .getMessagesRef()
//                .child(userMessagesList.get(position).getFrom())
//                .child(userMessagesList.get(position).getTo())
//                .child(userMessagesList.get(position).getMessage_id())
//                .removeValue()
//                .addOnSuccessListener(aVoid -> {
//                    if (position < userMessagesList.size()) {
//                        userMessagesList.remove(position);
//                        adapter.removeItem(position);
//                    }
//                })
//                .addOnFailureListener(e -> Log.e("DeleteForMeSender", "Failed to delete message: " + e.getMessage()));
//    }
//}
