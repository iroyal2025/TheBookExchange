// RestUsers.java
package edu.famu.thebookexchange.model.Rest;

public class RestUsers {

    private String email;
    private String password;
    private String major;
    private String profilePicture;
    private String role;
    private String userId;
    private boolean isActive;
    private double balance;
    private Double sellerRating; // Add this field
    private Long sellerRatingCount; // Add this field

    public RestUsers() {
    }

    public RestUsers(String email, String password, String major, String profilePicture, String role, String userId, boolean isActive, double balance) {
        this.email = email;
        this.password = password;
        this.major = major;
        this.profilePicture = profilePicture;
        this.role = role;
        this.userId = userId;
        this.isActive = isActive;
        this.balance = balance;
    }

    // Constructor including seller rating fields
    public RestUsers(String email, String password, String major, String profilePicture, String role, String userId, boolean isActive, double balance, Double sellerRating, Long sellerRatingCount) {
        this.email = email;
        this.password = password;
        this.major = major;
        this.profilePicture = profilePicture;
        this.role = role;
        this.userId = userId;
        this.isActive = isActive;
        this.balance = balance;
        this.sellerRating = sellerRating;
        this.sellerRatingCount = sellerRatingCount;
    }

    // Getters and setters
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getMajor() {
        return major;
    }

    public void setMajor(String major) {
        this.major = major;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public Double getSellerRating() {
        return sellerRating;
    }

    public void setSellerRating(Double sellerRating) {
        this.sellerRating = sellerRating;
    }

    public Long getSellerRatingCount() {
        return sellerRatingCount;
    }

    public void setSellerRatingCount(Long sellerRatingCount) {
        this.sellerRatingCount = sellerRatingCount;
    }
}