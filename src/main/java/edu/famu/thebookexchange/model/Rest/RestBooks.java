package edu.famu.thebookexchange.model.Rest;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.cloud.firestore.DocumentReference;
import edu.famu.thebookexchange.model.Abstracts.ABooks;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class RestBooks extends ABooks {

    @JsonSerialize(using = DocumentReferenceSerializer.class)
    @JsonDeserialize(using = DocumentReferenceDeserializer.class)
    private DocumentReference userId;

    @JsonSerialize(using = DocumentReferenceSerializer.class)
    @JsonDeserialize(using = DocumentReferenceDeserializer.class)
    private DocumentReference courseId;

    public RestBooks(String title, String author, String edition, String ISBN, String condition, String description, double price, boolean isDigital, String digitalCopyPath, DocumentReference userId, DocumentReference courseId) {
        super(title, author, edition, ISBN, condition, description, price, isDigital, digitalCopyPath);
        this.userId = userId;
        this.courseId = courseId;
    }

    public DocumentReference getUserId() {
        return userId;
    }

    public DocumentReference getCourseId() {
        return courseId;
    }

    public void setUserId(DocumentReference userId) {
        this.userId = userId;
    }

    public void setCourseId(DocumentReference courseId) {
        this.courseId = courseId;
    }
}