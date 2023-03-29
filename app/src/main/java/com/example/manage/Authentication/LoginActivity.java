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

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private ProgressDialog loadingBar;

    private Button btnLogin, btnGoogleLogin, btnPhoneLogin;
    private EditText etUserEmail, etUserPassword;
    private TextView tvNeedAccountLink, tvForgetPasswordLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
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
        btnGoogleLogin = findViewById(R.id.btn_google_login);
        btnPhoneLogin = findViewById(R.id.btn_phone_login);

        etUserEmail = findViewById(R.id.et_login_email);
        etUserPassword = findViewById(R.id.et_login_password);

        tvNeedAccountLink = findViewById(R.id.tv_login_has_account_link);
        tvForgetPasswordLink = findViewById(R.id.tv_reset_password_link);

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
                    sendToMainActivity();
                    Toast.makeText(this, "Logged in successfully", Toast.LENGTH_SHORT).show();
                    loadingBar.dismiss();
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