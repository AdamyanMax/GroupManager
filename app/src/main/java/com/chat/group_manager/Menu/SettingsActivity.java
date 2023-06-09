package com.chat.group_manager.Menu;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.chat.group_manager.Helpers.FirebaseDatabaseReferences;
import com.chat.group_manager.Helpers.NavigateUtil;
import com.chat.group_manager.Helpers.ProgressBar.TextProgressBarController;
import com.chat.group_manager.MainActivity;
import com.chat.group_manager.Menu.ImageCropper.CropperActivity;
import com.chat.group_manager.R;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_IMAGE_PICKER = 101;
    private final FirebaseDatabaseReferences firebaseDatabaseReferences = new FirebaseDatabaseReferences();
    private ActivityResultLauncher<String> mGetContent;
    private Button btnUpgradeAccountSettings;
    private EditText etUsername, etName, etUserStatus;
    private TextInputLayout tilUsername, tilName, tilProfileStatus;
    private CircleImageView civUserProfileImage;
    private String currentUserID;
    private StorageReference UserProfileImageRef;
    private TextProgressBarController progressBarController;
    private ImageButton ibBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        initializeFields();

        tilUsername.setVisibility(View.GONE);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        currentUserID = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        UserProfileImageRef = FirebaseStorage.getInstance().getReference().child("Profile Images");

        btnUpgradeAccountSettings.setOnClickListener(v -> updateSettings());

        ibBack.setOnClickListener(v -> onBackPressed());

        retrieveUserInfo();

        civUserProfileImage.setOnClickListener(v -> mGetContent.launch("image/*"));
        mGetContent = registerForActivityResult(new ActivityResultContracts.GetContent(), result -> {
            Intent imageCropperActivityIntent = new Intent(SettingsActivity.this, CropperActivity.class);
            imageCropperActivityIntent.putExtra("DATA", result.toString());
            startActivityForResult(imageCropperActivityIntent, 101);
        });
    }

    private void initializeFields() {
        btnUpgradeAccountSettings = findViewById(R.id.btn_update_settings);

        etName = findViewById(R.id.et_set_name);
        etUsername = findViewById(R.id.et_set_user_name);
        etUserStatus = findViewById(R.id.et_set_profile_status);

        tilUsername = findViewById(R.id.til_set_user_name);
        tilName = findViewById(R.id.til_set_name);
        tilProfileStatus = findViewById(R.id.til_set_profile_status);

        civUserProfileImage = findViewById(R.id.iv_set_profile_image);
        progressBarController = new TextProgressBarController(this);
        ibBack = findViewById(R.id.ib_settings_back);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == -1 && requestCode == REQUEST_CODE_IMAGE_PICKER) {
            progressBarController.show("Updating the profile image...");

            assert data != null;
            String result = data.getStringExtra("RESULT");
            Uri resultUri = null;
            if (result != null) {
                resultUri = Uri.parse(result);
            }

            StorageReference filePath = UserProfileImageRef.child(currentUserID + ".jpg");
            if (resultUri != null) {
                UploadTask uploadImageTask = filePath.putFile(resultUri);

                uploadImageTask.addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        task.getResult().getStorage().getDownloadUrl().addOnSuccessListener(uri -> {
                            String downloadedUrl = uri.toString();
                            firebaseDatabaseReferences.getUsersRef().child(currentUserID).child("image").setValue(downloadedUrl).addOnCompleteListener(task1 -> {
                                if (task1.isSuccessful()) {
                                    Toast.makeText(SettingsActivity.this, "Image saved", Toast.LENGTH_SHORT).show();
                                    progressBarController.hide();
                                } else {
                                    Toast.makeText(SettingsActivity.this, "Error: " + Objects.requireNonNull(task1.getException()), Toast.LENGTH_SHORT).show();
                                    progressBarController.hide();
                                }
                            });
                        }).addOnFailureListener(e -> {
                            String message = e.getMessage();
                            Toast.makeText(SettingsActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                            progressBarController.hide();
                        });
                    } else {
                        String message = Objects.requireNonNull(task.getException()).toString();
                        Toast.makeText(SettingsActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                        progressBarController.hide();
                    }
                });
            } else {
                Toast.makeText(SettingsActivity.this, "The result uri is empty", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void createAccount() {
        String setUsername = etUsername.getText().toString().trim().toLowerCase();
        String setName = etName.getText().toString().trim();
        String setStatus = etUserStatus.getText().toString().trim();

        tilUsername.setError(null);
        tilName.setError(null);
        tilProfileStatus.setError(null);

        if (TextUtils.isEmpty(setUsername)) {
            tilUsername.setError(getString(R.string.please_provide_a_username));
            tilUsername.requestFocus();
        } else if (!setUsername.matches("[A-Za-z0-9_]+")) {
            tilUsername.setError(getString(R.string.username_can_only_contain_alphanumeric_characters_and_underscores));
            tilUsername.requestFocus();
        } else if (setUsername.length() > 15) {
            tilUsername.setError(getString(R.string.username_must_be_less_than_15_characters));
            tilUsername.requestFocus();
        } else {
            checkIfUsernameExists(setUsername, isExists -> {
                if (isExists) {
                    tilUsername.setError(getString(R.string.username_already_exists));
                } else if (TextUtils.isEmpty(setName)) {
                    tilName.setError(getString(R.string.please_provide_a_name));
                } else if (TextUtils.isEmpty(setStatus)) {
                    tilProfileStatus.setError(getString(R.string.please_provide_a_status));
                } else {
                    HashMap<String, Object> profileMap = new HashMap<>();
                    profileMap.put("uid", currentUserID);
                    profileMap.put("name", setName);
                    profileMap.put("username", setUsername);
                    profileMap.put("status", setStatus);

                    firebaseDatabaseReferences.getUsersRef().child(currentUserID).updateChildren(profileMap).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(SettingsActivity.this, R.string.account_created, Toast.LENGTH_SHORT).show();
                            NavigateUtil.toMainActivity(getApplicationContext());
                        } else {
                            String message = Objects.requireNonNull(task.getException()).toString();
                            Toast.makeText(SettingsActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        }
    }

    private void updateSettings() {
        String setName = etName.getText().toString().trim();
        String setStatus = etUserStatus.getText().toString().trim();

        tilName.setError(null);
        tilProfileStatus.setError(null);

        if (TextUtils.isEmpty(setName)) {
            tilName.setError(getString(R.string.please_provide_a_name));
            tilName.requestFocus();
        } else if (TextUtils.isEmpty(setStatus)) {
            tilProfileStatus.setError(getString(R.string.please_provide_a_status));
            tilProfileStatus.requestFocus();
        }else {
            HashMap<String, Object> profileMap = new HashMap<>();
            profileMap.put("name", setName);
            profileMap.put("status", setStatus);

            firebaseDatabaseReferences.getUsersRef().child(currentUserID).updateChildren(profileMap).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    sendToMainActivity();
                    Toast.makeText(SettingsActivity.this, R.string.profile_updated, Toast.LENGTH_SHORT).show();
                } else {
                    String message = Objects.requireNonNull(task.getException()).toString();
                    Toast.makeText(SettingsActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void checkIfUsernameExists(String username, OnUsernameCheckListener listener) {
        Query usernameQuery = firebaseDatabaseReferences.getUsersRef().orderByChild("username").equalTo(username);
        usernameQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                listener.onCheck(dataSnapshot.exists());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // log error
                Log.e("checkIfUsernameExists", "onCancelled: " + databaseError.getMessage());
            }
        });
    }

    private void sendToMainActivity() {
        Intent mainIntent = new Intent(SettingsActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

    private void retrieveUserInfo() {
        firebaseDatabaseReferences.getUsersRef().child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.hasChild("name") && snapshot.hasChild("image") && snapshot.hasChild("username")) {
                    String retrieveUsername = Objects.requireNonNull(snapshot.child("username").getValue()).toString();
                    String retrieveName = Objects.requireNonNull(snapshot.child("name").getValue()).toString();
                    String retrieveStatus = Objects.requireNonNull(snapshot.child("status").getValue()).toString();
                    String retrieveProfileImage = Objects.requireNonNull(snapshot.child("image").getValue()).toString();

                    etUsername.setText(retrieveUsername);
                    etName.setText(retrieveName);
                    etUserStatus.setText(retrieveStatus);
                    Picasso.get().load(retrieveProfileImage).placeholder(R.drawable.user_default_profile_pic).into(civUserProfileImage);

                } else if (snapshot.exists() && snapshot.hasChild("name") && snapshot.hasChild("username")) {
                    String retrieveUsername = Objects.requireNonNull(snapshot.child("username").getValue()).toString();
                    String retrieveName = Objects.requireNonNull(snapshot.child("name").getValue()).toString();
                    String retrieveStatus = Objects.requireNonNull(snapshot.child("status").getValue()).toString();

                    etUsername.setText(retrieveUsername);
                    etName.setText(retrieveName);
                    etUserStatus.setText(retrieveStatus);
                } else {
                    tilUsername.setVisibility(View.VISIBLE);
                    ibBack.setVisibility(View.GONE);
                    Toast.makeText(SettingsActivity.this, R.string.please_provide_some_information_about_your_profile, Toast.LENGTH_SHORT).show();
                    btnUpgradeAccountSettings.setOnClickListener(v -> createAccount());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public interface OnUsernameCheckListener {
        void onCheck(boolean isExists);
    }
}