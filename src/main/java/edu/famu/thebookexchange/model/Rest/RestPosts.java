// Rest Class (RestPosts.java)
package edu.famu.thebookexchange.model.Rest;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.cloud.firestore.DocumentReference;
import edu.famu.thebookexchange.model.Abstracts.APosts;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@NoArgsConstructor
public class RestPosts extends APosts {

    @JsonSerialize(using = DocumentReferenceSerializer.class)
    @JsonDeserialize(using = DocumentReferenceDeserializer.class)
    private DocumentReference forumId;

    @JsonSerialize(using = DocumentReferenceSerializer.class)
    @JsonDeserialize(using = DocumentReferenceDeserializer.class)
    private DocumentReference userId;

    public RestPosts(String content, Timestamp createdAt, DocumentReference forumId, DocumentReference userId) {
        super(content, createdAt);
        this.forumId = forumId;
        this.userId = userId;
    }

    // Manual Getters
    public DocumentReference getForumId() {
        return forumId;
    }

    public DocumentReference getUserId() {
        return userId;
    }

    // Manual Setters
    public void setForumId(DocumentReference forumId) {
        this.forumId = forumId;
    }

    public void setUserId(DocumentReference userId) {
        this.userId = userId;
    }

    // Manual Getters from APosts
    public String getContent() {
        return super.getContent();
    }

    public Timestamp getCreatedAt() {
        return super.getCreatedAt();
    }

    // Manual Setters from APosts
    public void setContent(String content) {
        super.setContent(content);
    }

    public void setCreatedAt(Timestamp createdAt) {
        super.setCreatedAt(createdAt);
    }
}