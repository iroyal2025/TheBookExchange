

package edu.famu.thebookexchange.model.Abstracts;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@NoArgsConstructor
public abstract class AMessages {

    @JsonProperty("context")
    private String context;

    @JsonProperty("timestamp")
    private Timestamp timestamp;

    public AMessages(String content, Timestamp timestamp) {
        this.context = content;
        this.timestamp = timestamp;
    }

    // Manual Getters
    public String getContent() {
        return context;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    // Manual Setters
    public void setContent(String content) {
        this.context = content;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }
}