package edu.famu.thebookexchange.model.Rest;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.cloud.firestore.DocumentReference;

import edu.famu.thebookexchange.model.Abstracts.ABooks;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@NoArgsConstructor
@Getter
@Setter

public class RestBooks extends ABooks {

    @JsonSerialize(using = DocumentReferenceSerializer.class)
    private DocumentReference userId;

    @JsonSerialize(using = DocumentReferenceSerializer.class)
    private DocumentReference courseId;

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