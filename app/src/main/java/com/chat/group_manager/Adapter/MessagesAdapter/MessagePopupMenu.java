//package com.chat.group_manager.Adapter.MessagesAdapter;
//
//import android.view.View;
//
//import androidx.appcompat.widget.PopupMenu;
//
//import com.chat.group_manager.Helpers.FirebaseDatabaseReferences;
//import com.chat.group_manager.Module.Messages;
//import com.chat.group_manager.R;
//
//import java.util.List;
//
//public class MessagePopupMenu {
//    public static void showPopupMenu(View view,
//                                     boolean isSender,
//                                     int position,
//                                     List<Messages> userMessagesList,
//                                     FirebaseDatabaseReferences firebaseDatabaseReferences,
//                                     MessagesAdapter adapter) {
//        PopupMenu popupMenu = new PopupMenu(view.getContext(), view);
//        popupMenu.inflate(R.menu.message_popup_menu);
//        popupMenu.setOnMenuItemClickListener(item -> {
//            if (item.getItemId() == R.id.delete_message) {
//                MessageDeletionHandler.showDeleteDialog(view.getContext(), isSender, position,  userMessagesList, firebaseDatabaseReferences, adapter);
//                return true;
//            }
//            return false;
//        });
//        popupMenu.show();
//    }
//}
