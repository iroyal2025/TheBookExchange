package edu.famu.thebookexchange.model.Rest;

import edu.famu.thebookexchange.model.Abstracts.ABooks;
import java.util.List;

public class RestBooks extends ABooks {

    private String bookId;
    private List<String> ownedBy;
    private String userId;
    private String courseId;
    private Double rating;
    private Long ratingCount;
    private String addedBy; // New field to store seller's email

    public RestBooks() {
        super();
    }

    public RestBooks(String title, String author, String edition, String ISBN, String condition, String description, double price, boolean isDigital, String digitalCopyPath, String bookId, List<String> ownedBy, String userId, String courseId, Double rating, Long ratingCount) {
        super(title, author, edition, ISBN, condition, description, price, isDigital, digitalCopyPath);
        this.bookId = bookId;
        this.ownedBy = ownedBy;
        this.userId = userId;
        this.courseId = courseId;
        this.rating = rating;
        this.ratingCount = ratingCount;
    }

    // New constructor to accept 'addedBy'
    public RestBooks(String title, String author, String edition, String ISBN, String condition, String description, double price, boolean isDigital, String digitalCopyPath, String bookId, List<String> ownedBy, String userId, String courseId, Double rating, Long ratingCount, String addedBy) {
        super(title, author, edition, ISBN, condition, description, price, isDigital, digitalCopyPath);
        this.bookId = bookId;
        this.ownedBy = ownedBy;
        this.userId = userId;
        this.courseId = courseId;
        this.rating = rating;
        this.ratingCount = ratingCount;
        this.addedBy = addedBy;
    }

    // Getters and setters

    public String getBookId() {
        return bookId;
    }

    public void setBookId(String bookId) {
        this.bookId = bookId;
    }

    public List<String> getOwnedBy() {
        return ownedBy;
    }

    public void setOwnedBy(List<String> ownedBy) {
        this.ownedBy = ownedBy;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public Long getRatingCount() {
        return ratingCount;
    }

    public void setRatingCount(Long ratingCount) {
        this.ratingCount = ratingCount;
    }

    public String getAddedBy() {
        return addedBy;
    }

    public void setAddedBy(String addedBy) {
        this.addedBy = addedBy;
    }

    @Override
    public String toString() {
        return "RestBooks{" +
                "bookId='" + bookId + '\'' +
                ", ownedBy=" + ownedBy +
                ", userId='" + userId + '\'' +
                ", courseId='" + courseId + '\'' +
                ", rating=" + rating +
                ", ratingCount=" + ratingCount +
                ", addedBy='" + addedBy + '\'' +
                ", title='" + getTitle() + '\'' +
                ", author='" + getAuthor() + '\'' +
                ", edition='" + getEdition() + '\'' +
                ", ISBN='" + getISBN() + '\'' +
                ", condition='" + getCondition() + '\'' +
                ", description='" + getDescription() + '\'' +
                ", price=" + getPrice() +
                ", isDigital=" + isDigital() +
                ", digitalCopyPath='" + getDigitalCopyPath() + '\'' +
                '}';
    }
}