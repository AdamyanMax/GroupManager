package com.example.manage.Helpers;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthEmailException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;

public class FirebaseAuthHelper {

    private final FirebaseAuth mAuth;

    public FirebaseAuthHelper(FirebaseAuth mAuth) {
        this.mAuth = mAuth;
    }


    public void signIn(String email, String password, FirebaseAuthSignInCallback callback) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onSuccess();
                    } else {
                        Exception e = task.getException();
                        if (e != null) {
                            if (e instanceof FirebaseAuthInvalidUserException) {
                                callback.onUserDoesNotExist();
                            } else if (e instanceof FirebaseAuthInvalidCredentialsException) {
                                String errorCode = ((FirebaseAuthInvalidCredentialsException) e).getErrorCode();

                                switch (errorCode) {
                                    case "ERROR_INVALID_EMAIL":
                                        callback.onInvalidEmailFormat();
                                        break;

                                    case "ERROR_WRONG_PASSWORD":
                                        callback.onWrongPassword();
                                        break;

                                    case "ERROR_USER_DISABLED":
                                        callback.onUserDisabled();
                                        break;

                                    case "ERROR_USER_NOT_FOUND":
                                        callback.onUserNotFound();
                                        break;

                                    default:
                                        callback.onError(e.getMessage());
                                        break;
                                }
                            } else if (e instanceof FirebaseAuthEmailException) {
                                callback.onInvalidEmailFormat();
                            } else {
                                callback.onError(e.getMessage());
                            }
                        }
                    }
                });
    }

    public void signUp(String email, String password, FirebaseAuthSignUpCallback callback) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            user.sendEmailVerification()
                                    .addOnCompleteListener(verificationTask -> {
                                        if (verificationTask.isSuccessful()) {
                                            callback.onSuccess();
                                        } else {
                                            // Handle failed verification email sending here, if necessary
                                            Log.e("signUp", "Failed to send verification email.", verificationTask.getException());
                                        }
                                    });
                        } else {
                            Log.e("signUp", "User is unexpectedly null after successful sign up.");
                        }
                    } else {
                        Exception e = task.getException();
                        if (e != null) {
                            if (e instanceof FirebaseAuthUserCollisionException) {
                                callback.onUserCollision();
                            } else if (e instanceof FirebaseAuthWeakPasswordException) {
                                callback.onWeakPassword();
                            } else if (e instanceof FirebaseAuthInvalidCredentialsException) {
                                callback.onInvalidEmailFormat();
                            } else {
                                String errorMessage = e.getMessage() != null ? e.getMessage() : "Unknown error";
                                callback.onError(errorMessage);
                            }
                        }
                    }
                });
    }

    public void deleteUser(FirebaseAuthDeleteUserCallback callback) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            currentUser.delete()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            callback.onSuccess();
                        } else {
                            Log.e("FirebaseAuthHelper", "Error deleting user", task.getException());
                            // Add null check before calling getMessage()
                            String errorMessage = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                            callback.onError(errorMessage);
                        }
                    });
        } else {
            callback.onError("No current user");
        }
    }

    public void refreshUser(FirebaseAuthRefreshUserCallback callback) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            currentUser.reload()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            callback.onSuccess();
                        } else {
                            Log.e("FirebaseAuthHelper", "Error refreshing user", task.getException());
                            String errorMessage = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                            callback.onError(errorMessage);
                        }
                    });
        } else {
            callback.onError("No current user");
        }
    }

    public boolean isEmailVerified() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        return currentUser != null && currentUser.isEmailVerified();
    }

    public interface FirebaseAuthRefreshUserCallback {
        void onSuccess();

        void onError(String message);
    }

    public interface FirebaseAuthSignInCallback {
        void onSuccess();

        void onUserDoesNotExist();

        void onInvalidEmailFormat();

        void onWrongPassword();

        void onUserDisabled();

        void onUserNotFound();

        void onError(String message);
    }

    public interface FirebaseAuthSignUpCallback {
        void onSuccess();

        void onInvalidEmailFormat();

        void onWeakPassword();

        void onUserCollision();

        void onError(String message);
    }

    public interface FirebaseAuthDeleteUserCallback {
        void onSuccess();

        void onError(String message);
    }
}
