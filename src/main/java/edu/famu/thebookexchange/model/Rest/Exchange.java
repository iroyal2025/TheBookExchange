package edu.famu.thebookexchange.model.Rest;

import java.time.LocalDateTime;

public class Exchange {
    private String exchangeId;
    private String offeredBookId;
    private String requestedBookId;
    private String requesterId;
    private String ownerId;
    private String status; // "pending", "accepted", "rejected", "completed", "cancelled"
    private LocalDateTime requestedAt;
    private LocalDateTime respondedAt;

    // Default constructor (for Firestore)
    public Exchange() {
    }

    public Exchange(String offeredBookId, String requestedBookId, String requesterId, String ownerId, String status) {
        this.offeredBookId = offeredBookId;
        this.requestedBookId = requestedBookId;
        this.requesterId = requesterId;
        this.ownerId = ownerId;
        this.status = status;
        this.requestedAt = LocalDateTime.now();
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getRequestedAt() {
        return requestedAt;
    }

    public void setRequestedAt(LocalDateTime requestedAt) {
        this.requestedAt = requestedAt;
    }

    public LocalDateTime getRespondedAt() {
        return respondedAt;
    }

    public void setRespondedAt(LocalDateTime respondedAt) {
        this.respondedAt = respondedAt;
    }
}