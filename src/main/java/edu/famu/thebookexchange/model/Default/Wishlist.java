package edu.famu.thebookexchange.model.Default;

import com.fasterxml.jackson.annotation.JsonProperty;
import edu.famu.thebookexchange.model.Abstracts.AWishlist;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.ArrayList;

@NoArgsConstructor // Be cautious with this if you don't explicitly call super
public class Wishlist extends AWishlist {

    @JsonProperty("userId")
    private String userId;

    public Wishlist(List<String> bookRequests, String userId) {
        super(bookRequests); // Correctly calling the AWishlist constructor
        this.userId = userId;
    }

    // If you have a no-argument constructor, you MUST call super with a List<String>
    public Wishlist() {
        super(new ArrayList<>()); // Calling AWishlist constructor with an empty list
    }

    // Manual Getters
    public String getUserId() {
        return userId;
    }

    // Manual Setters
    public void setUserId(String userId) {
        this.userId = userId;
    }

    // Manual Getters from AWishlist
    public List<String> getBookRequests() {
        return super.getBookRequests();
    }

    // Manual Setters from AWishlist
    public void setBookRequests(List<String> bookRequests) {
        super.setBookRequests(bookRequests);
    }
}