// Abstract Class (AForums.java)
package edu.famu.thebookexchange.model.Abstracts;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;

@Setter
@Data
@NoArgsConstructor
public abstract class AForums {

    @JsonProperty("topic")
    private String topic;

    @JsonProperty("createdAt")
    private Timestamp createdAt;

    public AForums(String topic, Timestamp createdAt) {
        this.topic = topic;
        this.createdAt = createdAt;
    }

    // Manual Getters
    public String getTopic() {
        return topic;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    // Manual Setters (if needed, though @Setter already provides them)
    public void setTopic(String topic) {
        this.topic = topic;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}