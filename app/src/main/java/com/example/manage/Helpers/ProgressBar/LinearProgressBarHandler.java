package com.example.manage.Helpers.ProgressBar;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.manage.R;
import com.google.android.material.progressindicator.LinearProgressIndicator;

public class LinearProgressBarHandler {

    private final ConstraintLayout progressBarLayout;
    private final LinearProgressIndicator progressBar;

    public LinearProgressBarHandler(@NonNull View view) {
        this.progressBarLayout = view.findViewById(R.id.progress_bar_layout);
        this.progressBar = view.findViewById(R.id.linear_progress_bar);
    }

    public void show() {
        progressBarLayout.setVisibility(View.VISIBLE);
        progressBar.show(); // This starts the indeterminate animation.
    }

    public void hide() {
        progressBarLayout.setVisibility(View.GONE);
        progressBar.hide(); // This stops the indeterminate animation.
    }

}
