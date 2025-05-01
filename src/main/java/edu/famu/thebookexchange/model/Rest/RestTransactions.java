// Rest Class (RestTransactions.java)
package edu.famu.thebookexchange.model.Rest;

import edu.famu.thebookexchange.model.Abstracts.ATransactions;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class RestTransactions extends ATransactions {

    private String bookId;

    private String userId;

    public RestTransactions(String orderStatus, String bookId, String userId) {
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