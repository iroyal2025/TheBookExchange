// Abstract Class (AWishlist.java)
package edu.famu.thebookexchange.model.Abstracts;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public abstract class AWishlist {

    @JsonProperty("book requests")
    private String bookRequests;

    public AWishlist(String bookRequests) {
        this.bookRequests = bookRequests;
    }

    // Manual Getters
    public String getBookRequests() {
        return bookRequests;
    }

    // Manual Setters
    public void setBookRequests(String bookRequests) {
        this.bookRequests = bookRequests;
    }
}