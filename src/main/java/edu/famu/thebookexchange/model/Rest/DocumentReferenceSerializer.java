package edu.famu.thebookexchange.model.Rest;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.google.cloud.firestore.DocumentReference;
import java.io.IOException;

public class DocumentReferenceSerializer extends JsonSerializer<DocumentReference> {

    @Override
    public void serialize(DocumentReference value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (value == null) {
            gen.writeNull();
        } else {
            gen.writeString(value.getPath()); // Serialize as the document path
        }
    }
}
