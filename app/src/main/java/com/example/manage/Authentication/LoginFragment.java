package com.example.manage.Authentication;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.manage.Helpers.FirebaseAuthHelper;
import com.example.manage.Helpers.NavigateUtil;
import com.example.manage.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;

public class LoginFragment extends Fragment {

    private FirebaseAuthHelper mAuthHelper;
    private TextInputLayout tilUserEmail, tilUserPassword;
    private TextInputEditText etUserEmail, etUserPassword;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAuthHelper = new FirebaseAuthHelper(FirebaseAuth.getInstance());

        Button btnLogin = view.findViewById(R.id.btn_login);
        tilUserEmail = view.findViewById(R.id.til_login_email);
        tilUserPassword = view.findViewById(R.id.til_login_password);
        etUserEmail = view.findViewById(R.id.et_login_email);
        etUserPassword = view.findViewById(R.id.et_login_password);

        btnLogin.setOnClickListener(v -> AllowUserToLogin());
    }

    private void AllowUserToLogin() {
        String email = etUserEmail.getText() != null ? etUserEmail.getText().toString() : "";
        String password = etUserPassword.getText() != null ? etUserPassword.getText().toString() : "";

        // Clear previous errors
        tilUserEmail.setError(null);
        tilUserPassword.setError(null);

        if (TextUtils.isEmpty(email)) {
            tilUserEmail.setError("Please enter an email");
        } else if (TextUtils.isEmpty(password)) {
            tilUserPassword.setError("Please enter a password");
        } else {
            mAuthHelper.signIn(email, password, new FirebaseAuthHelper.FirebaseAuthSignInCallback() {
                @Override
                public void onSuccess() {
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    if (user != null) {
                        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
                            if (!task.isSuccessful()) {
                                Log.w("AllowUserToLogin", "Fetching FCM registration token failed", task.getException());
                                return;
                            }

                            // Get new FCM registration token
                            String token = task.getResult();

                            // Save the token in your DB, associated with this user
                            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users");
                            String userId = user.getUid();
                            databaseReference.child(userId).child("device_token").setValue(token);

                            NavigateUtil.toMainActivity(getContext());
                        });
                    } else {
                        // Handle null user scenario
                        Toast.makeText(getContext(), "Error: User is null", Toast.LENGTH_SHORT).show();
                    }
                }


                @Override
                public void onUserDoesNotExist() {
                    tilUserEmail.setError("This account doesn't exist.");
                }

                @Override
                public void onInvalidEmailFormat() {
                    tilUserEmail.setError("The email address is badly formatted.");
                }

                @Override
                public void onWrongPassword() {
                    tilUserPassword.setError("The password is invalid.");
                }

                @Override
                public void onUserDisabled() {
                    tilUserEmail.setError("The user account has been disabled by an administrator.");
                }

                @Override
                public void onUserNotFound() {

                }

                @Override
                public void onError(String message) {
                    tilUserEmail.setError("Error: " + message);
                }
            });
        }
    }
}
