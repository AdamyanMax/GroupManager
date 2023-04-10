package com.example.manage.Menu;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.manage.MainActivity;
import com.example.manage.Menu.ImageCropper.CropperActivity;
import com.example.manage.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_IMAGE_PICKER = 101;
    ActivityResultLauncher<String> mGetContent;
    private Button btnUpgradeAccountSettings;
    private EditText etUsername, etUserStatus;
    private CircleImageView civUserProfileImage;

    private String currentUserID;
    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;

    private StorageReference UserProfileImageRef;
    private ProgressDialog loadingBar;
    private Toolbar settingsToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        initializeFields();

        // TODO: Make up your mind about this
        etUsername.setVisibility(View.GONE);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        RootRef = FirebaseDatabase.getInstance().getReference();
        UserProfileImageRef = FirebaseStorage.getInstance().getReference().child("Profile Images");

        btnUpgradeAccountSettings.setOnClickListener(v -> updateSettings());

        retrieveUserInfo();

        // TODO: Use 'ActivityResultLauncher<Intent>' instead of 'startActivityForResult'
        civUserProfileImage.setOnClickListener(v -> mGetContent.launch("image/*"));
        mGetContent = registerForActivityResult(new ActivityResultContracts.GetContent(), result -> {
            Intent imageCropperActivityIntent = new Intent(SettingsActivity.this, CropperActivity.class);
            imageCropperActivityIntent.putExtra("DATA", result.toString());
            startActivityForResult(imageCropperActivityIntent, 101);
        });

    }

    private void initializeFields() {
        btnUpgradeAccountSettings = findViewById(R.id.btn_update_settings);
        etUsername = findViewById(R.id.et_set_user_name);
        etUserStatus = findViewById(R.id.et_set_profile_status);
        civUserProfileImage = findViewById(R.id.iv_set_profile_image);
        loadingBar = new ProgressDialog(this);
        settingsToolbar = findViewById(R.id.settingsToolbar);
        setSupportActionBar(settingsToolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setTitle("Settings");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == -1 && requestCode == REQUEST_CODE_IMAGE_PICKER) {
            loadingBar.setTitle("Setting profile image");
            loadingBar.setMessage("Updating the profile image...");
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();


            assert data != null;
            String result = data.getStringExtra("RESULT");
            Uri resultUri = null;
            if (result != null) {
                resultUri = Uri.parse(result);
            }

            StorageReference filePath = UserProfileImageRef.child(currentUserID + ".jpg");
            if (resultUri != null) {
                filePath.putFile(resultUri).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        task.getResult().getStorage().getDownloadUrl().addOnSuccessListener(uri -> {
                            String downloadedUrl = uri.toString();
                            RootRef.child("Users").child(currentUserID).child("image").setValue(downloadedUrl).addOnCompleteListener(task1 -> {
                                if (task1.isSuccessful()) {
                                    Toast.makeText(SettingsActivity.this, "Image saved", Toast.LENGTH_SHORT).show();
                                    loadingBar.dismiss();
                                } else {
                                    Toast.makeText(SettingsActivity.this, "Error: " + Objects.requireNonNull(task1.getException()), Toast.LENGTH_SHORT).show();
                                    loadingBar.dismiss();
                                }
                            });
                        }).addOnFailureListener(e -> {
                            String message = e.getMessage();
                            Toast.makeText(SettingsActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                            loadingBar.dismiss();
                        });
                    } else {
                        String message = Objects.requireNonNull(task.getException()).toString();
                        Toast.makeText(SettingsActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                        loadingBar.dismiss();
                    }
                });
            } else {
                Toast.makeText(SettingsActivity.this, "The result uri is empty", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void updateSettings() {
        String setUsername = etUsername.getText().toString().trim();
        String setStatus = etUserStatus.getText().toString().trim();

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

            RootRef.child("Users").child(currentUserID).updateChildren(profileMap)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()){
                            sendToMainActivity();
                            Toast.makeText(SettingsActivity.this, R.string.profile_updated, Toast.LENGTH_SHORT).show();
                        }
                        else {
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
                    Picasso.get().load(retrieveProfileImage).into(civUserProfileImage);

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