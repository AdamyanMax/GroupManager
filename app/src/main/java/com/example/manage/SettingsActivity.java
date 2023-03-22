package com.example.manage;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Objects;

public class SettingsActivity extends AppCompatActivity {

    private static final int REQUEST_PERMISSION = 100;
    private static final int REQUEST_IMAGE_CAPTURE = 101;

    private Button btnUpgradeAccountSettings;
    private EditText etUsername, etUserStatus;
    private ImageView ivUserProfileImage;

    private String currentUserID;
    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        initializeFields();

        // TODO: Make up your mind about this
        etUsername.setVisibility(View.INVISIBLE);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        RootRef = FirebaseDatabase.getInstance().getReference();

        btnUpgradeAccountSettings.setOnClickListener(v -> updateSettings());

        retrieveUserInfo();
    }


    private void initializeFields() {
        btnUpgradeAccountSettings = findViewById(R.id.btn_update_settings);
        etUsername = findViewById(R.id.et_set_user_name);
        etUserStatus = findViewById(R.id.et_set_profile_status);
        ivUserProfileImage = findViewById(R.id.iv_set_profile_image);
    }

    private void updateSettings() {
        String setUsername = etUsername.getText().toString();
        String setStatus = etUserStatus.getText().toString();

        if (TextUtils.isEmpty(setUsername)) {
            Toast.makeText(this, R.string.please_provide_a_username, Toast.LENGTH_SHORT).show();
        }
        if (TextUtils.isEmpty(setStatus)) {
            Toast.makeText(this, R.string.please_provide_a_status, Toast.LENGTH_SHORT).show();
        } else {
            HashMap<String, Object> profileMap = new HashMap<>();
            profileMap.put("uid", currentUserID);
            profileMap.put("name", setUsername);
            profileMap.put("status", setStatus);
            RootRef.child("Users").child(currentUserID).setValue(profileMap).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    sendToMainActivity();
                    // TODO: It throws random numbers instead of a toast message
                    Toast.makeText(SettingsActivity.this, R.string.profile_updated, Toast.LENGTH_SHORT).show();
                } else {
                    String message = Objects.requireNonNull(task.getException()).toString();
                    Toast.makeText(this, "Error: " + message, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void sendToMainActivity() {
        Intent mainIntent = new Intent(SettingsActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }


    private void retrieveUserInfo() {
        RootRef.child("Users").child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if ((snapshot.exists()) && (snapshot.hasChild("name") && (snapshot.hasChild("image")))) {
                    String retrieveUsername = Objects.requireNonNull(snapshot.child("name").getValue()).toString();
                    String retrieveStatus = Objects.requireNonNull(snapshot.child("status").getValue()).toString();
                    String retrieveProfileImage = Objects.requireNonNull(snapshot.child("image").getValue()).toString();

                    etUsername.setText(retrieveUsername);
                    etUserStatus.setText(retrieveStatus);

                } else if ((snapshot.exists()) && (snapshot.hasChild("name"))) {
                    // TODO: Add the profile image
                    String retrieveUsername = Objects.requireNonNull(snapshot.child("name").getValue()).toString();
                    String retrieveStatus = Objects.requireNonNull(snapshot.child("status").getValue()).toString();

                    etUsername.setText(retrieveUsername);
                    etUserStatus.setText(retrieveStatus);
                } else {
                    // TODO: Make up your mind about this
                    etUsername.setVisibility(View.VISIBLE);
                    Toast.makeText(SettingsActivity.this, R.string.please_provide_some_information_about_your_profile + "", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}