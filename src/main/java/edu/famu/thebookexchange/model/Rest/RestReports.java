package edu.famu.thebookexchange.model.Rest;

import com.fasterxml.jackson.annotation.JsonProperty;
import edu.famu.thebookexchange.model.Abstracts.APosts;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@NoArgsConstructor
public class RestReports extends APosts {

    @JsonProperty("bookTitle")
    private String bookTitle;

    @JsonProperty("userEmail")
    private String userEmail;

    public RestReports(String content, Timestamp createdAt, String bookTitle, String userEmail) {
        super(content, createdAt);
        this.bookTitle = bookTitle;
        this.userEmail = userEmail;
    }

    // Manual Getters
    public String getBookTitle() {
        return bookTitle;
    }

    public String getUserEmail() {
        return userEmail;
    }

    // Manual Setters
    public void setBookTitle(String bookTitle) {
        this.bookTitle = bookTitle;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    // Manual Getters from APosts
    public String getContent() {
        return super.getContent();
    }

    public Timestamp getCreatedAt() {
        return super.getCreatedAt();
    }

    // Manual Setters from APosts
    public void setContent(String content) {
        super.setContent(content);
    }

    public void setCreatedAt(Timestamp createdAt) {
        super.setCreatedAt(createdAt);
    }
}