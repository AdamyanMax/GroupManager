package com.chat.group_manager.Helpers;

import android.content.Context;
import android.content.Intent;

import com.chat.group_manager.MainActivity;

public class NavigateUtil {

    public static void toMainActivity(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
    }

//    public static void toAuthenticationActivity(Context context) {
//        Intent intent = new Intent(context, AuthenticationActivity.class);
//        context.startActivity(intent);
//    }
//
//    public static void toFindFriendsActivity(Context context) {
//        Intent intent = new Intent(context, FindFriendsActivity.class);
//        context.startActivity(intent);
//    }
//
//    public static void toSettingsActivity(Context context) {
//        Intent intent = new Intent(context, SettingsActivity.class);
//        context.startActivity(intent);
//    }
}
