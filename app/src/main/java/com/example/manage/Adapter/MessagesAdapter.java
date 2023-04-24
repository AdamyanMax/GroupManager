package com.example.manage.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.manage.Chats.Messages;
import com.example.manage.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
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

        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(fromUserID);

        usersRef.addValueEventListener(new ValueEventListener() {
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

        if (fromMessageType.equals("text")) {

            if (fromUserID.equals(messageSenderID)) {
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
        } else if (fromMessageType.equals("image")) {
            if (fromUserID.equals(messageSenderID)) {
                holder.cardSenderImage.setVisibility(View.VISIBLE);
                holder.tvSenderImageTime.setText(messages.getTime());
                holder.civReceiverProfileImage.setVisibility(View.GONE);

                Picasso.get().load(messages.getMessage()).into(holder.ivSenderImage);
            } else {
                holder.cardReceiverImage.setVisibility(View.VISIBLE);
                holder.tvReceiverImageTime.setText(messages.getTime());

                Picasso.get().load(messages.getMessage()).placeholder(R.drawable.ic_image).into(holder.ivReceiverImage);
            }
        }

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

        // Image
        public ImageView ivSenderImage, ivReceiverImage;
        public TextView tvSenderImageTime, tvReceiverImageTime;
        public androidx.cardview.widget.CardView cardSenderImage, cardReceiverImage;


        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            // Universal
            civReceiverProfileImage = itemView.findViewById(R.id.civ_receiver_profile_image);
            // Text
            tvSenderMessageText = itemView.findViewById(R.id.tv_sender_message);
            tvReceiverMessageText = itemView.findViewById(R.id.tv_receiver_message);
            tvReceiverTextTime = itemView.findViewById(R.id.tv_receiver_message_time);
            tvSenderTextTime = itemView.findViewById(R.id.tv_sender_message_time);
            cardSenderText = itemView.findViewById(R.id.card_sender_text);
            cardReceiverText = itemView.findViewById(R.id.card_receiver_text);
            // Image
            ivSenderImage = itemView.findViewById(R.id.iv_sender_image);
            ivReceiverImage = itemView.findViewById(R.id.iv_receiver_image);
            tvSenderImageTime = itemView.findViewById(R.id.tv_sender_image_time);
            tvReceiverImageTime = itemView.findViewById(R.id.tv_receiver_image_time);
            cardSenderImage = itemView.findViewById(R.id.card_sender_image);
            cardReceiverImage = itemView.findViewById(R.id.card_receiver_image);
        }
    }


}
