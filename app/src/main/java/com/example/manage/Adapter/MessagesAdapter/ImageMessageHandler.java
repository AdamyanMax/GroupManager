package com.example.manage.Adapter.MessagesAdapter;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;

import com.example.manage.Chats.Image.FullScreenImageActivity;
import com.example.manage.Helpers.FirebaseDatabaseReferences;
import com.example.manage.Module.Messages;
import com.example.manage.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class ImageMessageHandler {
    public static void hideKeyboard(@NonNull Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        // Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        // If no view currently has focus, create a new one, just so we can grab a window token from it.
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static void handleImageMessages(@NonNull MessagesAdapter.MessageViewHolder holder,
                                           Messages messages,
                                           boolean isSender,
                                           int position,
                                           List<Messages> userMessagesList,
                                           FirebaseDatabaseReferences firebaseDatabaseReferences,
                                           MessagesAdapter adapter) {
        // TODO: Long clicking doesn't show the popup menu
        holder.cardSenderImage.setOnLongClickListener(v -> {
            MessagePopupMenu.showPopupMenu(v, isSender, position, userMessagesList, firebaseDatabaseReferences, adapter);
            return true;
        });
        holder.cardReceiverImage.setOnLongClickListener(v -> {
            MessagePopupMenu.showPopupMenu(v, isSender, position, userMessagesList, firebaseDatabaseReferences, adapter);
            return true;
        });
        holder.ivSenderImage.setOnClickListener(v -> viewImageFullscreen(holder, position, userMessagesList));

        holder.ivReceiverImage.setOnClickListener(v -> viewImageFullscreen(holder, position, userMessagesList));


        if (isSender) {
            String status = messages.getStatus();

            if (status != null) {
                switch (status) {
                    case "sent":
                        holder.ivImageSentSeen.setImageResource(R.drawable.ic_image_sent);
                        break;
                    case "delivered":
                        holder.ivImageSentSeen.setImageResource(R.drawable.ic_image_delivered);
                        break;
                    case "seen":
                        holder.ivImageSentSeen.setImageResource(R.drawable.ic_image_seen);
                        break;
                }
            } else {
                Log.e("handleImageMessages", "handleImageMessages: message status is null");
            }

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

    public static void viewImageFullscreen(@NonNull MessagesAdapter.MessageViewHolder holder,
                                           int position,
                                           @NonNull List<Messages> userMessagesList) {
        // Hide the keyboard
        hideKeyboard((Activity) holder.itemView.getContext());

        Intent viewImageIntent = new Intent(holder.itemView.getContext(), FullScreenImageActivity.class);
        viewImageIntent.putExtra("url", userMessagesList.get(position).getMessage());
        viewImageIntent.putExtra("uid", userMessagesList.get(position).getFrom());
        viewImageIntent.putExtra("time", userMessagesList.get(position).getTime());
        viewImageIntent.putExtra("date", userMessagesList.get(position).getDate());
        holder.itemView.getContext().startActivity(viewImageIntent);
    }
}
