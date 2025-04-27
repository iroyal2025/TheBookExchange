package edu.famu.thebookexchange.model.Rest;

import com.fasterxml.jackson.annotation.JsonProperty;
import edu.famu.thebookexchange.model.Abstracts.APosts;

import java.sql.Timestamp;

public class RestReports extends APosts {

    @JsonProperty("bookTitle")
    private String bookTitle;

    @JsonProperty("userEmail")
    private String userEmail;

    @JsonProperty("sellerEmail")
    private String sellerEmail;

    @JsonProperty("reportedBy")
    private String reportedBy;

    @JsonProperty("reportType")
    private String reportType;

    public RestReports() {
        super(null, null); // Call the APosts constructor with default null values
    }

    public RestReports(String content, Timestamp createdAt, String bookTitle, String userEmail, String sellerEmail, String reportedBy, String reportType) {
        super(content, createdAt);
        this.bookTitle = bookTitle;
        this.userEmail = userEmail;
        this.sellerEmail = sellerEmail;
        this.reportedBy = reportedBy;
        this.reportType = reportType;
    }

    public RestReports(String content, Timestamp createdAt, String bookTitle, String userEmail) {
        super(content, createdAt);
        this.bookTitle = bookTitle;
        this.userEmail = userEmail;
    }

    // Getters
    public String getBookTitle() {
        return bookTitle;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public String getSellerEmail() {
        return sellerEmail;
    }

    public String getReportedBy() {
        return reportedBy;
    }

    public String getReportType() {
        return reportType;
    }

    // Setters
    public void setBookTitle(String bookTitle) {
        this.bookTitle = bookTitle;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public void setSellerEmail(String sellerEmail) {
        this.sellerEmail = sellerEmail;
    }

    public void setReportedBy(String reportedBy) {
        this.reportedBy = reportedBy;
    }

    public void setReportType(String reportType) {
        this.reportType = reportType;
    }

    // Getters from APosts
    public String getContent() {
        return super.getContent();
    }

    public Timestamp getCreatedAt() {
        return super.getCreatedAt();
    }

    // Setters from APosts
    public void setContent(String content) {
        super.setContent(content);
    }

    public void setCreatedAt(Timestamp createdAt) {
        super.setCreatedAt(createdAt);
    }

    @Override
    public String toString() {
        return "RestReports{" +
                "bookTitle='" + bookTitle + '\'' +
                ", userEmail='" + userEmail + '\'' +
                ", sellerEmail='" + sellerEmail + '\'' +
                ", reportedBy='" + reportedBy + '\'' +
                ", reportType='" + reportType + '\'' +
                ", content='" + getContent() + '\'' +
                ", createdAt=" + getCreatedAt() +
                '}';
    }
}