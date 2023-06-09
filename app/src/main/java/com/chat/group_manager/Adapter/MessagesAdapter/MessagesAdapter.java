package com.chat.group_manager.Adapter.MessagesAdapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;

import com.chat.group_manager.Chats.Image.FullScreenImageActivity;
import com.chat.group_manager.Helpers.FirebaseDatabaseReferences;
import com.chat.group_manager.Module.Messages;
import com.chat.group_manager.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.MessageViewHolder> {
    private static final int VIEW_TYPE_TEXT_MESSAGE = 1;
    private static final int VIEW_TYPE_FILE_MESSAGE = 2;
    private static final int VIEW_TYPE_IMAGE_MESSAGE = 3;

    private final List<Messages> userMessagesList;
    private final FirebaseDatabaseReferences firebaseDatabaseReferences = new FirebaseDatabaseReferences();
    private FirebaseAuth mAuth;


    public MessagesAdapter(List<Messages> userMessagesList) {
        this.userMessagesList = userMessagesList;
    }

    @Override
    public int getItemViewType(int position) {
        Messages messages = userMessagesList.get(position);
        String fromMessageType = messages.getType();

        if (fromMessageType.equals("text")) {
            return VIEW_TYPE_TEXT_MESSAGE;
        } else if (fromMessageType.equals("file")) {
            return VIEW_TYPE_FILE_MESSAGE;
        } else {
            return VIEW_TYPE_IMAGE_MESSAGE;
        }
    }


    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;

        if (viewType == VIEW_TYPE_TEXT_MESSAGE) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_text_messages_layout, parent, false);
        } else if (viewType == VIEW_TYPE_FILE_MESSAGE) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_file_messages_layout, parent, false);
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_image_messages_layout, parent, false);
        }

        mAuth = FirebaseAuth.getInstance();
        return new MessageViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        String messageSenderID = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        Messages messages = userMessagesList.get(position);

        String fromUserID = messages.getFrom();
        String fromMessageType = messages.getType();

        DatabaseReference senderUserId = firebaseDatabaseReferences.getUsersRef().child(fromUserID);

        senderUserId.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.hasChild("image")) {
                    String receiverImage = Objects.requireNonNull(snapshot.child("image").getValue()).toString();
                    Picasso.get().load(receiverImage).placeholder(R.drawable.user_default_profile_pic).into(holder.civReceiverProfileImage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        boolean isSender = fromUserID.equals(messageSenderID);

        switch (fromMessageType) {
            case "text":
                handleTextMessages(holder, messages, fromUserID.equals(messageSenderID), position);
                break;
            case "image":
                handleImageMessages(holder, messages, isSender, position);
                break;
            case "file":
                handleFileMessages(holder, messages, isSender, position);
                break;
        }
    }

    private void handleTextMessages(@NonNull MessageViewHolder holder, Messages messages, boolean isSender, int position) {
        holder.cardSenderText.setOnLongClickListener(view -> {
            showPopupMenu(view, isSender, position);
            return true;
        });
        holder.cardReceiverText.setOnLongClickListener(view -> {
            showPopupMenu(view, isSender, position);
            return true;
        });

        if (isSender) {
            String status = messages.getStatus();

            if (status != null) {
                switch (status) {
                    case "sent":
                        holder.ivTextSeenSent.setImageResource(R.drawable.ic_sent);
                        break;
                    case "delivered":
                        holder.ivTextSeenSent.setImageResource(R.drawable.ic_delivered);
                        break;
                    case "seen":
                        holder.ivTextSeenSent.setImageResource(R.drawable.ic_seen);
                        break;
                }
            } else {
                Log.e("handleTextMessages", "handleTextMessages: message status is null");
            }

            holder.cardSenderText.setVisibility(View.VISIBLE);
            holder.tvSenderMessageText.setText(messages.getMessage());
            holder.tvSenderTextTime.setText(messages.getTime());

            holder.cardReceiverText.setVisibility(View.GONE);
            holder.civReceiverProfileImage.setVisibility(View.GONE);
        } else {
            holder.cardReceiverText.setVisibility(View.VISIBLE);
            holder.civReceiverProfileImage.setVisibility(View.VISIBLE);
            holder.tvReceiverMessageText.setText(messages.getMessage());
            holder.tvReceiverTextTime.setText(messages.getTime());

            holder.cardSenderText.setVisibility(View.GONE);
        }
    }

    private void handleImageMessages(@NonNull MessageViewHolder holder, Messages messages, boolean isSender, int position) {
        // TODO: Long clicking doesn't show the popup menu
        holder.cardSenderImage.setOnLongClickListener(v -> {
            showPopupMenu(v, isSender, position);
            return true;
        });
        holder.cardReceiverImage.setOnLongClickListener(v -> {
            showPopupMenu(v, isSender, position);
            return true;
        });

        holder.ivSenderImage.setOnClickListener(v -> viewImageFullscreen(holder, position));

        holder.ivReceiverImage.setOnClickListener(v -> viewImageFullscreen(holder, position));


        if (isSender) {
            holder.cardSenderImage.setVisibility(View.VISIBLE);
            holder.tvSenderImageTime.setText(messages.getTime());
            holder.civReceiverProfileImage.setVisibility(View.GONE);

            String status = messages.getStatus();

            if (status != null) {
                switch (status) {
                    case "sent":
                        holder.ivImageSeenSent.setImageResource(R.drawable.ic_sent);
                        break;
                    case "delivered":
                        holder.ivImageSeenSent.setImageResource(R.drawable.ic_delivered);
                        break;
                    case "seen":
                        holder.ivImageSeenSent.setImageResource(R.drawable.ic_seen);
                        break;
                }
            } else {
                Log.e("handleTextMessages", "handleTextMessages: message status is null");
            }

            Picasso.get().load(messages.getMessage()).into(holder.ivSenderImage);
        } else {
            holder.cardReceiverImage.setVisibility(View.VISIBLE);
            holder.civReceiverProfileImage.setVisibility(View.VISIBLE);
            holder.tvReceiverImageTime.setText(messages.getTime());

            Picasso.get().load(messages.getMessage()).placeholder(R.drawable.ic_image).into(holder.ivReceiverImage);
        }
    }

    private void handleFileMessages(@NonNull MessageViewHolder holder, Messages messages, boolean isSender, int position) {
        holder.cardSenderFile.setOnLongClickListener(view -> {
            showPopupMenu(view, isSender, position);
            return true;
        });
        holder.cardReceiverFile.setOnLongClickListener(view -> {
            showPopupMenu(view, isSender, position);
            return true;
        });

        holder.cardSenderFile.setOnClickListener(v -> downloadFile(holder, position));

        holder.cardReceiverFile.setOnClickListener(v -> downloadFile(holder, position));

        if (isSender) {
            holder.cardSenderFile.setVisibility(View.VISIBLE);

            holder.civReceiverProfileImage.setVisibility(View.GONE);
            holder.cardReceiverFile.setVisibility(View.GONE);

            holder.tvSenderFileName.setText(messages.getFileName());
            holder.tvSenderFileSize.setText(messages.getFileSize());
            holder.tvSenderFileTime.setText(messages.getTime());

            String status = messages.getStatus();

            if (status != null) {
                switch (status) {
                    case "sent":
                        holder.ivFileSeenSent.setImageResource(R.drawable.ic_sent);
                        break;
                    case "delivered":
                        holder.ivFileSeenSent.setImageResource(R.drawable.ic_delivered);
                        break;
                    case "seen":
                        holder.ivFileSeenSent.setImageResource(R.drawable.ic_seen);
                        break;
                }
            } else {
                Log.e("handleTextMessages", "handleTextMessages: message status is null");
            }

        } else {
            holder.cardReceiverFile.setVisibility(View.VISIBLE);
            holder.cardSenderFile.setVisibility(View.GONE);

            holder.tvReceiverFileName.setText(messages.getFileName());
            holder.tvReceiverFileSize.setText(messages.getFileSize());
            holder.tvReceiverFileTime.setText(messages.getTime());
        }
    }

    private void viewImageFullscreen(@NonNull MessageViewHolder holder, int position) {
        // Hide the keyboard
        hideKeyboard((Activity) holder.itemView.getContext());

        Intent viewImageIntent = new Intent(holder.itemView.getContext(), FullScreenImageActivity.class);
        viewImageIntent.putExtra("url", userMessagesList.get(position).getMessage());
        viewImageIntent.putExtra("uid", userMessagesList.get(position).getFrom());
        viewImageIntent.putExtra("time", userMessagesList.get(position).getTime());
        viewImageIntent.putExtra("date", userMessagesList.get(position).getDate());
        holder.itemView.getContext().startActivity(viewImageIntent);
    }


    public void hideKeyboard(@NonNull Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        // Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        // If no view currently has focus, create a new one, just so we can grab a window token from it.
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }



    private void downloadFile(@NonNull MessageViewHolder holder, int position) {
        Intent downloadIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(userMessagesList.get(position).getMessage()));
        holder.itemView.getContext().startActivity(downloadIntent);
    }

    private void showDeleteDialog(Context context, boolean isSender, int position) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);
        builder.setTitle(R.string.delete_message);

        String[] options;
        if (isSender) {
            options = new String[]{context.getString(R.string.delete_for_everyone), context.getString(R.string.delete_for_me), context.getString(R.string.cancel)};
        } else {
            options = new String[]{context.getString(R.string.delete_for_me), context.getString(R.string.cancel)};
        }

        builder.setItems(options, (dialog, which) -> {
            if (isSender) {
                switch (which) {
                    case 0: // Delete for everyone
                        deleteForEveryone(position);
                        break;
                    case 1: // Delete for me
                        deleteForMeSender(position);
                        break;

                }
            } else {
                if (which == 0) {
                    // Delete for me (when not sender)
                    deleteForMeReceiver(position);
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    // TODO: Also delete from the firebase storage for image and file type messages
    private void deleteForEveryone(final int position) {
        // Delete for the receiver
        firebaseDatabaseReferences
                .getMessagesRef()
                .child(userMessagesList.get(position).getTo())
                .child(userMessagesList.get(position).getFrom())
                .child(userMessagesList.get(position).getMessage_id())
                .removeValue()
                .addOnSuccessListener(aVoid -> {
                    // Delete for the sender
                    firebaseDatabaseReferences
                            .getMessagesRef()
                            .child(userMessagesList.get(position).getFrom())
                            .child(userMessagesList.get(position).getTo())
                            .child(userMessagesList.get(position).getMessage_id())
                            .removeValue()
                            .addOnSuccessListener(aVoid2 -> {
                                if (position < userMessagesList.size()) {
                                    userMessagesList.remove(position);
                                    notifyItemRemoved(position);
                                }
                            })
                            .addOnFailureListener(e -> Log.e("DeleteForEveryone", "Failed to delete message for sender: " + e.getMessage()));
                })
                .addOnFailureListener(e -> Log.e("DeleteForEveryone", "Failed to delete message for receiver: " + e.getMessage()));
    }

    private void deleteForMeReceiver(final int position) {
        firebaseDatabaseReferences
                .getMessagesRef()
                .child(userMessagesList.get(position).getTo())
                .child(userMessagesList.get(position).getFrom())
                .child(userMessagesList.get(position).getMessage_id())
                .removeValue()
                .addOnSuccessListener(aVoid -> {
                    if (position < userMessagesList.size()) {
                        userMessagesList.remove(position);
                        notifyItemRemoved(position);
                    }
                })
                .addOnFailureListener(e -> Log.e("DeleteForMeReceiver", "Failed to delete message: " + e.getMessage()));
    }

    private void deleteForMeSender(final int position) {
        firebaseDatabaseReferences
                .getMessagesRef()
                .child(userMessagesList.get(position).getFrom())
                .child(userMessagesList.get(position).getTo())
                .child(userMessagesList.get(position).getMessage_id())
                .removeValue()
                .addOnSuccessListener(aVoid -> {
                    if (position < userMessagesList.size()) {
                        userMessagesList.remove(position);
                        notifyItemRemoved(position);
                    }
                })
                .addOnFailureListener(e -> Log.e("DeleteForMeSender", "Failed to delete message: " + e.getMessage()));
    }

    public void clearChatHistory(String senderUserId, String receiverUserId) {
        firebaseDatabaseReferences.getMessagesRef().child(senderUserId).child(receiverUserId)
                .removeValue()
                .addOnSuccessListener(aVoid -> {
                    // Clear the local message list as well
                    int size = userMessagesList.size();
                    userMessagesList.clear();
                    // Notify the adapter that the items have been removed
                    notifyItemRangeRemoved(0, size);
                })
                .addOnFailureListener(e -> Log.e("ClearChatHistory", "Failed to clear chat history: " + e.getMessage()));
    }

    private void showPopupMenu(View view, boolean isSender, int position) {
        PopupMenu popupMenu = new PopupMenu(view.getContext(), view);
        popupMenu.inflate(R.menu.message_popup_menu);
        popupMenu.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.delete_message) {
                showDeleteDialog(view.getContext(), isSender, position);
                return true;
            }
            return false;
        });
        popupMenu.show();
    }

    @Override
    public int getItemCount() {
        return userMessagesList.size();
    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        // Universal
        public CircleImageView civReceiverProfileImage;
        // Text
        public TextView tvSenderMessageText, tvReceiverMessageText, tvReceiverTextTime, tvSenderTextTime;
        public androidx.cardview.widget.CardView cardSenderText, cardReceiverText;
        public ImageView ivTextSeenSent;

        // Image
        public ImageView ivSenderImage, ivReceiverImage;
        public TextView tvSenderImageTime, tvReceiverImageTime;
        public androidx.cardview.widget.CardView cardSenderImage, cardReceiverImage;
        public ImageView ivImageSeenSent;

        // File
        public TextView tvSenderFileName, tvSenderFileSize, tvReceiverFileName, tvReceiverFileSize;
        public TextView tvSenderFileTime, tvReceiverFileTime;
        public androidx.cardview.widget.CardView cardSenderFile, cardReceiverFile;
        public ImageView ivFileSeenSent;


        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            // Universal
            civReceiverProfileImage = itemView.findViewById(R.id.civ_receiver_profile_image);

            // Text
            tvSenderMessageText = itemView.findViewById(R.id.tv_sender_message);
            tvReceiverMessageText = itemView.findViewById(R.id.tv_receiver_message);

            tvReceiverTextTime = itemView.findViewById(R.id.tv_receiver_message_time);
            tvSenderTextTime = itemView.findViewById(R.id.tv_sender_message_time);
            ivTextSeenSent = itemView.findViewById(R.id.iv_text_sent_seen);

            cardSenderText = itemView.findViewById(R.id.card_sender_text);
            cardReceiverText = itemView.findViewById(R.id.card_receiver_text);

            // Image
            ivSenderImage = itemView.findViewById(R.id.iv_sender_image);
            ivReceiverImage = itemView.findViewById(R.id.iv_receiver_image);

            tvSenderImageTime = itemView.findViewById(R.id.tv_sender_image_time);
            tvReceiverImageTime = itemView.findViewById(R.id.tv_receiver_image_time);
            ivImageSeenSent = itemView.findViewById(R.id.iv_image_sent_seen);

            cardSenderImage = itemView.findViewById(R.id.card_sender_image);
            cardReceiverImage = itemView.findViewById(R.id.card_receiver_image);

            // File
            tvSenderFileName = itemView.findViewById(R.id.tv_sender_file_name);
            tvSenderFileSize = itemView.findViewById(R.id.tv_sender_file_size);

            tvReceiverFileName = itemView.findViewById(R.id.tv_receiver_file_name);
            tvReceiverFileSize = itemView.findViewById(R.id.tv_receiver_file_size);

            tvSenderFileTime = itemView.findViewById(R.id.tv_sender_file_time);
            tvReceiverFileTime = itemView.findViewById(R.id.tv_receiver_file_time);
            ivFileSeenSent = itemView.findViewById(R.id.iv_file_sent_seen);

            cardSenderFile = itemView.findViewById(R.id.card_sender_file);
            cardReceiverFile = itemView.findViewById(R.id.card_receiver_file);
        }
    }
}