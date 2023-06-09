package com.chat.group_manager.Helpers.ChatHelper;

import static com.chat.group_manager.Helpers.ChatHelper.ChatConstants.BYTES_IN_KB;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class ChatFileHelper {
    private final AppCompatActivity activity;
    private final ActivityResultLauncher<Intent> fileLauncher;
    private final ActivityResultLauncher<String> galleryLauncher;

    public ChatFileHelper(@NonNull AppCompatActivity activity) {
        this.activity = activity;
        this.fileLauncher = activity.registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    // Handle the Intent
                }
        );

        this.galleryLauncher = activity.registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    // Handle the Uri
                }
        );
    }

    public String getFileName(@NonNull Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = activity.getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int columnIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (columnIndex != -1) {
                        result = cursor.getString(columnIndex);
                    }
                }
            }
        }

        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    public String getFileSize(Uri uri) {
        Cursor cursor = activity.getContentResolver().query(uri, null, null, null, null);
        if (cursor == null) {
            return "0 B";
        }

        int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
        if (sizeIndex == -1) {
            cursor.close();
            return "0 B";
        }

        cursor.moveToFirst();
        long size = cursor.getLong(sizeIndex);
        cursor.close();

        String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int unitIndex;
        for (unitIndex = 0; unitIndex < units.length - 1 && size >= BYTES_IN_KB; unitIndex++) {
            size /= BYTES_IN_KB;
        }

        return size + " " + units[unitIndex];
    }

    public void openGallery() {
        galleryLauncher.launch("image/*");
    }

    public void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        fileLauncher.launch(intent);
    }
}

