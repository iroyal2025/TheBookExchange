package edu.famu.thebookexchange.model.Rest;

import edu.famu.thebookexchange.model.Abstracts.AWishlist;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;

public class RestWishlist extends AWishlist {

    private String userId;
    private String wishlistId;

    // Constructor accepting List<String> and String
    public RestWishlist(List<String> bookRequests, String userId) {
        super(bookRequests); // Call the AWishlist constructor with bookRequests
        this.userId = userId;
    }

    // Constructor accepting List<Map<String, Object>> and String (with dummy parameter)
    public RestWishlist(List<Map<String, Object>> bookDetails, String userId, boolean isBookDetails) {
        super(bookDetails != null ?
                bookDetails.stream().map(book -> (String) book.get("bookId")).toList() :
                new ArrayList<>()); // Call AWishlist constructor with the derived List<String>
        this.userId = userId;
    }

    public RestWishlist() {
        super(new ArrayList<>()); // Call AWishlist constructor with an empty List<String>
    }

    public List<String> getBookRequests() {
        return super.getBookRequests(); // Inherit from AWishlist
    }

    public void setBookRequests(List<String> bookRequests) {
        super.setBookRequests(bookRequests); // Inherit from AWishlist
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getWishlistId() {
        return wishlistId;
    }

    public void setWishlistId(String wishlistId) {
        this.wishlistId = wishlistId;
    }
}