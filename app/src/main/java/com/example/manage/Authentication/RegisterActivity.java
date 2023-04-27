package com.example.manage.Authentication;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.manage.MainActivity;
import com.example.manage.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.Objects;

public class RegisterActivity extends AppCompatActivity {

    private Button btnCreateAccount;
    private EditText etUserEmail, etUserPassword;
    private TextView tvAlreadyHasAccountLink;

    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;
    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        RootRef = FirebaseDatabase.getInstance().getReference();
        InitializeFields();

        tvAlreadyHasAccountLink.setOnClickListener(v -> sendToLoginActivity());
        btnCreateAccount.setOnClickListener(v -> createNewAccount());
    }

    private void createNewAccount() {
        String email = etUserEmail.getText().toString();
        String password = etUserPassword.getText().toString();

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Please enter an email", Toast.LENGTH_SHORT).show();
        }
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please enter a password", Toast.LENGTH_SHORT).show();
        } else {
            loadingBar.setTitle("Creating a new account");
            loadingBar.setMessage("Please wait");
            loadingBar.setCanceledOnTouchOutside(true);
            loadingBar.show();

            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    String deviceToken = String.valueOf(FirebaseMessaging.getInstance().getToken());

                    String currentUserID = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
                    RootRef.child("Users").child(currentUserID).setValue("");

                    RootRef.child("Users").child(currentUserID).child("device_token").setValue(deviceToken);

                    sendToMainActivity();
                    Toast.makeText(RegisterActivity.this, "Account created successfully", Toast.LENGTH_SHORT).show();
                    loadingBar.dismiss();
                } else {
                    String message = Objects.requireNonNull(task.getException()).toString();
                    Toast.makeText(RegisterActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                    loadingBar.dismiss();
                }
            });
        }
    }

    private void sendToMainActivity() {
        Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

    private void sendToLoginActivity() {
        Intent loginIntent = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(loginIntent);
    }

    private void InitializeFields() {
        btnCreateAccount = findViewById(R.id.btn_register);

        etUserEmail = findViewById(R.id.et_register_email);
        etUserPassword = findViewById(R.id.et_register_password);

        tvAlreadyHasAccountLink = findViewById(R.id.tv_register_need_account_link);

        loadingBar = new ProgressDialog(this);
    }
}