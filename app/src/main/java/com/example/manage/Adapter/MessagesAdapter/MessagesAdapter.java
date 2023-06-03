package com.example.manage.Adapter.MessagesAdapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.manage.Helpers.FirebaseDatabaseReferences;
import com.example.manage.Module.Messages;
import com.example.manage.R;
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

    public void removeItem(int position) {
        userMessagesList.remove(position);
        notifyItemRemoved(position);
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
                TextMessageHandler.handleTextMessages(holder, messages, fromUserID.equals(messageSenderID), position, userMessagesList, firebaseDatabaseReferences, this);
                break;
            case "image":
                ImageMessageHandler.handleImageMessages(holder, messages, isSender, position, userMessagesList, firebaseDatabaseReferences, this);
                break;
            case "file":
                FileMessageHandler.handleFileMessages(holder, messages, isSender, position, userMessagesList, firebaseDatabaseReferences, this);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return userMessagesList.size();
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
        public ImageView ivImageSentSeen;

        // File
        public TextView tvSenderFileName, tvSenderFileSize, tvReceiverFileName, tvReceiverFileSize;
        public TextView tvSenderFileTime, tvReceiverFileTime;
        public androidx.cardview.widget.CardView cardSenderFile, cardReceiverFile;
        public ImageView ivFileSentSeen;


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
            ivImageSentSeen = itemView.findViewById(R.id.iv_image_sent_seen);

            cardSenderImage = itemView.findViewById(R.id.card_sender_image);
            cardReceiverImage = itemView.findViewById(R.id.card_receiver_image);

            // File
            tvSenderFileName = itemView.findViewById(R.id.tv_sender_file_name);
            tvSenderFileSize = itemView.findViewById(R.id.tv_sender_file_size);

            tvReceiverFileName = itemView.findViewById(R.id.tv_receiver_file_name);
            tvReceiverFileSize = itemView.findViewById(R.id.tv_receiver_file_size);

            tvSenderFileTime = itemView.findViewById(R.id.tv_sender_file_time);
            tvReceiverFileTime = itemView.findViewById(R.id.tv_receiver_file_time);
            ivFileSentSeen = itemView.findViewById(R.id.iv_file_sent_seen);

            cardSenderFile = itemView.findViewById(R.id.card_sender_file);
            cardReceiverFile = itemView.findViewById(R.id.card_receiver_file);
        }
    }
}
