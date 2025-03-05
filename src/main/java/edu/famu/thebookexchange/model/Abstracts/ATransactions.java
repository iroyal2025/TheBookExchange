// Abstract Class (ATransactions.java)
package edu.famu.thebookexchange.model.Abstracts;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public abstract class ATransactions {

    @JsonProperty("order status")
    private String orderStatus;

    public ATransactions(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    // Manual Getters
    public String getOrderStatus() {
        return orderStatus;
    }

    // Manual Setters
    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }
}