package com.example.emsiapp;

public class Document {
    private String name;
    private String url;
    private String type;
    private String date;

    public Document() {
        // Requis pour Firestore
    }

    public Document(String name, String url, String type, String date) {
        this.name = name;
        this.url = url;
        this.type = type;
        this.date = date;
    }

    public String getName() { return name; }
    public String getUrl() { return url; }
    public String getType() { return type; }
    public String getDate() { return date; }
}