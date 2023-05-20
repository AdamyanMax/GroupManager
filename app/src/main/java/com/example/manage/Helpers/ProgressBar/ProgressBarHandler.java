package com.example.manage.Helpers.ProgressBar;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.manage.R;

public class ProgressBarHandler {

    private final ConstraintLayout progressBarLayout;

    public ProgressBarHandler(@NonNull View view) {
        this.progressBarLayout = view.findViewById(R.id.progress_bar_layout);
    }

    public void show() {
        progressBarLayout.setVisibility(View.VISIBLE);
    }

    public void hide() {
        progressBarLayout.setVisibility(View.GONE);
    }
}
