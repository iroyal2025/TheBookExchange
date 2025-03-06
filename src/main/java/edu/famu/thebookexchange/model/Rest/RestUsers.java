package edu.famu.thebookexchange.model.Rest;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.cloud.firestore.DocumentReference;
import edu.famu.thebookexchange.model.Abstracts.AUsers;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class RestUsers extends AUsers {

    @JsonSerialize(using = DocumentReferenceSerializer.class)
    @JsonDeserialize(using = DocumentReferenceDeserializer.class)
    private DocumentReference userId; // DocumentReference acts as the userId

    public RestUsers(String email, String password, String major, String profilePicture, String role, DocumentReference userId) {
        super(email, password, major, profilePicture, role); // Added role to super constructor.
        this.userId = userId;
    }

}