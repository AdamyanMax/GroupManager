package com.example.manage.Authentication;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.manage.Helpers.FirebaseAuthHelper;
import com.example.manage.Helpers.NavigateUtil;
import com.example.manage.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;

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
                    NavigateUtil.toMainActivity(getContext());
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
