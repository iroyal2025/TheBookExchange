package edu.famu.thebookexchange.model.Rest;

import com.google.cloud.firestore.DocumentReference;
import edu.famu.thebookexchange.model.Abstracts.ABooks;
import java.util.List;

public class RestBooks extends ABooks {

    private String bookId;
    private List<String> ownedBy;
    private String userId; // Changed to String
    private String courseId; // Changed to String

    public RestBooks() {
        super();
    }

    public RestBooks(String title, String author, String edition, String ISBN, String condition, String description, double price, boolean isDigital, String digitalCopyPath, String bookId, List<String> ownedBy, DocumentReference userId, DocumentReference courseId) {
        super(title, author, edition, ISBN, condition, description, price, isDigital, digitalCopyPath);
        this.bookId = bookId;
        this.ownedBy = ownedBy;
        this.userId = userId != null ? userId.getId() : null; // Store ID as String
        this.courseId = courseId != null ? courseId.getId() : null; // Store ID as String
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

    public String getUserId() { // Changed return type to String
        return userId;
    }

    public void setUserId(String userId) { // Changed parameter type to String
        this.userId = userId;
    }

    public String getCourseId() { // Changed return type to String
        return courseId;
    }

    public void setCourseId(String courseId) { // Changed parameter type to String
        this.courseId = courseId;
    }

    @Override
    public String toString() {
        return "RestBooks{" +
                "bookId='" + bookId + '\'' +
                ", ownedBy=" + ownedBy +
                ", userId='" + userId + '\'' + // Changed to String
                ", courseId='" + courseId + '\'' + // Changed to String
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