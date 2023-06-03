package com.example.manage.Helpers.MessagesHelper;

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

public class MessageFactory {
    @NonNull
    public Map<String, Object> createMessageBody(String messageType, String content, String senderId, String receiverId, String messageId, String time, String date) {

        Map<String, Object> messageBody = new HashMap<>();
        messageBody.put("message", content);
        messageBody.put("type", messageType);
        messageBody.put("from", senderId);
        messageBody.put("to", receiverId);
        messageBody.put("message_id", messageId);
        messageBody.put("time", time);
        messageBody.put("date", date);
        messageBody.put("status", "sent");

        return messageBody;
    }
}
