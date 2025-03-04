package edu.famu.thebookexchange.model.Rest;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.google.cloud.firestore.DocumentReference;
import com.google.firebase.cloud.FirestoreClient;
import java.io.IOException;

public class DocumentReferenceDeserializer extends JsonDeserializer<DocumentReference> {

    @Override
    public DocumentReference deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        String documentPath = jsonParser.getText();
        if (documentPath == null || documentPath.isEmpty()) {
            return null;
        }
        return FirestoreClient.getFirestore().document(documentPath);
    }
}