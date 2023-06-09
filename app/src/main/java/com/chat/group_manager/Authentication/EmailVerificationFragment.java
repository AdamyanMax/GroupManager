package com.chat.group_manager.Authentication;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.chat.group_manager.Helpers.FirebaseAuthHelper;
import com.chat.group_manager.Helpers.NavigateUtil;
import com.chat.group_manager.R;
import com.google.firebase.auth.FirebaseAuth;

public class EmailVerificationFragment extends Fragment {
    private FirebaseAuthHelper mAuthHelper;
    private Handler handler;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_email_verification, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView tvNotYou = view.findViewById(R.id.tv_not_you);
        TextView tvPassedEmail = view.findViewById(R.id.tv_passed_email);
        TextView tvRefresh = view.findViewById(R.id.tv_refresh);

        assert getArguments() != null;
        String email = EmailVerificationFragmentArgs.fromBundle(getArguments()).getUserEmail();
        tvPassedEmail.setText(email);

        mAuthHelper = new FirebaseAuthHelper(FirebaseAuth.getInstance());

        tvNotYou.setOnClickListener(v -> {
            if (getActivity() != null) {
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_emailVerificationFragment_to_authFragment);
            }

            mAuthHelper.deleteUser(new FirebaseAuthHelper.FirebaseAuthDeleteUserCallback() {
                @Override
                public void onSuccess() {
                    // Deleted successfully
                }

                @Override
                public void onError(String message) {
                    // Error deleting user
                    Log.e("EmailVerificationFragment", "onError: " + message );
                }
            });

            if (handler != null) {
                handler.removeCallbacksAndMessages(null);
            }
        });

        tvRefresh.setOnClickListener(v -> checkEmailVerificationStatus());

        handler = new Handler();
        handler.postDelayed(() -> {
            if (getActivity() != null) {
                mAuthHelper.deleteUser(new FirebaseAuthHelper.FirebaseAuthDeleteUserCallback() {
                    @Override
                    public void onSuccess() {
                        NavHostFragment.findNavController(EmailVerificationFragment.this)
                                .navigate(R.id.action_emailVerificationFragment_to_authFragment);
                        Toast.makeText(getActivity(), "Took too long to verify the account", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(String message) {
                        Log.e("EmailVerificationFragment", "Couldn't undo creating account: " + message);
                    }
                });
            }
        }, 300000);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
    }

    private void checkEmailVerificationStatus() {
        mAuthHelper.refreshUser(new FirebaseAuthHelper.FirebaseAuthRefreshUserCallback() {
            @Override
            public void onSuccess() {
                if (mAuthHelper.isEmailVerified()) {
                    // Navigate to the next screen or activity
                    NavigateUtil.toMainActivity(getActivity());
                } else {
                    Toast.makeText(getActivity(), "Email is not yet verified", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(String message) {
                Log.e("EmailVerificationFragment", "Couldn't refresh user: " + message);
            }
        });
    }
}
