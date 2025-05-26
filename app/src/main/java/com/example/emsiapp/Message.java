package com.example.emsiapp;

public class Message {
    public String content;
    public boolean isUser;

    public Message(String content, boolean isUser) {
        this.content = content;
        this.isUser = isUser;
    }
}