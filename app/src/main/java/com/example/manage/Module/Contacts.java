package com.example.manage.Module;

public class Contacts {
    public String name, username, status, image;


    public Contacts() {
    }

    public Contacts(String name, String status, String image) {
        this.name = name;
        this.status = status;
        this.image = image;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getImage() {
        return image;
    }

}
