// Abstract Class (APosts.java)
package edu.famu.thebookexchange.model.Abstracts;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@NoArgsConstructor
public abstract class APosts {

    @JsonProperty("content")
    private String content;

    @JsonProperty("createdAt")
    private Timestamp createdAt;

    public APosts(String content, Timestamp createdAt) {
        this.content = content;
        this.createdAt = createdAt;
    }

    // Manual Getters
    public String getContent() {
        return content;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    // Manual Setters
    public void setContent(String content) {
        this.content = content;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}
