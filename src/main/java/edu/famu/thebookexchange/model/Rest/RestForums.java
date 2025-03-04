package edu.famu.thebookexchange.model.Rest;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.cloud.firestore.DocumentReference;
import edu.famu.thebookexchange.model.Abstracts.AForums;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;

@Setter
@Getter
@NoArgsConstructor
public class RestForums extends AForums {

    @JsonSerialize(using = DocumentReferenceSerializer.class)
    @JsonDeserialize(using = DocumentReferenceDeserializer.class)
    private DocumentReference userId;

    public RestForums(String topic, Timestamp createdAt, DocumentReference userId) {
        super(topic, createdAt);
        this.userId = userId;
    }

    // Manual Getters
    public DocumentReference getUserId() {
        return userId;
    }

    // Manual Setters
    public void setUserId(DocumentReference userId) {
        this.userId = userId;
    }

    // Manual Getters from AForums
    public String getTopic() {
        return super.getTopic();
    }

    public Timestamp getCreatedAt() {
        return super.getCreatedAt();
    }

    // Manual Setters from AForums (if needed)
    public void setTopic(String topic) {
        super.setTopic(topic);
    }

    public void setCreatedAt(Timestamp createdAt) {
        super.setCreatedAt(createdAt);
    }
}