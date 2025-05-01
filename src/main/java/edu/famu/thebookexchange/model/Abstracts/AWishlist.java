// Abstract Class (AWishlist.java)
package edu.famu.thebookexchange.model.Abstracts;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
public abstract class AWishlist {

    @JsonProperty("bookRequests")
    private List<String> bookRequests; // Changed to List<String>

    public AWishlist(List<String> bookRequests) {
        this.bookRequests = bookRequests;
    }

    // Manual Getters
    public List<String> getBookRequests() {
        return bookRequests;
    }

    // Manual Setters
    public void setBookRequests(List<String> bookRequests) {
        this.bookRequests = bookRequests;
    }
}