// Rest Class (RestWishlist.java)
package edu.famu.thebookexchange.model.Rest;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.cloud.firestore.DocumentReference;
import edu.famu.thebookexchange.model.Abstracts.AWishlist;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class RestWishlist extends AWishlist {

    @JsonSerialize(using = DocumentReferenceSerializer.class)
    @JsonDeserialize(using = DocumentReferenceDeserializer.class)
    private DocumentReference userId;

    public RestWishlist(String bookRequests, DocumentReference userId) {
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