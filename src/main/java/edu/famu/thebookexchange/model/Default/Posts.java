package edu.famu.thebookexchange.model.Default;

import java.sql.Timestamp;

public class Posts {

    private int forumId;
    private int userId;
    private String content;
    private Timestamp createdAt;

    public Posts() {
    }

    public Posts(int forumId, int userId, String content, Timestamp createdAt) {
        this.forumId = forumId;
        this.userId = userId;
        this.content = content;
        this.createdAt = createdAt;
    }

    // Manual Getters
    public int getForumId() {
        return forumId;
    }

    public int getUserId() {
        return userId;
    }

    public String getContent() {
        return content;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    // Manual Setters
    public void setForumId(int forumId) {
        this.forumId = forumId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}