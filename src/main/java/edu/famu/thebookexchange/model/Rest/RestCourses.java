package edu.famu.thebookexchange.model.Rest;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.cloud.firestore.DocumentReference;
import edu.famu.thebookexchange.model.Abstracts.ACourses;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class RestCourses extends ACourses {

    @JsonSerialize(using = DocumentReferenceSerializer.class)
    @JsonDeserialize(using = DocumentReferenceDeserializer.class)
    private DocumentReference userId; // DocumentReference acts as the userId

    public RestCourses(String courseName, DocumentReference userId) {
        super(courseName);
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
}