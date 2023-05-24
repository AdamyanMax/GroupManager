package com.example.manage.Helpers;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthEmailException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;

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
                        callback.onSuccess();
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
}
