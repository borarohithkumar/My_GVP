package com.example.mygvp;

public class LostItem {
    private String id;
    private String title;
    private String status;
    private String uploaderName;
    private String uploaderRoll;
    private String message;
    private String imageUrl;
    private long timestamp;

    public LostItem() {}

    public LostItem(String id, String title, String status, String uploaderName, String uploaderRoll, String message, String imageUrl, long timestamp) {
        this.id = id;
        this.title = title;
        this.status = status;
        this.uploaderName = uploaderName;
        this.uploaderRoll = uploaderRoll;
        this.message = message;
        this.imageUrl = imageUrl;
        this.timestamp = timestamp;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getStatus() { return status; }
    public String getUploaderName() { return uploaderName; }
    public String getUploaderRoll() { return uploaderRoll; }
    public String getMessage() { return message; }
    public String getImageUrl() { return imageUrl; }
    public long getTimestamp() { return timestamp; }
}