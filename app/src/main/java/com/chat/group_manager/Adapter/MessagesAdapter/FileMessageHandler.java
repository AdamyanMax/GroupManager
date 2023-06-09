//package com.chat.group_manager.Adapter.MessagesAdapter;
//
//import android.content.Intent;
//import android.net.Uri;
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
//public class FileMessageHandler {
//
//    public static void handleFileMessages(@NonNull MessagesAdapter.MessageViewHolder holder,
//                                          Messages messages,
//                                          boolean isSender,
//                                          int position,
//                                          @NonNull List<Messages> userMessagesList,
//                                          FirebaseDatabaseReferences firebaseDatabaseReferences,
//                                          MessagesAdapter adapter) {
//        holder.cardSenderFile.setOnLongClickListener(view -> {
//
//            MessagePopupMenu.showPopupMenu(view,
//                    isSender,
//                    position,
//                    userMessagesList,
//                    firebaseDatabaseReferences,
//                    adapter);
//            return true;
//        });
//        holder.cardReceiverFile.setOnLongClickListener(view -> {
//
//            MessagePopupMenu.showPopupMenu(view,
//                    isSender,
//                    position,
//                    userMessagesList,
//                    firebaseDatabaseReferences,
//                    adapter);
//            return true;
//        });
//
//        holder.cardSenderFile.setOnClickListener(v -> downloadFile(holder,
//                position,
//                userMessagesList));
//
//        holder.cardReceiverFile.setOnClickListener(v -> downloadFile(holder,
//                position,
//                userMessagesList));
//
//        if (isSender) {
//            String status = messages.getStatus();
//
//            if (status != null) {
//                switch (status) {
//                    case "sent":
//                        holder.ivFileSentSeen.setImageResource(R.drawable.ic_sent);
//                        break;
//                    case "delivered":
//                        holder.ivFileSentSeen.setImageResource(R.drawable.ic_delivered);
//                        break;
//                    case "seen":
//                        holder.ivFileSentSeen.setImageResource(R.drawable.ic_seen);
//                        break;
//                }
//            } else {
//                Log.e("handleFileMessages", "handleFileMessages: message status is null");
//            }
//
//            holder.cardSenderFile.setVisibility(View.VISIBLE);
//
//            holder.civReceiverProfileImage.setVisibility(View.GONE);
//            holder.cardReceiverFile.setVisibility(View.GONE);
//
//            holder.tvSenderFileName.setText(messages.getFileName());
//            holder.tvSenderFileSize.setText(messages.getFileSize());
//            holder.tvSenderFileTime.setText(messages.getTime());
//        } else {
//            holder.cardReceiverFile.setVisibility(View.VISIBLE);
//            holder.cardSenderFile.setVisibility(View.GONE);
//
//            holder.tvReceiverFileName.setText(messages.getFileName());
//            holder.tvReceiverFileSize.setText(messages.getFileSize());
//            holder.tvReceiverFileTime.setText(messages.getTime());
//        }
//    }
//
//    public static void downloadFile(@NonNull MessagesAdapter.MessageViewHolder holder,
//                                    int position,
//                                    @NonNull List<Messages> userMessagesList) {
//        Intent downloadIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(userMessagesList.get(position).getMessage()));
//        holder.itemView.getContext().startActivity(downloadIntent);
//    }
//}
