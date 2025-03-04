// Forums.java
package edu.famu.thebookexchange.model.Default;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@NoArgsConstructor
public class Forums {

    private int forumId;
    private String topic;
    private int userId;
    private Timestamp createdAt;

    public Forums(int forumId, String topic, int userId, Timestamp createdAt) {
        this.forumId = forumId;
        this.topic = topic;
        this.userId = userId;
        this.createdAt = createdAt;
    }

    // Manual Getters
    public int getForumId() {
        return forumId;
    }

    public String getTopic() {
        return topic;
    }

    public int getUserId() {
        return userId;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    // Manual Setters
    public void setForumId(int forumId) {
        this.forumId = forumId;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}