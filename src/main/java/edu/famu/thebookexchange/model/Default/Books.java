package edu.famu.thebookexchange.model.Default; // Or edu.famu.thebookexchange.model

import com.google.cloud.firestore.DocumentReference;
import edu.famu.thebookexchange.model.Abstracts.ABooks;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Books extends ABooks {
    private DocumentReference userId;
    private DocumentReference courseId;
}