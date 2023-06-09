package com.chat.group_manager.Helpers;

public interface OperationCallback {
    void onSuccess();
    void onFailure(Exception error);
}
