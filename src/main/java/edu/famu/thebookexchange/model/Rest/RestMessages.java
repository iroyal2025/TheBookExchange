package edu.famu.thebookexchange.model.Rest;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.cloud.firestore.DocumentReference;
import edu.famu.thebookexchange.model.Abstracts.AMessages;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@NoArgsConstructor
public class RestMessages extends AMessages {

    @JsonSerialize(using = DocumentReferenceSerializer.class)
    @JsonDeserialize(using = DocumentReferenceDeserializer.class)
    private DocumentReference userId;

    public RestMessages(String context, Timestamp timestamp, DocumentReference userId) {
        super(context, timestamp);
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

    // Manual Getters from AMessages
    public String getContent() {
        return super.getContent();
    }

    public Timestamp getTimestamp() {
        return super.getTimestamp();
    }

    // Manual Setters from AMessages
    public void setContent(String context) {
        super.setContent(context);
    }

    public void setTimestamp(Timestamp timestamp) {
        super.setTimestamp(timestamp);
    }
}