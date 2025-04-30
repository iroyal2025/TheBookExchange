package edu.famu.thebookexchange.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import edu.famu.thebookexchange.model.Rest.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
public class ExchangeService {

    private static final Logger logger = LoggerFactory.getLogger(ExchangeService.class);
    private static final String EXCHANGES_COLLECTION = "Exchanges";
    private static final String OWNED_BOOKS_COLLECTION = "OwnedBooks"; // Assuming you have this collection
    private static final int FIRESTORE_TIMEOUT = 5; // seconds

    private final Firestore firestore;
    private final NotificationService notificationService;
    private final UsersService usersService; // To get user details for notifications
    private final BooksService bookService; // To get book details for notifications

    @Autowired
    public ExchangeService(Firestore firestore, NotificationService notificationService, UsersService usersService, BooksService bookService) {
        this.firestore = firestore;
        this.notificationService = notificationService;
        this.usersService = usersService;
        this.bookService = bookService;
    }

    public String requestExchange(String offeredBookId, String requestedBookId, String requesterId, String ownerId) throws InterruptedException, ExecutionException, TimeoutException {
        Exchange exchange = new Exchange(offeredBookId, requestedBookId, requesterId, ownerId, "pending");
        ApiFuture<DocumentReference> future = firestore.collection(EXCHANGES_COLLECTION).add(exchange);
        DocumentReference exchangeRef = future.get();
        String exchangeId = exchangeRef.getId();
        exchange.setExchangeId(exchangeId);
        logger.info("Exchange requested with ID: {}", exchangeId);

        // Expedite notification: Directly include relevant details
        String requesterEmail = usersService.getUserIdByEmail(requesterId); // Assuming getUserIdByEmail returns email now or adjust
        String requestedBookTitle = bookService.getBookTitle(requestedBookId); // Assuming you have a method to get book title
        String offeredBookTitle = bookService.getBookTitle(offeredBookId);

        if (requesterEmail != null && requestedBookTitle != null && offeredBookTitle != null) {
            notificationService.createNotification(
                    ownerId,
                    "exchange_requested",
                    String.format("User %s wants to exchange their book '%s' for your book '%s'.",
                            requesterEmail, offeredBookTitle, requestedBookTitle),
                    "/exchange/" + exchangeId, // Link to the exchange details
                    exchangeId
            );
            logger.info("Exchange requested notification sent to user: {}", ownerId);
        } else {
            logger.warn("Could not retrieve user or book details for exchange request notification.");
        }

        return exchangeId;
    }

    public boolean respondToExchange(String exchangeId, String action, String responderId) throws InterruptedException, ExecutionException, TimeoutException {
        DocumentReference exchangeRef = firestore.collection(EXCHANGES_COLLECTION).document(exchangeId);
        ApiFuture<DocumentSnapshot> exchangeSnapshotFuture = exchangeRef.get();
        DocumentSnapshot exchangeSnapshot = exchangeSnapshotFuture.get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS);

        if (exchangeSnapshot.exists()) {
            Exchange exchange = exchangeSnapshot.toObject(Exchange.class);
            if (exchange.getOwnerId().equals(responderId) && exchange.getStatus().equals("pending")) {
                Map<String, Object> updates = new HashMap<>();
                updates.put("status", action);
                updates.put("respondedAt", LocalDateTime.now());
                ApiFuture<WriteResult> updateFuture = exchangeRef.update(updates);
                updateFuture.get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS);
                logger.info("Exchange with ID {} responded to with action: {}", exchangeId, action);

                // Expedite notification
                String requesterId = exchange.getRequesterId();
                String responderEmail = usersService.getUserIdByEmail(responderId);
                String requestedBookTitle = bookService.getBookTitle(exchange.getRequestedBookId());
                String offeredBookTitle = bookService.getBookTitle(exchange.getOfferedBookId());

                if (responderEmail != null && requestedBookTitle != null && offeredBookTitle != null) {
                    notificationService.createNotification(
                            requesterId,
                            "exchange_response",
                            String.format("User %s has %s your exchange request for '%s' (your '%s').",
                                    responderEmail, action, requestedBookTitle, offeredBookTitle),
                            "/exchange/" + exchangeId,
                            exchangeId
                    );
                    logger.info("Exchange response notification sent to user: {}", requesterId);
                } else {
                    logger.warn("Could not retrieve user or book details for exchange response notification.");
                }

                if (action.equals("accepted")) {
                    return completeExchange(exchange.getOfferedBookId(), exchange.getRequestedBookId(), exchange.getRequesterId(), exchange.getOwnerId(), exchangeId);
                }
                return true; // Rejected or already handled accepted
            } else {
                logger.warn("Invalid exchange response attempt for ID: {}", exchangeId);
                return false;
            }
        } else {
            logger.warn("Exchange with ID {} not found.", exchangeId);
            return false;
        }
    }

    private boolean completeExchange(String offeredBookId, String requestedBookId, String requesterId, String ownerId, String exchangeId) throws InterruptedException, ExecutionException, TimeoutException {
        WriteBatch batch = firestore.batch();

        // 1. Find and update the offered book's owner
        Query offeredBookQuery = firestore.collection(OWNED_BOOKS_COLLECTION)
                .whereEqualTo("bookId", offeredBookId)
                .whereEqualTo("userId", requesterId)
                .limit(1);
        QuerySnapshot offeredBookSnapshot = offeredBookQuery.get().get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS);
        if (!offeredBookSnapshot.isEmpty()) {
            DocumentReference offeredBookRef = offeredBookSnapshot.getDocuments().get(0).getReference();
            batch.update(offeredBookRef, "userId", ownerId);
        } else {
            logger.warn("Offered book with ID {} not found for user {}", offeredBookId, requesterId);
            return false; // Or throw an exception
        }

        // 2. Find and update the requested book's owner
        Query requestedBookQuery = firestore.collection(OWNED_BOOKS_COLLECTION)
                .whereEqualTo("bookId", requestedBookId)
                .whereEqualTo("userId", ownerId)
                .limit(1);
        QuerySnapshot requestedBookSnapshot = requestedBookQuery.get().get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS);
        if (!requestedBookSnapshot.isEmpty()) {
            DocumentReference requestedBookRef = requestedBookSnapshot.getDocuments().get(0).getReference();
            batch.update(requestedBookRef, "userId", requesterId);
        } else {
            logger.warn("Requested book with ID {} not found for user {}", requestedBookId, ownerId);
            return false; // Or throw an exception
        }

        // 3. Update exchange status to completed
        DocumentReference exchangeCompleteRef = firestore.collection(EXCHANGES_COLLECTION).document(exchangeId);
        batch.update(exchangeCompleteRef, "status", "completed");

        // Commit the batch
        ApiFuture<List<WriteResult>> commitFuture = batch.commit();
        commitFuture.get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS);

        logger.info("Exchange with ID {} completed. Book {} now owned by {} and Book {} now owned by {}.",
                exchangeId, offeredBookId, ownerId, requestedBookId, requesterId);

        // Notifications for completion
        String requesterEmail = usersService.getUserIdByEmail(requesterId);
        String ownerEmail = usersService.getUserIdByEmail(ownerId);
        String requestedBookTitle = bookService.getBookTitle(requestedBookId);
        String offeredBookTitle = bookService.getBookTitle(offeredBookId);

        if (requesterEmail != null && ownerEmail != null && requestedBookTitle != null && offeredBookTitle != null) {
            notificationService.createNotification(
                    requesterId,
                    "exchange_completed",
                    String.format("Your exchange for '%s' (your '%s') with %s is complete!",
                            requestedBookTitle, offeredBookTitle, ownerEmail),
                    "/owned-books", // Link to owned books
                    exchangeId
            );
            notificationService.createNotification(
                    ownerId,
                    "exchange_completed",
                    String.format("Your exchange of '%s' for '%s' (from %s) is complete!",
                            requestedBookTitle, offeredBookTitle, requesterEmail),
                    "/owned-books",
                    exchangeId
            );
        }

        return true;
    }

    public void createDirectExchangeRequest(String offeredBookId, String recipientEmail, String requesterEmail) throws InterruptedException, ExecutionException, TimeoutException {
        logger.info("Creating direct exchange request:");
        logger.info("  Offered Book ID: {}", offeredBookId);
        logger.info("  Recipient Email: {}", recipientEmail);
        logger.info("  Requester Email: {}", requesterEmail);

        Map<String, Object> exchangeData = new HashMap<>();
        exchangeData.put("offeredBookId", offeredBookId);
        exchangeData.put("recipientEmail", recipientEmail);
        exchangeData.put("requesterEmail", requesterEmail);
        exchangeData.put("status", "pending");
        exchangeData.put("createdAt", LocalDateTime.now());

        ApiFuture<DocumentReference> future = firestore.collection(EXCHANGES_COLLECTION).add(exchangeData);
        String exchangeId = future.get().getId();
        logger.info("Direct exchange request created with ID: {}", exchangeId);

        // Send notification to the recipient
        String offeredBookTitle = bookService.getBookTitle(offeredBookId);
        if (offeredBookTitle != null) {
            notificationService.createNotification(
                    recipientEmail,
                    "direct_exchange_requested",
                    String.format("User %s wants to exchange their book '%s' with you.",
                            requesterEmail, offeredBookTitle),
                    "/exchange/" + exchangeId, // Link to exchange details (to be implemented)
                    exchangeId
            );
            logger.info("Direct exchange request notification sent to: {}", recipientEmail);
        } else {
            logger.warn("Could not retrieve offered book title for direct exchange notification.");
        }
    }

    // Method to get all exchange requests for a user (optional)
    public List<Exchange> getExchangesForUser(String userId) throws InterruptedException, ExecutionException, TimeoutException {
        List<Exchange> exchanges = new ArrayList<>();
        // Fetch exchanges where the user is either the requester or the owner
        Query queryRequester = firestore.collection(EXCHANGES_COLLECTION)
                .whereEqualTo("requesterId", userId);
        QuerySnapshot requesterSnapshot = queryRequester.get().get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS);
        for (QueryDocumentSnapshot doc : requesterSnapshot) {
            exchanges.add(doc.toObject(Exchange.class));
        }

        Query queryOwner = firestore.collection(EXCHANGES_COLLECTION)
                .whereEqualTo("ownerId", userId);
        QuerySnapshot ownerSnapshot = queryOwner.get().get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS);
        for (QueryDocumentSnapshot doc : ownerSnapshot) {
            exchanges.add(doc.toObject(Exchange.class));
        }
        return exchanges;
    }

    // Method to get a specific exchange by ID (optional)
    public Exchange getExchangeById(String exchangeId) throws InterruptedException, ExecutionException, TimeoutException {
        DocumentReference exchangeRef = firestore.collection(EXCHANGES_COLLECTION).document(exchangeId);
        DocumentSnapshot exchangeSnapshot = exchangeRef.get().get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS);
        if (exchangeSnapshot.exists()) {
            return exchangeSnapshot.toObject(Exchange.class);
        } else {
            logger.warn("Exchange with ID {} not found.", exchangeId);
            return null;
        }
    }
}