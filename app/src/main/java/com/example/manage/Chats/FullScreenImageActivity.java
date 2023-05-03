package com.example.manage.Chats;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.manage.R;
import com.github.chrisbanes.photoview.PhotoView;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FullScreenImageActivity extends AppCompatActivity {

    // TODO: Save the user's scroll position in the ChatActivity

    private static final int PERMISSION_REQUEST_CODE = 100;

    private MaterialToolbar toolbar;
    private ConstraintLayout constraintLayout;
    private boolean isToolbarVisible = true;
    private ImageButton ibBackButton, ibDownloadImage;
    private PhotoView fullScreenImage;
    private TextView tvUsername, tvTime;
    private String image_url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen_image);

        initializeFields();
        getWindow().setStatusBarColor(getResources().getColor(R.color.black, getTheme()));

        image_url = getIntent().getStringExtra("url");
        Picasso.get().load(image_url).into(fullScreenImage);

        String uid = getIntent().getStringExtra("uid");
        String time = getIntent().getStringExtra("time");
        String date = getIntent().getStringExtra("date");

        fetchNameAndSetTextView(uid);
        String timeAndDate = date + " " + getString(R.string.at) + " " + time;
        tvTime.setText(timeAndDate);

        constraintLayout.setOnClickListener(v -> {
            if (isToolbarVisible) {
                hideToolbar();
            } else {
                showToolbar();
            }
        });
        fullScreenImage.setOnClickListener(v -> {
            if (isToolbarVisible) {
                hideToolbar();
            } else {
                showToolbar();
            }
        });

        ibBackButton.setOnClickListener(v -> onBackPressed());
        ibDownloadImage.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(FullScreenImageActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(FullScreenImageActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
            } else {
                downloadImage();
            }
        });
    }

    private void initializeFields() {
        toolbar = findViewById(R.id.full_image_view_fullscreen_toolbar);
        constraintLayout = findViewById(R.id.cl_full_screen_image);
        fullScreenImage = findViewById(R.id.full_screen_image);
        ibBackButton = toolbar.findViewById(R.id.ib_back);
        ibDownloadImage = toolbar.findViewById(R.id.ib_download_image);
        tvUsername = toolbar.findViewById(R.id.tv_full_screen_image_username);
        tvTime = toolbar.findViewById(R.id.tv_full_screen_image_time);
    }

    private void fetchNameAndSetTextView(String uid) {
        DatabaseReference userNameRef = FirebaseDatabase.getInstance().getReference().child("Users").child(uid).child("name");

        userNameRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String name = dataSnapshot.getValue(String.class);
                    tvUsername.setText(name);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle any error that might occur while fetching data
                Log.e("fetchNameAndSetTextView", databaseError.getMessage());
            }
        });
    }

    private void hideToolbar() {
        if (isToolbarVisible) {
            toolbar.animate().translationY(-toolbar.getBottom()).setDuration(200).start();
            isToolbarVisible = false;
        }
    }

    private void showToolbar() {
        if (!isToolbarVisible) {
            toolbar.animate().translationY(0).setDuration(200).start();
            isToolbarVisible = true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                downloadImage();
            } else {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    Toast.makeText(FullScreenImageActivity.this, "Permission denied (rationale)", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(FullScreenImageActivity.this, "Permission denied permanently (settings)", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void downloadImage() {
        // Replace 'image_url' with the actual image URL
        Picasso.get().load(image_url).into(new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                Uri imageUri = getImageUri(FullScreenImageActivity.this, bitmap);
                if (imageUri != null) {
                    saveImageToGallery(imageUri);
                    Toast.makeText(FullScreenImageActivity.this, "Image saved to gallery", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(FullScreenImageActivity.this, "Failed to save image", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                Toast.makeText(FullScreenImageActivity.this, "Failed to save image", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
            }
        });
    }


    private void saveImageToGallery(Uri imageUri) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, "image_" + System.currentTimeMillis() + ".jpg");
            contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
            contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/YourAppFolder");

            Uri externalContentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            ContentResolver resolver = getContentResolver();
            Uri uri = resolver.insert(externalContentUri, contentValues);

            if (uri != null) {
                try (OutputStream outputStream = resolver.openOutputStream(uri)) {
                    if (outputStream != null) {
                        InputStream inputStream = getContentResolver().openInputStream(imageUri);
                        if (inputStream != null) {
                            byte[] buffer = new byte[4096];
                            int bytesRead;
                            while ((bytesRead = inputStream.read(buffer)) != -1) {
                                outputStream.write(buffer, 0, bytesRead);
                            }
                            inputStream.close();
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Error saving image to gallery: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                if (bitmap != null) {
                    String imagePath = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "image_" + System.currentTimeMillis(), null);
                    if (imagePath == null) {
                        Toast.makeText(this, "Failed to save image", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Image saved to gallery", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "Failed to retrieve image", Toast.LENGTH_SHORT).show();
                }
            } catch (IOException e) {
                Toast.makeText(this, "Error saving image to gallery: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private Uri getImageUri(@NonNull Context context, @NonNull Bitmap bitmap) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), bitmap, "Title", null);
        return Uri.parse(path);
    }

}
