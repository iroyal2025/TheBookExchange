package edu.famu.thebookexchange.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.*;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import edu.famu.thebookexchange.model.Rest.Exchange;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
    public static final String EXCHANGES_COLLECTION = "Exchanges"; // Changed to public static final
    private static final String OWNED_BOOKS_COLLECTION = "OwnedBooks"; // Assuming you have this collection
    public static final long FIRESTORE_TIMEOUT = 5; // seconds // seconds

    private final Firestore firestore;
    private final NotificationService notificationService;
    private final UsersService usersService; // To get user details for notifications
    private final BooksService bookService; // To get book details for notifications
    private final FirebaseAuth firebaseAuth; // Inject FirebaseAuth

    @Autowired
    public ExchangeService(Firestore firestore, NotificationService notificationService, UsersService usersService, BooksService bookService, FirebaseAuth firebaseAuth) {
        this.firestore = firestore;
        this.notificationService = notificationService;
        this.usersService = usersService;
        this.bookService = bookService;
        this.firebaseAuth = firebaseAuth;
    }

    @PostConstruct
    public void fixRespondedAtTimestamps() {
        logger.info("Starting to check and fix respondedAt timestamps...");
        ApiFuture<QuerySnapshot> future = firestore.collection(EXCHANGES_COLLECTION).get();

        try {
            QuerySnapshot querySnapshot = future.get();
            for (QueryDocumentSnapshot document : querySnapshot) {
                Map<String, Object> data = document.getData();
                if (data.containsKey("respondedAt") && data.get("respondedAt") instanceof Map) {
                    String exchangeId = document.getId();
                    DocumentReference exchangeRef = firestore.collection(EXCHANGES_COLLECTION).document(exchangeId);
                    Map<String, Object> update = new HashMap<>();
                    update.put("respondedAt", FieldValue.serverTimestamp());
                    ApiFuture<WriteResult> updateFuture = exchangeRef.update(update);
                    updateFuture.get();
                    logger.info("Successfully updated respondedAt for exchange ID: {}", exchangeId);
                }
            }
            logger.info("Finished checking and updating respondedAt timestamps.");
        } catch (InterruptedException | ExecutionException e) {
            logger.error("Error getting or updating Exchange documents: {}", e.getMessage());
        }
    }

    @PostConstruct
    public void migrateRespondedAtIfNeeded() {
        logger.info("Starting to check and migrate respondedAt timestamps if needed...");
        ApiFuture<QuerySnapshot> future = firestore.collection(EXCHANGES_COLLECTION).get();

        try {
            QuerySnapshot querySnapshot = future.get();
            for (QueryDocumentSnapshot document : querySnapshot) {
                Map<String, Object> data = document.getData();
                if (data.containsKey("respondedAt") && data.get("respondedAt") instanceof Long) {
                    // Migration logic (same as your standalone script)
                    Long respondedAtLong = document.getLong("respondedAt");
                    Timestamp respondedAtTimestamp = Timestamp.ofTimeSecondsAndNanos(respondedAtLong / 1000, (int) ((respondedAtLong % 1000) * 1000000));

                    Map<String, Object> updates = new HashMap<>();
                    updates.put("respondedAt", respondedAtTimestamp);

                    ApiFuture<WriteResult> writeResult = document.getReference().update(updates);
                    writeResult.get();
                    logger.info("Migrated respondedAt for exchange ID: {}", document.getId());
                }
            }
            logger.info("Finished checking and migrating respondedAt timestamps.");
            // Optionally, you could add a flag or check to ensure this runs only once
        } catch (InterruptedException | ExecutionException e) {
            logger.error("Error during migration of Exchange documents: {}", e.getMessage());
        }
    }

    public String requestExchange(String offeredBookId, String requestedBookId, String requesterId, String ownerId) throws InterruptedException, ExecutionException, TimeoutException {
        Exchange exchange = new Exchange(offeredBookId, requestedBookId, requesterId, ownerId, "pending");
        ApiFuture<DocumentReference> future = firestore.collection(EXCHANGES_COLLECTION).add(exchange);
        DocumentReference exchangeRef = future.get();
        String exchangeId = exchangeRef.getId();
        exchange.setExchangeId(exchangeId);
        logger.info("Exchange requested with ID: {}", exchangeId);

        String requesterEmail = usersService.getUserEmailByUid(requesterId);
        String requestedBookTitle = bookService.getBookTitle(requestedBookId);
        String offeredBookTitle = bookService.getBookTitle(offeredBookId);

        if (requesterEmail != null && requestedBookTitle != null && offeredBookTitle != null) {
            notificationService.createNotification(
                    ownerId,
                    "exchange_requested",
                    String.format("User %s wants to exchange their book '%s' for your book '%s'.",
                            requesterEmail, offeredBookTitle, requestedBookTitle),
                    "/exchange/" + exchangeId,
                    exchangeId
            );
            logger.info("Exchange requested notification sent to user: {}", ownerId);
        } else {
            logger.warn("Could not retrieve user or book details for exchange request notification.");
        }

        return exchangeId;
    }

    public boolean respondToExchange(String exchangeId, String action, String responderId) throws InterruptedException, ExecutionException, TimeoutException {
        logger.info("Attempting to respond to exchange ID: {}, action: {}, responder ID: {}", exchangeId, action, responderId);
        DocumentReference exchangeRef = firestore.collection(EXCHANGES_COLLECTION).document(exchangeId);

        try {
            return firestore.runTransaction(transaction -> {
                DocumentSnapshot exchangeSnapshot = transaction.get(exchangeRef).get();
                if (!exchangeSnapshot.exists()) {
                    logger.warn("Exchange with ID {} not found during transaction.", exchangeId);
                    return false;
                }

                Exchange exchange = exchangeSnapshot.toObject(Exchange.class);
                if (exchange == null) {
                    logger.error("Failed to convert Firestore document to Exchange object for ID: {} during transaction.", exchangeId);
                    return false;
                }

                logger.info("Retrieved exchange from Firestore: {}", exchange);
                logger.info("Exchange status: {}", exchange.getStatus());
                logger.info("Exchange recipientId: {}", exchange.getRecipientId());
                logger.info("Exchange offeredBookId: {}", exchange.getOfferedBookId());
                logger.info("Exchange requestedBookId: {}", exchange.getRequestedBookId());
                logger.info("Exchange requesterId: {}", exchange.getRequesterId());
                logger.info("Exchange ownerId: {}", exchange.getOwnerId());

                // Check if the exchange has already been responded to
                if (!exchange.getStatus().equals("pending")) {
                    logger.warn("Exchange with ID {} has already been responded to (status: {}).", exchangeId, exchange.getStatus());
                    return false; // Indicate failure as it's already responded to
                }

                boolean isTargetedExchange = exchange.getRequestedBookId() != null && exchange.getOwnerId() != null && exchange.getOwnerId().equals(responderId);
                boolean isDirectExchange = exchange.getRequestedBookId() == null && exchange.getRecipientId() == null; // RecipientId should be null at this point for a new acceptance

                logger.info("isTargetedExchange: {}, isDirectExchange: {}", isTargetedExchange, isDirectExchange);

                if (isDirectExchange) {
                    if (action.equals("accepted")) {
                        logger.info("Direct exchange ID {} accepted by user {}. Offered book: {}, Requester: {}",
                                exchangeId, responderId, exchange.getOfferedBookId(), exchange.getRequesterId());

                        transaction.update(exchangeRef, "status", "accepted", "recipientId", responderId, "respondedAt", System.currentTimeMillis() / 1000);

                        logger.info("Calling completeDirectExchange with: offeredBookId={}, offeringUserId={}, acceptingUserId={}, exchangeId={}",
                                exchange.getOfferedBookId(), exchange.getRequesterId(), responderId, exchangeId);

                        return completeDirectExchange(exchange.getOfferedBookId(), exchange.getRequesterId(), responderId, exchangeId);
                    } else if (action.equals("rejected")) {
                        logger.info("Direct exchange ID {} rejected by user {}.", exchangeId, responderId);
                        transaction.update(exchangeRef, "status", "rejected", "respondedAt", System.currentTimeMillis() / 1000);
                        return true;
                    } else {
                        logger.warn("Invalid action '{}' for direct exchange ID: {}", action, exchangeId);
                        return false;
                    }
                } else if (isTargetedExchange) {
                    if (action.equals("accepted")) {
                        logger.info("Targeted exchange ID {} accepted by owner {}. Offered book: {}, Requested book: {}, Requester: {}",
                                exchangeId, responderId, exchange.getOfferedBookId(), exchange.getRequestedBookId(), exchange.getRequesterId());
                        transaction.update(exchangeRef, "status", "accepted", "respondedAt", System.currentTimeMillis() / 1000);
                        return completeExchange(exchange.getOfferedBookId(), exchange.getRequestedBookId(), exchange.getRequesterId(), responderId, exchangeId);
                    } else if (action.equals("rejected")) {
                        logger.info("Targeted exchange ID {} rejected by owner {}.", exchangeId, responderId);
                        transaction.update(exchangeRef, "status", "rejected", "respondedAt", System.currentTimeMillis() / 1000);
                        return true;
                    } else {
                        logger.warn("Invalid action '{}' for targeted exchange ID: {}", action, exchangeId);
                        return false;
                    }
                } else {
                    logger.warn("Attempted to respond to an exchange (ID {}) that is neither direct nor targeted or responder is not the owner.", exchangeId);
                    return false;
                }
            }).get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS);
        } catch (ExecutionException e) {
            logger.error("Error during Firestore transaction for exchange ID {}: {}", exchangeId, e.getMessage());
            return false;
        }
    }
    public boolean completeExchange(String offeredBookId, String requestedBookId, String requesterId, String ownerId, String exchangeId) throws InterruptedException, ExecutionException, TimeoutException {
        WriteBatch batch = firestore.batch();

        // 1. Update offered book owner
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
            return false;
        }

        // 2. Update requested book owner
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
            return false;
        }

        // 3. Update exchange status to completed
        DocumentReference exchangeCompleteRef = firestore.collection(EXCHANGES_COLLECTION).document(exchangeId);
        batch.update(exchangeCompleteRef, "status", "completed");

        ApiFuture<List<WriteResult>> commitFuture = batch.commit();
        commitFuture.get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS);

        logger.info("Exchange with ID {} completed. Book {} now owned by {} and Book {} now owned by {}.",
                exchangeId, offeredBookId, ownerId, requestedBookId, requesterId);

        String requesterEmail = usersService.getUserEmailByUid(requesterId);
        String ownerEmail = usersService.getUserEmailByUid(ownerId);
        String requestedBookTitle = bookService.getBookTitle(requestedBookId);
        String offeredBookTitle = bookService.getBookTitle(offeredBookId);

        if (requesterEmail != null && ownerEmail != null && requestedBookTitle != null && offeredBookTitle != null) {
            notificationService.createNotification(
                    requesterId,
                    "exchange_completed",
                    String.format("Your exchange for '%s' (your '%s') with %s is complete!",
                            requestedBookTitle, offeredBookTitle, ownerEmail),
                    "/owned-books",
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

    public boolean completeDirectExchange(String offeredBookId, String offeringUserId, String acceptingUserId, String exchangeId) throws InterruptedException, ExecutionException, TimeoutException {
        WriteBatch batch = firestore.batch();

        // 1. Find and update the offered book's owner
        Query offeredBookQuery = firestore.collection(OWNED_BOOKS_COLLECTION)
                .whereEqualTo("bookId", offeredBookId)
                .whereEqualTo("userId", offeringUserId)
                .limit(1);
        QuerySnapshot offeredBookSnapshot = offeredBookQuery.get().get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS);
        if (!offeredBookSnapshot.isEmpty()) {
            DocumentReference offeredBookRef = offeredBookSnapshot.getDocuments().get(0).getReference();
            batch.update(offeredBookRef, "userId", acceptingUserId);
        } else {
            logger.warn("Offered book with ID {} not found for user {}", offeredBookId, offeringUserId);
            return false;
        }

        // 2. Update exchange status to completed
        DocumentReference exchangeCompleteRef = firestore.collection(EXCHANGES_COLLECTION).document(exchangeId);
        batch.update(exchangeCompleteRef, "status", "completed");

        ApiFuture<List<WriteResult>> commitFuture = batch.commit();
        commitFuture.get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS);

        logger.info("Direct exchange with ID {} completed. Book {} now owned by {}.",
                exchangeId, offeredBookId, acceptingUserId);

        String offeringUserEmail = usersService.getUserEmailByUid(offeringUserId);
        String acceptingUserEmail = usersService.getUserEmailByUid(acceptingUserId);
        String offeredBookTitle = bookService.getBookTitle(offeredBookId);

        if (offeringUserEmail != null && acceptingUserEmail != null && offeredBookTitle != null) {
            notificationService.createNotification(
                    offeringUserId,
                    "exchange_completed",
                    String.format("Your direct exchange offer for '%s' has been accepted by %s!",
                            offeredBookTitle, acceptingUserEmail),
                    "/owned-books",
                    exchangeId
            );
            notificationService.createNotification(
                    acceptingUserId,
                    "exchange_completed",
                    String.format("You have accepted the direct exchange for '%s' from %s!",
                            offeredBookTitle, offeringUserEmail),
                    "/owned-books",
                    exchangeId
            );
        }

        return true;
    }

    public void createDirectExchangeRequest(String offeredBookId, String requesterId) throws InterruptedException, ExecutionException, TimeoutException {
        try {
            logger.info("Direct exchange requested by user ID: {} for book: {}", requesterId, offeredBookId);

            UserRecord userRecord = firebaseAuth.getUser(requesterId);
            String requesterEmail = userRecord.getEmail();
            logger.info("Requester email: {}", requesterEmail);

            String offeredBookTitle = bookService.getBookTitle(offeredBookId);
            String offeringUserEmail = usersService.getUserEmailByUid(requesterId);
            String ownerId = bookService.getBookOwnerId(offeredBookId);

            // Create a single Exchange document for the direct exchange offer
            Map<String, Object> exchangeData = new HashMap<>();
            exchangeData.put("offeredBookId", offeredBookId);
            exchangeData.put("requesterId", requesterId);
            exchangeData.put("recipientId", null); // No specific recipient yet
            exchangeData.put("status", "pending");
            exchangeData.put("createdAt", FieldValue.serverTimestamp());
            exchangeData.put("ownerId", ownerId); // Owner of the offered book

            DocumentReference exchangeDocument = firestore.collection("Exchanges").document();
            String exchangeId = exchangeDocument.getId();
            exchangeDocument.set(exchangeData);
            logger.info("Direct exchange offer created with ID: {}", exchangeId);

            // Notify all other student users about the offer
            ApiFuture<QuerySnapshot> querySnapshotFuture = firestore.collection("Users").whereEqualTo("role", "student").get();
            QuerySnapshot querySnapshot = querySnapshotFuture.get();

            for (QueryDocumentSnapshot studentDoc : querySnapshot.getDocuments()) {
                String recipientUid = studentDoc.getId();
                String recipientUserId = studentDoc.getString("userId");

                if (recipientUserId != null && !recipientUserId.equals(requesterId)) {
                    if (offeredBookTitle != null) {
                        String message = String.format("User %s is offering their book '%s' for direct exchange.", offeringUserEmail, offeredBookTitle);
                        String link = "/exchange/" + exchangeId; // Link to the single exchange document
                        notificationService.createNotification(recipientUid, "direct_exchange_offer", message, link, exchangeId);
                        logger.info("Direct exchange offer notification sent to user UID: {}", recipientUid);
                    } else {
                        logger.warn("Could not retrieve offered book title for direct exchange offer notification to UID: {}", recipientUid);
                    }
                }
            }

        } catch (InterruptedException | ExecutionException e) {
            logger.error("Error creating direct exchange request: {}", e.getMessage());
            throw e; // Re-throwing the exception for the controller to handle
        } catch (FirebaseAuthException e) {
            logger.error("Error retrieving requester's email: {}", e.getMessage());
            // Improved error handling: Throw a more specific exception
            throw new RuntimeException("Invalid requester ID: " + requesterId, e);
        } catch (TimeoutException e) {
            throw new RuntimeException(e);
        }
    }

    // Method to get all exchange requests for a user (optional)
    public List<Exchange> getExchangesForUser(String userId) throws InterruptedException, ExecutionException, TimeoutException {
        List<Exchange> exchanges = new ArrayList<>();
        Query queryRequester = firestore.collection(EXCHANGES_COLLECTION)
                .whereEqualTo("requesterId", userId);
        QuerySnapshot requesterSnapshot = queryRequester.get().get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS);
        for (QueryDocumentSnapshot doc : requesterSnapshot) {
            exchanges.add(doc.toObject(Exchange.class));
        }

        Query queryOwner = firestore.collection(EXCHANGES_COLLECTION).whereEqualTo("ownerId", userId);
        QuerySnapshot ownerSnapshot = queryOwner.get().get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS);
        for (QueryDocumentSnapshot doc : ownerSnapshot) {
            exchanges.add(doc.toObject(Exchange.class));
        }

        Query queryRecipient = firestore.collection(EXCHANGES_COLLECTION)
                .whereEqualTo("recipientId", userId);
        QuerySnapshot recipientSnapshot = queryRecipient.get().get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS);
        for (QueryDocumentSnapshot doc : recipientSnapshot) {
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