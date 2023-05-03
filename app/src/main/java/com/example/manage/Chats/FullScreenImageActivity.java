package com.example.manage.Chats;

import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.manage.R;
import com.github.chrisbanes.photoview.PhotoView;
import com.google.android.material.appbar.MaterialToolbar;
import com.squareup.picasso.Picasso;

public class FullScreenImageActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private ConstraintLayout constraintLayout;
    private boolean isToolbarVisible = true;
    private ImageButton ibBackButton, ibDownloadImage;
    private PhotoView fullScreenImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen_image);
        initializeFields();
        getWindow().setStatusBarColor(getResources().getColor(R.color.black, getTheme()));

        String image_url = getIntent().getStringExtra("url");
        Picasso.get().load(image_url).into(fullScreenImage);

        constraintLayout.setOnClickListener(v -> {
            if (isToolbarVisible) {
                hideToolbar();
            } else {
                showToolbar();
                autoHideToolbar();
            }
        });
        fullScreenImage.setOnClickListener(v -> {
            if (isToolbarVisible) {
                hideToolbar();
            } else {
                showToolbar();
                autoHideToolbar();
            }
        });

        ibBackButton.setOnClickListener(v -> onBackPressed());
        ibDownloadImage.setOnClickListener(v -> {

        });

        autoHideToolbar();
    }

    private void initializeFields() {
        toolbar = findViewById(R.id.full_image_view_fullscreen_toolbar);
        constraintLayout = findViewById(R.id.cl_full_screen_image);
        fullScreenImage = findViewById(R.id.full_screen_image);
        ibBackButton = toolbar.findViewById(R.id.ib_back);
        ibDownloadImage = toolbar.findViewById(R.id.ib_download_image);

    }


    private void autoHideToolbar() {
        new Handler().postDelayed(this::hideToolbar, 3000);
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
}
