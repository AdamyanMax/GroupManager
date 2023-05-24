package com.example.manage.Authentication;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.manage.Helpers.FirebaseAuthHelper;
import com.example.manage.Helpers.NavigateUtil;
import com.example.manage.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;

public class SignupFragment extends Fragment {

    private FirebaseAuthHelper mAuthHelper;
    private TextInputLayout tilUserEmail, tilUserPassword, tilUserRepeatPassword;
    private TextInputEditText etUserEmail, etUserPassword, etUserRepeatPassword;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_signup, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAuthHelper = new FirebaseAuthHelper(FirebaseAuth.getInstance());

        MaterialButton btnSignUp = view.findViewById(R.id.btn_signup);

        tilUserEmail = view.findViewById(R.id.til_signup_email);
        tilUserPassword = view.findViewById(R.id.til_signup_password);
        tilUserRepeatPassword = view.findViewById(R.id.til_signup_repeat_password);

        etUserEmail = view.findViewById(R.id.et_signup_email);
        etUserPassword = view.findViewById(R.id.et_signup_password);
        etUserRepeatPassword = view.findViewById(R.id.et_signup_repeat_password);

        btnSignUp.setOnClickListener(v -> createNewAccount());
    }

    private void createNewAccount() {
        String email = etUserEmail.getText() != null ? etUserEmail.getText().toString() : "";
        String password = etUserPassword.getText() != null ? etUserPassword.getText().toString() : "";
        String repeatPassword = etUserRepeatPassword.getText() != null ? etUserRepeatPassword.getText().toString() : "";

        // Clear previous errors
        tilUserEmail.setError(null);
        tilUserPassword.setError(null);
        tilUserRepeatPassword.setError(null);

        if (TextUtils.isEmpty(email)) {
            tilUserEmail.setError("Please enter an email");
        } else if (TextUtils.isEmpty(password)) {
            tilUserPassword.setError("Please enter a password");
        } else if (TextUtils.isEmpty(repeatPassword)) {
            tilUserRepeatPassword.setError("Please confirm your password");
        } else if (!password.equals(repeatPassword)) {
            tilUserRepeatPassword.setError("Passwords do not match");
        } else {
            mAuthHelper.signUp(email, password, new FirebaseAuthHelper.FirebaseAuthSignUpCallback() {
                @Override
                public void onSuccess() {
                    // Handle success
                    NavigateUtil.toMainActivity(getContext());
                }

                @Override
                public void onInvalidEmailFormat() {
                    tilUserEmail.setError("Invalid email format");
                }

                @Override
                public void onWeakPassword() {
                    tilUserPassword.setError("The password is too weak.");
                }

                @Override
                public void onUserCollision() {
                    tilUserEmail.setError("The email address is already in use by another account.");
                }

                @Override
                public void onError(String message) {
                    Toast.makeText(getContext(), "Error: " + message, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}