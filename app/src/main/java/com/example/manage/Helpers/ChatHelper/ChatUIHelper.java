package com.example.manage.Helpers.ChatHelper;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.slidingpanelayout.widget.SlidingPaneLayout;

import com.example.manage.R;
import com.google.android.material.imageview.ShapeableImageView;

public class ChatUIHelper {
    private final Context context;
    private final AppCompatActivity activity;
    private final ChatFileHelper chatFileHelper;
    private PopupWindow popupWindow;

    public ChatUIHelper(Context context, AppCompatActivity activity) {
        this.context = context;
        this.activity = activity;
        this.chatFileHelper = new ChatFileHelper(this.activity);
    }

    public void configureSlidingPane(@NonNull SlidingPaneLayout slidingPaneLayout, @NonNull ConstraintLayout slidingPane) {
        int transparentColor = ContextCompat.getColor(context, android.R.color.transparent);
        slidingPaneLayout.setBackgroundColor(transparentColor);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        int screenWidth = displayMetrics.widthPixels;
        int slidingPaneWidth = (int) (screenWidth * 0.875);

        ViewGroup.LayoutParams layoutParams = slidingPane.getLayoutParams();
        layoutParams.width = slidingPaneWidth;
        slidingPane.setLayoutParams(layoutParams);

        slidingPaneLayout.openPane();
    }

    public void showExpandableMenu(@NonNull View view) {
        LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View customView = inflater.inflate(R.layout.expandable_menu_layout, null);

        GridLayout menuLayout = customView.findViewById(R.id.expandable_menu_layout);
        createMenuItem(menuLayout, R.drawable.ic_image, activity.getString(R.string.gallery), v -> {
            popupWindow.dismiss();
            chatFileHelper.openGallery();
        });

        createMenuItem(menuLayout, R.drawable.ic_file, activity.getString(R.string.file), v -> {
            popupWindow.dismiss();
            chatFileHelper.openFilePicker();
        });
        createMenuItem(menuLayout, R.drawable.ic_cam, activity.getString(R.string.camera), v -> {
            // handle camera option
        });

        // Measure the view fully.
        customView.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));

        popupWindow = new PopupWindow(customView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        popupWindow.showAsDropDown(view, 0, -view.getHeight());

        Animation scaleTranslateAnimation = AnimationUtils.loadAnimation(activity, R.anim.scale_translate);
        menuLayout.startAnimation(scaleTranslateAnimation);
    }

    public void toggleButtonsBasedOnEditTextContent(@NonNull EditText etMessageInput, ImageButton ibSendFile, ImageButton ibSendMessage) {
        etMessageInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 0) {
                    // No text in EditText
                    ibSendFile.setVisibility(View.VISIBLE);
                    ibSendMessage.setVisibility(View.GONE);
                } else {
                    // Text is present in EditText
                    ibSendFile.setVisibility(View.GONE);
                    ibSendMessage.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void createMenuItem(GridLayout menuLayout, @DrawableRes int iconRes, String text, View.OnClickListener onClickListener) {
        View menuItemView = LayoutInflater.from(activity).inflate(R.layout.menu_attach_item_layout, menuLayout, false);

        ShapeableImageView imageView = menuItemView.findViewById(R.id.menu_item_icon);
        imageView.setImageResource(iconRes);

        TextView textView = menuItemView.findViewById(R.id.menu_item_text);
        textView.setText(text);

        menuItemView.setOnClickListener(onClickListener);

        menuLayout.addView(menuItemView);
    }
}
