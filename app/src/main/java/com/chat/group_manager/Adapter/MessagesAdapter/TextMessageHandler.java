//package com.chat.group_manager.Adapter.MessagesAdapter;
//
//import android.util.Log;
//import android.view.View;
//
//import androidx.annotation.NonNull;
//
//import com.chat.group_manager.Helpers.FirebaseDatabaseReferences;
//import com.chat.group_manager.Module.Messages;
//import com.chat.group_manager.R;
//
//import java.util.List;
//
//public class TextMessageHandler {
//    public static void handleTextMessages(@NonNull MessagesAdapter.MessageViewHolder holder,
//                                          Messages messages,
//                                          boolean isSender,
//                                          int position,
//                                          List<Messages> userMessagesList,
//                                          FirebaseDatabaseReferences firebaseDatabaseReferences,
//                                          MessagesAdapter adapter) {
//        holder.cardSenderText.setOnLongClickListener(view -> {
//            MessagePopupMenu.showPopupMenu(view,
//                    isSender,
//                    position,
//                    userMessagesList,
//                    firebaseDatabaseReferences,
//                    adapter);
//            return true;
//        });
//        holder.cardReceiverText.setOnLongClickListener(view -> {
//            MessagePopupMenu.showPopupMenu(view,
//                    isSender,
//                    position,
//                    userMessagesList,
//                    firebaseDatabaseReferences,
//                    adapter);
//            return true;
//        });
//
//        if (isSender) {
//            String status = messages.getStatus();
//
//            if (status != null) {
//                switch (status) {
//                    case "sent":
//                        holder.ivTextSeenSent.setImageResource(R.drawable.ic_sent);
//                        break;
//                    case "delivered":
//                        holder.ivTextSeenSent.setImageResource(R.drawable.ic_delivered);
//                        break;
//                    case "seen":
//                        holder.ivTextSeenSent.setImageResource(R.drawable.ic_seen);
//                        break;
//                }
//            } else {
//                // You could potentially set a default icon here
//                // holder.ivTextSeenSent.setImageResource(R.drawable.ic_default);
//                Log.e("handleTextMessages", "handleTextMessages: message status is null");
//            }
//
//            holder.cardSenderText.setVisibility(View.VISIBLE);
//            holder.tvSenderMessageText.setText(messages.getMessage());
//            holder.tvSenderTextTime.setText(messages.getTime());
//
//            holder.cardReceiverText.setVisibility(View.GONE);
//            holder.civReceiverProfileImage.setVisibility(View.GONE);
//        } else {
//            holder.cardReceiverText.setVisibility(View.VISIBLE);
//            holder.civReceiverProfileImage.setVisibility(View.VISIBLE);
//            holder.tvReceiverMessageText.setText(messages.getMessage());
//            holder.tvReceiverTextTime.setText(messages.getTime());
//
//            holder.cardSenderText.setVisibility(View.GONE);
//        }
//    }
//}
