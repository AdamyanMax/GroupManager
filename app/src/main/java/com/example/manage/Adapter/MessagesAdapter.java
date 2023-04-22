package com.example.manage.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
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
    private final List<Messages> userMessagesList;
    private FirebaseAuth mAuth;

    public MessagesAdapter(List<Messages> userMessagesList) {
        this.userMessagesList = userMessagesList;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_text_messages_layout, parent, false);
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
        holder.tvReceiverMessageText.setVisibility(View.GONE);
        holder.civReceiverProfileImage.setVisibility(View.GONE);
        holder.tvSenderMessageText.setVisibility(View.GONE);
        if (fromMessageType.equals("text")) {

            if (fromUserID.equals(messageSenderID)) {
                holder.tvSenderMessageText.setVisibility(View.VISIBLE);
                holder.tvSenderMessageTime.setVisibility(View.VISIBLE);
                holder.llSenderLayout.setBackgroundResource(R.drawable.bg_sender_messages_layout);
                holder.llReceiverLayout.setVisibility(View.INVISIBLE);

                holder.tvSenderMessageText.setText(messages.getMessage());
                holder.tvSenderMessageTime.setText(messages.getTime());

            } else {
                holder.civReceiverProfileImage.setVisibility(View.VISIBLE);
                holder.tvReceiverMessageText.setVisibility(View.VISIBLE);
                holder.tvReceiverMessageTime.setVisibility(View.VISIBLE);

                holder.llReceiverLayout.setBackgroundResource(R.drawable.bg_receiver_messages_layout);
                holder.llSenderLayout.setVisibility(View.INVISIBLE);

                holder.tvReceiverMessageText.setText(messages.getMessage());
                holder.tvReceiverMessageTime.setText(messages.getTime());

            }
        }
    }

    @Override
    public int getItemCount() {
        return userMessagesList.size();
    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        public TextView tvSenderMessageText, tvReceiverMessageText, tvReceiverMessageTime, tvSenderMessageTime;
        public CircleImageView civReceiverProfileImage;
        public LinearLayout llSenderLayout, llReceiverLayout;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);

            tvSenderMessageText = itemView.findViewById(R.id.tv_sender_message);
            tvReceiverMessageText = itemView.findViewById(R.id.tv_receiver_message);
            civReceiverProfileImage = itemView.findViewById(R.id.civ_receiver_profile_image);
            tvReceiverMessageTime = itemView.findViewById(R.id.tv_receiver_message_time);
            tvSenderMessageTime = itemView.findViewById(R.id.tv_sender_message_time);
            llReceiverLayout = itemView.findViewById(R.id.ll_receiver_message_layout);
            llSenderLayout = itemView.findViewById(R.id.ll_sender_message_layout);
        }
    }


}
