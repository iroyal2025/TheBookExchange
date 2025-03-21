// edu.famu.thebookexchange.model.Rest.RestBooks.java
package edu.famu.thebookexchange.model.Rest;

import edu.famu.thebookexchange.model.Abstracts.ABooks;


import java.util.List;


public class RestBooks extends ABooks {

    private String bookId;
    private List<String> ownedBy;
    private String userId;
    private String courseId;

    public RestBooks() {
        super();
    }

    public RestBooks(String title, String author, String edition, String ISBN, String condition, String description, double price, boolean isDigital, String digitalCopyPath, String bookId, List<String> ownedBy, String userId, String courseId) {
        super(title, author, edition, ISBN, condition, description, price, isDigital, digitalCopyPath);
        this.bookId = bookId;
        this.ownedBy = ownedBy;
        this.userId = userId;
        this.courseId = courseId;
    }

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

    @Override
    public String toString() {
        return "RestBooks{" +
                "bookId='" + bookId + '\'' +
                ", ownedBy=" + ownedBy +
                ", userId='" + userId + '\'' +
                ", courseId='" + courseId + '\'' +
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