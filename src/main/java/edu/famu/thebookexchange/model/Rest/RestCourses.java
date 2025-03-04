package edu.famu.thebookexchange.model.Rest;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.cloud.firestore.DocumentReference;
import edu.famu.thebookexchange.model.Abstracts.ACourses;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class RestCourses extends ACourses {

    @JsonSerialize(using = DocumentReferenceSerializer.class)
    @JsonDeserialize(using = DocumentReferenceDeserializer.class)
    private DocumentReference userId; // DocumentReference acts as the userId

    public RestCourses(String courseName, String textbookList, DocumentReference userId) {
        super(courseName, textbookList);
        this.userId = userId;
    }

    public DocumentReference getUserId() {
        return userId;
    }

    public void setUserId(DocumentReference userId) {
        this.userId = userId;
    }
}
