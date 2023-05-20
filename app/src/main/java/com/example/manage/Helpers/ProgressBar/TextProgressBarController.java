package com.example.manage.Helpers.ProgressBar;

import android.app.Activity;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.manage.R;

public class TextProgressBarController {
    private final ConstraintLayout clProgressBarLayout;
    private final TextView tvProgressBarText;

    public TextProgressBarController(@NonNull Activity activity) {
        this.clProgressBarLayout = activity.findViewById(R.id.cl_text_progress_bar_layout);
        this.tvProgressBarText = activity.findViewById(R.id.tv_progress_bar_text);
    }

    public void show(String text) {
        clProgressBarLayout.setVisibility(View.VISIBLE);
        tvProgressBarText.setText(text);
    }

    public void hide() {
        clProgressBarLayout.setVisibility(View.GONE);
    }

}
