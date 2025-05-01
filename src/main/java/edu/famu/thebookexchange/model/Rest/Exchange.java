package edu.famu.thebookexchange.model.Rest;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.annotation.DocumentId;

public class Exchange {
    @DocumentId
    private String exchangeId;
    private String offeredBookId;
    private String requestedBookId;
    private String requesterId;
    private String ownerId;
    private String recipientId;
    private String status;
    private Timestamp requestedAt;
    private Timestamp respondedAt;
    private Timestamp createdAt;
    private String acceptedBy;

    // Default constructor (for Firestore)
    public Exchange() {
        this.createdAt = Timestamp.now(); // Initialize createdAt in default constructor as well
    }

    // Constructor for targeted exchange
    public Exchange(String offeredBookId, String requestedBookId, String requesterId, String ownerId, String status, String acceptedBy) {
        this.offeredBookId = offeredBookId;
        this.requestedBookId = requestedBookId;
        this.requesterId = requesterId;
        this.ownerId = ownerId;
        this.status = status;
        this.acceptedBy = acceptedBy;
        this.requestedAt = Timestamp.now();
        this.createdAt = Timestamp.now();
    }

    // Constructor for direct exchange
    public Exchange(String offeredBookId, String requesterId, String recipientId, String status, String acceptedBy) {
        this.offeredBookId = offeredBookId;
        this.requesterId = requesterId;
        this.recipientId = recipientId;
        this.status = status;
        this.acceptedBy = acceptedBy;
        this.requestedAt = Timestamp.now();
        this.createdAt = Timestamp.now();
    }

    public String getExchangeId() {
        return exchangeId;
    }

    public void setExchangeId(String exchangeId) {
        this.exchangeId = exchangeId;
    }

    public String getOfferedBookId() {
        return offeredBookId;
    }

    public void setOfferedBookId(String offeredBookId) {
        this.offeredBookId = offeredBookId;
    }

    public String getRequestedBookId() {
        return requestedBookId;
    }

    public void setRequestedBookId(String requestedBookId) {
        this.requestedBookId = requestedBookId;
    }

    public String getRequesterId() {
        return requesterId;
    }

    public void setRequesterId(String requesterId) {
        this.requesterId = requesterId;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(String recipientId) {
        this.recipientId = recipientId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Timestamp getRequestedAt() {
        return requestedAt;
    }

    public void setRequestedAt(Timestamp requestedAt) {
        this.requestedAt = requestedAt;
    }

    public Timestamp getRespondedAt() {
        return respondedAt;
    }

    public void setRespondedAt(Timestamp respondedAt) {
        this.respondedAt = respondedAt;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public String getAcceptedBy() {
        return acceptedBy;
    }

    public void setAcceptedBy(String acceptedBy) {
        this.acceptedBy = acceptedBy;
    }
}