// Rest Class (RestTransactions.java)
package edu.famu.thebookexchange.model.Rest;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.cloud.firestore.DocumentReference;
import edu.famu.thebookexchange.model.Abstracts.ATransactions;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class RestTransactions extends ATransactions {

    @JsonSerialize(using = DocumentReferenceSerializer.class)
    @JsonDeserialize(using = DocumentReferenceDeserializer.class)
    private DocumentReference bookId;

    @JsonSerialize(using = DocumentReferenceSerializer.class)
    @JsonDeserialize(using = DocumentReferenceDeserializer.class)
    private DocumentReference userId;

    public RestTransactions(String orderStatus, DocumentReference bookId, DocumentReference userId) {
        super(orderStatus);
        this.bookId = bookId;
        this.userId = userId;
    }

    // Manual Getters
    public DocumentReference getBookId() {
        return bookId;
    }

    public DocumentReference getUserId() {
        return userId;
    }

    // Manual Setters
    public void setBookId(DocumentReference bookId) {
        this.bookId = bookId;
    }

    public void setUserId(DocumentReference userId) {
        this.userId = userId;
    }

    // Manual Getters from ATransactions
    public String getOrderStatus() {
        return super.getOrderStatus();
    }

    // Manual Setters from ATransactions
    public void setOrderStatus(String orderStatus) {
        super.setOrderStatus(orderStatus);
    }
}