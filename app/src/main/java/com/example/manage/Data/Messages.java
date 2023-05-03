package com.example.manage.Data;

public class Messages {
    private String from;
    private String message;
    private String type;
    private String to;
    private String message_id;
    private String time;
    private String date;
    private String name;

    private String fileName;
    private String fileSize;

    public Messages() {
    }

    public Messages(String from, String message, String type, String to, String messageID, String time, String date, String name) {
        this.from = from;
        this.message = message;
        this.type = type;
        this.to = to;
        this.message_id = messageID;
        this.time = time;
        this.date = date;
        this.name = name;
    }

    public Messages(String message, String type, String from, String to, String messageId, String time, String date, String fileName, String fileSize) {
        this.message = message;
        this.type = type;
        this.from = from;
        this.to = to;
        this.message_id = messageId;
        this.time = time;
        this.date = date;
        this.fileName = fileName;
        this.fileSize = fileSize;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getMessage_id() {
        return message_id;
    }

    public void setMessage_id(String message_id) {
        this.message_id = message_id;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileSize() {
        return fileSize;
    }

    public void setFileSize(String fileSize) {
        this.fileSize = fileSize;
    }
}
