package com.example.manage.Module;

public class Messages {
    private String from, message, type, to, message_id, time, date, name, fileName, fileSize, status;


    public Messages() {
    }

    public Messages(String from, String message, String type, String to, String messageID, String time, String date, String name, String status) {
        this.from = from;
        this.message = message;
        this.type = type;
        this.to = to;
        this.message_id = messageID;
        this.time = time;
        this.date = date;
        this.name = name;
        this.status = status;
    }

    public Messages(String message, String type, String from, String to, String messageId, String time, String date, String fileName, String fileSize, String status) {
        this.message = message;
        this.type = type;
        this.from = from;
        this.to = to;
        this.message_id = messageId;
        this.time = time;
        this.date = date;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.status = status;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getType() {
        return type;
    }


    public String getTo() {
        return to;
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


    public String getDate() {
        return date;
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


    public String getFileSize() {
        return fileSize;
    }

}
