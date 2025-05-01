package edu.famu.thebookexchange.model.Default;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.cloud.firestore.DocumentReference;
import edu.famu.thebookexchange.model.Abstracts.ATransactions;
import edu.famu.thebookexchange.model.Rest.DocumentReferenceDeserializer;
import edu.famu.thebookexchange.model.Rest.DocumentReferenceSerializer;

public class Transactions extends ATransactions {
    private String bookId;

    private String userId;

    public Transactions(String orderStatus, String bookId, String userId) {
        super(orderStatus);
        this.bookId = bookId;
        this.userId = userId;
    }

    // Manual Getters
    public String getBookId() {
        return bookId;
    }

    public String getUserId() {
        return userId;
    }

    // Manual Setters
    public void setBookId(String bookId) {
        this.bookId = bookId;
    }

    public void setUserId(String userId) {
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
