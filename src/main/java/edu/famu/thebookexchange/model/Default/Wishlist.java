package edu.famu.thebookexchange.model.Default;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.cloud.firestore.DocumentReference;
import edu.famu.thebookexchange.model.Abstracts.AWishlist;
import edu.famu.thebookexchange.model.Rest.DocumentReferenceDeserializer;
import edu.famu.thebookexchange.model.Rest.DocumentReferenceSerializer;

public class Wishlist extends AWishlist {

    @JsonSerialize(using = DocumentReferenceSerializer.class)
    @JsonDeserialize(using = DocumentReferenceDeserializer.class)
    private DocumentReference userId;
    public Wishlist(String bookRequests, DocumentReference userId) {
        super(bookRequests);
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

    // Manual Getters from AWishlist
    public String getBookRequests() {
        return super.getBookRequests();
    }

    // Manual Setters from AWishlist
    public void setBookRequests(String bookRequests) {
        super.setBookRequests(bookRequests);
    }
}

