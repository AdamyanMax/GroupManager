package com.example.manage.Helpers;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FirebaseDatabaseReferences {
    private final DatabaseReference rootRef, usersRef, messagesRef, notificationsRef, chatRequestsRef, contactsRef, groupsRef;

    public FirebaseDatabaseReferences() {
        rootRef = FirebaseDatabase.getInstance().getReference();
        usersRef = rootRef.child("Users");
        messagesRef = rootRef.child("Messages");
        notificationsRef = rootRef.child("Notifications");
        chatRequestsRef = rootRef.child("Chat Requests");
        contactsRef = rootRef.child("Contacts");
        groupsRef = rootRef.child("Groups");
    }

    public DatabaseReference getGroupsRef() {
        return groupsRef;
    }

    public DatabaseReference getChatRequestsRef() {
        return chatRequestsRef;
    }

    public DatabaseReference getRootRef() {
        return rootRef;
    }

    public DatabaseReference getContactsRef() {
        return contactsRef;
    }

    public DatabaseReference getUsersRef() {
        return usersRef;
    }

    public DatabaseReference getMessagesRef() {
        return messagesRef;
    }

    public DatabaseReference getNotificationsRef() {
        return notificationsRef;
    }
}
