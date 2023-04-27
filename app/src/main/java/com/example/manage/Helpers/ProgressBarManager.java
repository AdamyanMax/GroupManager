package com.example.manage.Helpers;

import android.app.Activity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.manage.R;

public class ProgressBarManager {
    private final LinearLayout llProgressBarLayout;
    private final TextView tvProgressBarText;
    private final View vBackgroundDim;

    public ProgressBarManager(@NonNull Activity activity) {
        this.llProgressBarLayout = activity.findViewById(R.id.ll_progress_bar_layout);
        this.tvProgressBarText = activity.findViewById(R.id.tv_progress_bar_text);
        this.vBackgroundDim = activity.findViewById(R.id.background_dim);
    }

    public void show(String text) {
        llProgressBarLayout.setVisibility(View.VISIBLE);
        vBackgroundDim.setVisibility(View.VISIBLE);
        tvProgressBarText.setText(text);
    }

    public void hide() {
        llProgressBarLayout.setVisibility(View.GONE);
        vBackgroundDim.setVisibility(View.GONE);
    }

}
