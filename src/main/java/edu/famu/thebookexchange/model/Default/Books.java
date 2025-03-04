package edu.famu.thebookexchange.model.Default; // Or edu.famu.thebookexchange.model

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.cloud.firestore.DocumentReference;
import edu.famu.thebookexchange.model.Abstracts.ABooks;
import edu.famu.thebookexchange.model.Rest.DocumentReferenceSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Setter
@Getter
@Data
@NoArgsConstructor
@AllArgsConstructor


public class Books extends ABooks {
    @JsonSerialize(using = DocumentReferenceSerializer.class)
    private DocumentReference userId;

    @JsonSerialize(using = DocumentReferenceSerializer.class)
    private DocumentReference courseId;

    public Books(String title, String author, String edition, String ISBN, String condition, String description, double price, boolean isDigital, String digitalCopyPath, DocumentReference userId, DocumentReference courseId) {
        super(title, author, edition, ISBN, condition, description, price, isDigital, digitalCopyPath);
        this.userId = userId;
        this.courseId = courseId;
    }

}
