package edu.famu.thebookexchange.model.Rest;

public class RestNotification {
    private String notificationId;
    private String userId;
    private String type;
    private String message;
    private long timestamp; // Storing as seconds since epoch for Firestore compatibility
    private boolean isRead;
    private String link;
    private String relatedItemId;

    // Constructors (default and with fields)
    public RestNotification() {
    }

    public RestNotification(String userId, String type, String message, long timestamp, boolean isRead, String link, String relatedItemId) {
        this.userId = userId;
        this.type = type;
        this.message = message;
        this.timestamp = timestamp;
        this.isRead = isRead;
        this.link = link;
        this.relatedItemId = relatedItemId;
    }

    // Getters
    public String getNotificationId() {
        return notificationId;
    }

    public String getUserId() {
        return userId;
    }

    public String getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public boolean isRead() {
        return isRead;
    }

    public String getLink() {
        return link;
    }

    public String getRelatedItemId() {
        return relatedItemId;
    }

    // Setters
    public void setNotificationId(String notificationId) {
        this.notificationId = notificationId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public void setRelatedItemId(String relatedItemId) {
        this.relatedItemId = relatedItemId;
    }
}