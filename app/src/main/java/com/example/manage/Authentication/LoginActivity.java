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

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private ProgressDialog loadingBar;
    private DatabaseReference UsersRef;
    private Button btnLogin, btnPhoneLogin;
    private EditText etUserEmail, etUserPassword;
    private TextView tvNeedAccountLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        InitializeFields();

        tvNeedAccountLink.setOnClickListener(v -> sendUserToRegisterActivity());
        btnLogin.setOnClickListener(v -> AllowUserToLogin());
        btnPhoneLogin.setOnClickListener(v -> {
            Intent phoneLoginIntent = new Intent(LoginActivity.this, PhoneLoginActivity.class);
            startActivity(phoneLoginIntent);
        });
    }

    private void InitializeFields() {
        btnLogin = findViewById(R.id.btn_login);
        btnPhoneLogin = findViewById(R.id.btn_phone_login);

        etUserEmail = findViewById(R.id.et_login_email);
        etUserPassword = findViewById(R.id.et_login_password);

        tvNeedAccountLink = findViewById(R.id.tv_login_has_account_link);

        loadingBar = new ProgressDialog(this);
    }

    private void AllowUserToLogin() {
        String email = etUserEmail.getText().toString();
        String password = etUserPassword.getText().toString();

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Please enter an email", Toast.LENGTH_SHORT).show();
        }
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please enter a password", Toast.LENGTH_SHORT).show();
        } else {
            loadingBar.setTitle("Signing in...");
            loadingBar.setMessage("Please wait");
            loadingBar.setCanceledOnTouchOutside(true);
            loadingBar.show();

            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    String deviceToken = String.valueOf(FirebaseMessaging.getInstance().getToken());
                    String currentUserID = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();

                    UsersRef.child(currentUserID).child("device_token")
                            .setValue(deviceToken)
                            .addOnCompleteListener(task1 -> {
                                if (task1.isSuccessful()) {
                                    sendToMainActivity();
                                    Toast.makeText(LoginActivity.this, "Logged in successfully", Toast.LENGTH_SHORT).show();
                                    loadingBar.dismiss();
                                }
                            });


                } else {
                    // TODO: Using try/catch make the error message different for different occasions,
                    //  and more user friendly
                    String message = Objects.requireNonNull(task.getException()).toString();
                    Toast.makeText(LoginActivity.this, "Error : " + message, Toast.LENGTH_SHORT).show();
                    loadingBar.dismiss();
                }
            });
        }
    }

    private void sendToMainActivity() {
        Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

    private void sendUserToRegisterActivity() {
        Intent registerIntent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(registerIntent);
    }
}