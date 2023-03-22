package com.example.manage;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class PhoneLoginActivity extends AppCompatActivity {

    private LinearLayout llPhoneLoginP1, llPhoneLoginP2;
    private EditText etPhoneCountryCode, etPhoneNumber, etActivationCode;
    private Button btnSendVerificationCode, btnRegisterWithPhone;

    private ProgressDialog loadingBar;

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private FirebaseAuth mAuth;

    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_login);

        initializeFields();

        btnSendVerificationCode.setOnClickListener(v -> {
            String phoneCountryCode = etPhoneCountryCode.getText().toString();
            String phoneNumber = etPhoneNumber.getText().toString();

            if (phoneCountryCode.isEmpty()) {
                Toast.makeText(this, R.string.please_enter_a_country_code, Toast.LENGTH_SHORT).show();
            } else if (phoneNumber.isEmpty()) {
                Toast.makeText(this, R.string.please_enter_a_phone_number, Toast.LENGTH_SHORT).show();
            } else {
                displayLoadingBarMessage(getString(R.string.phone_verification), getString(R.string.sending_verification_code));

                PhoneAuthOptions options = PhoneAuthOptions.newBuilder(mAuth).setPhoneNumber(phoneCountryCode + phoneNumber)       // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(this)                 // Activity (for callback binding)
                        .setCallbacks(mCallbacks)          // OnVerificationStateChangedCallbacks
                        .build();
                PhoneAuthProvider.verifyPhoneNumber(options);
            }

        });

        btnRegisterWithPhone.setOnClickListener(v -> {
            String verificationCode = etActivationCode.getText().toString();

            if (verificationCode.isEmpty()) {
                Toast.makeText(this, R.string.please_enter_an_activation_code, Toast.LENGTH_SHORT).show();
            } else {
                displayLoadingBarMessage(getString(R.string.code_verification), getString(R.string.verifying_the_verification_code));

                PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, verificationCode);
                signInWithPhoneAuthCredential(credential);
            }
        });

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                loadingBar.dismiss();
                Toast.makeText(PhoneLoginActivity.this, R.string.invalid_phone_number_please_check_if_the_country_code_or_the_phone_number_you_have_provided_are_correct, Toast.LENGTH_SHORT).show();
                llPhoneLoginP1.setVisibility(View.VISIBLE);
                llPhoneLoginP2.setVisibility(View.GONE);
            }

            @Override
            public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken token) {
                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId;
                mResendToken = token;

                loadingBar.dismiss();
                Toast.makeText(PhoneLoginActivity.this, R.string.the_notification_has_been_sent, Toast.LENGTH_SHORT).show();

                llPhoneLoginP1.setVisibility(View.GONE);
                llPhoneLoginP2.setVisibility(View.VISIBLE);
            }
        };

    }

    private void displayLoadingBarMessage(String Code_verification, String message) {
        loadingBar.setTitle(Code_verification);
        loadingBar.setMessage(message);
        loadingBar.setCanceledOnTouchOutside(false);
        loadingBar.show();
    }

    private void initializeFields() {
        etPhoneCountryCode = findViewById(R.id.et_phone_country_code);
        etPhoneNumber = findViewById(R.id.et_phone_number);
        etActivationCode = findViewById(R.id.et_activation_code);

        btnSendVerificationCode = findViewById(R.id.btn_next_send_verification_code);
        btnRegisterWithPhone = findViewById(R.id.btn_register_with_phone);

        loadingBar = new ProgressDialog(this);

        llPhoneLoginP1 = findViewById(R.id.ll_phone_login_p1);
        llPhoneLoginP2 = findViewById(R.id.ll_phone_login_p2);

        mAuth = FirebaseAuth.getInstance();
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    loadingBar.dismiss();
                    sendUserToMainActivity();
                } else {
                    String message = Objects.requireNonNull(task.getException()).toString();
                    Toast.makeText(PhoneLoginActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void sendUserToMainActivity() {
        Intent mainIntent = new Intent(PhoneLoginActivity.this, MainActivity.class);
        startActivity(mainIntent);
        finish();
    }
}