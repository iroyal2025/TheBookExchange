package edu.famu.thebookexchange.model.Default;

import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@NoArgsConstructor
public class Messages {

    private int messageId;
    private String content;
    private Timestamp timestamp;
    private int userId;

    public Messages(int messageId, String content, Timestamp timestamp, int userId) {
        this.messageId = messageId;
        this.content = content;
        this.timestamp = timestamp;
        this.userId = userId;
    }

    // Manual Getters
    public int getMessageId() {
        return messageId;
    }

    public String getContent() {
        return content;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public int getUserId() {
        return userId;
    }

    // Manual Setters
    public void setMessageId(int messageId) {
        this.messageId = messageId;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }
}