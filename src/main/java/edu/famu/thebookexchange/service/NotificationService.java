package edu.famu.thebookexchange.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import edu.famu.thebookexchange.model.Rest.RestNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);
    private Firestore firestore;

    public static final String NOTIFICATIONS_COLLECTION = "notifications";
    private static final int FIRESTORE_TIMEOUT = 5; // seconds

    public NotificationService() {
        this.firestore = FirestoreClient.getFirestore();
    }

    public String addNotification(RestNotification notification) throws InterruptedException, ExecutionException {
        logger.info("Adding notification with details: {}", notification);
        DocumentReference docRef = firestore.collection(NOTIFICATIONS_COLLECTION).document();
        notification.setNotificationId(docRef.getId());
        notification.setTimestamp(LocalDateTime.now().toEpochSecond(ZoneOffset.UTC));
        ApiFuture<WriteResult> result = docRef.set(notification);
        logger.info("Notification added with ID: {} at timestamp: {}", docRef.getId(), notification.getTimestamp());
        return docRef.getId();
    }

    public List<RestNotification> getUserNotifications(String userId) throws InterruptedException, ExecutionException, TimeoutException {
        logger.info("Retrieving notifications for userId: {}", userId);
        CollectionReference notificationsCollection = firestore.collection(NOTIFICATIONS_COLLECTION);
        Query query = notificationsCollection.whereEqualTo("userId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING);
        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        List<QueryDocumentSnapshot> documents;
        try {
            documents = querySnapshot.get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS).getDocuments();
        } catch (TimeoutException e) {
            logger.error("Timeout occurred while getting notifications for user {}: {}", userId, e.getMessage(), e);
            throw e;
        }
        List<RestNotification> notifications = new ArrayList<>();
        for (QueryDocumentSnapshot doc : documents) {
            notifications.add(doc.toObject(RestNotification.class));
        }
        logger.info("Retrieved {} notifications for user {}", notifications.size(), userId);
        return notifications;
    }

    public boolean markNotificationAsRead(String notificationId) throws InterruptedException, ExecutionException, TimeoutException {
        logger.info("Marking notification with ID: {} as read", notificationId);
        DocumentReference docRef = firestore.collection(NOTIFICATIONS_COLLECTION).document(notificationId);
        ApiFuture<WriteResult> result = docRef.update("read", true); // Corrected field name to "read"
        try {
            result.get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS);
            logger.info("Notification {} marked as read successfully.", notificationId);
            return true;
        } catch (ExecutionException e) {
            if (e.getMessage().contains("No document to update")) {
                logger.warn("Notification with ID {} not found.", notificationId);
                return false; // Notification not found
            }
            logger.error("Error marking notification {} as read: {}", notificationId, e.getMessage(), e);
            throw e;
        } catch (TimeoutException e) {
            logger.error("Timeout occurred while marking notification {} as read: {}", notificationId, e.getMessage(), e);
            throw e;
        }
    }

    public String createNotification(String userId, String type, String message, String link, String relatedItemId) throws InterruptedException, ExecutionException {
        logger.info("Creating notification for userId: {}, type: {}, message: {}, link: {}, relatedItemId: {}",
                userId, type, message, link, relatedItemId);
        if (userId == null || userId.trim().isEmpty()) {
            logger.error("Attempted to create notification with an invalid userId.");
            return null; // Or throw IllegalArgumentException
        }
        RestNotification notification = new RestNotification();
        notification.setUserId(userId);
        notification.setType(type);
        notification.setMessage(message);
        notification.setTimestamp(LocalDateTime.now().toEpochSecond(ZoneOffset.UTC));
        notification.setRead(false);
        notification.setLink(link);
        notification.setRelatedItemId(relatedItemId);
        DocumentReference docRef = firestore.collection(NOTIFICATIONS_COLLECTION).document();
        notification.setNotificationId(docRef.getId());
        ApiFuture<WriteResult> result = docRef.set(notification);
        logger.info("Notification created with ID: {} at timestamp: {}", docRef.getId(), notification.getTimestamp());
        return docRef.getId();
    }

    public void handleWishlistMatch(String userId, String wishlistItemTitle, String bookId, String bookTitle) throws InterruptedException, ExecutionException {
        String message = "A book matching your wishlist item '" + wishlistItemTitle + "' ('" + bookTitle + "') has been added!";
        String link = "/books/" + bookId;
        createNotification(userId, "wishlist_match", message, link, bookId);
    }

    public void handleNewBookAdded(List<String> userIds, String bookTitle, String bookId, String sellerName) throws InterruptedException, ExecutionException {
        String message = String.format("A new book '%s' has been added by %s.", bookTitle, sellerName);
        String link = "/books/" + bookId;
        for (String userId : userIds) {
            createNotification(userId, "new_book_added", message, link, bookId);
            logger.info("Generated 'new_book_added' notification for user {} for book ID: {}", userId, bookId);
        }
    }

    public void handleTransactionUpdate(String userId, String transactionId, String bookTitle, String status) throws InterruptedException, ExecutionException {
        String message = "Your transaction for '" + bookTitle + "' has been updated to: " + status;
        String link = "/transactions/" + transactionId;
        createNotification(userId, "transaction_update", message, link, transactionId);
    }

    public boolean deleteNotificationByMessage(String message) throws InterruptedException, ExecutionException, TimeoutException {
        logger.info("Deleting notifications with message: {}", message);
        CollectionReference notificationsCollection = firestore.collection(NOTIFICATIONS_COLLECTION);
        Query query = notificationsCollection.whereEqualTo("message", message);
        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        List<QueryDocumentSnapshot> documents;
        try {
            documents = querySnapshot.get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS).getDocuments();
        } catch (TimeoutException e) {
            logger.error("Timeout occurred while querying notifications with message {}: {}", message, e.getMessage(), e);
            throw e;
        }

        boolean deletedAny = false;
        for (QueryDocumentSnapshot document : documents) {
            ApiFuture<WriteResult> writeResult = firestore.collection(NOTIFICATIONS_COLLECTION).document(document.getId()).delete();
            try {
                writeResult.get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS);
                logger.info("Notification with ID {} and message '{}' deleted successfully.", document.getId(), message);
                deletedAny = true;
            } catch (ExecutionException e) {
                logger.error("Error deleting notification with ID {} and message '{}': {}", document.getId(), message, e.getMessage(), e);
                // Optionally handle individual deletion failures
            } catch (TimeoutException e) {
                logger.error("Timeout occurred while deleting notification with ID {} and message '{}': {}", document.getId(), message, e.getMessage(), e);
                throw e;
            }
        }

        return deletedAny;
    }

    public void notifyStudentBookPurchased(String studentId, String bookTitle, String bookId) throws InterruptedException, ExecutionException {
        String message = String.format("You have successfully purchased '%s'. It has been added to your owned books.", bookTitle);
        String link = "/books/" + bookId; // Link to the book details page
        createNotification(studentId, "book_purchase", message, link, bookId);
        logger.info("Generated 'book_purchase' notification for student {} for book ID: {}", studentId, bookId);
    }

    public void notifyStudentBookDeleted(List<String> userIds, String bookTitle, String bookId) throws InterruptedException, ExecutionException {
        String message = String.format("The book '%s' has been removed from the marketplace.", bookTitle);
        // Optionally link to a general marketplace or search page
        String link = "/marketplace";
        for (String userId : userIds) {
            createNotification(userId, "book_deleted", message, link, bookId);
            logger.info("Generated 'book_deleted' notification for user {} for book ID: {}", userId, bookId);
        }
    }

    public void notifyStudentBookUpdated(List<String> userIds, String originalBookTitle, String newBookTitle, String bookId) throws InterruptedException, ExecutionException {
        String message;
        if (originalBookTitle != null && !originalBookTitle.equals(newBookTitle)) {
            message = String.format("The book listing for '%s' has been updated. It is now listed as '%s'.", originalBookTitle, newBookTitle);
        } else {
            message = String.format("The book listing for '%s' has been updated.", newBookTitle);
        }
        String link = "/books/" + bookId; // Link to the book details page
        for (String userId : userIds) {
            createNotification(userId, "book_updated", message, link, bookId);
            logger.info("Generated 'book_updated' notification for user {} for book ID: {}", userId, bookId);
        }
    }


    public void notifyStudentNewBookAdded(List<String> allStudentIds, String title, String bookId, String sellerEmail) throws InterruptedException, ExecutionException {
        String message = String.format("A new book '%s' has been added by %s.", title, sellerEmail);
        String link = "/books/" + bookId;
        for (String userId : allStudentIds) {
            createNotification(userId, "new_book_added", message, link, bookId);
            logger.info("Generated 'new_book_added' notification for user {} for book ID: {}", userId, bookId);
        }
    }

    public void notifyAdminUserDeleted(String adminUserId, String deletedUserId, String deletedUserEmail) throws InterruptedException, ExecutionException {
        String message = String.format("Admin deleted user with ID '%s' (Email: %s).", deletedUserId, deletedUserEmail);
        String link = "/admin-dashboard/users"; // Link to the user management page
        createNotification(adminUserId, "user_deleted", message, link, deletedUserId);
        logger.info("Generated 'user_deleted' notification for admin {} about deleted user {}.", adminUserId, deletedUserId);
    }

    public void notifyAdminUserEdited(String adminUserId, String editedUserId, String editedUserEmail, String changes) throws InterruptedException, ExecutionException {
        String message = String.format("Admin edited user with ID '%s' (Email: %s). Changes: %s", editedUserId, editedUserEmail, changes);
        String link = "/admin-dashboard/users/edit/" + editedUserId; // Link to the edited user's page
        createNotification(adminUserId, "user_edited", message, link, editedUserId);
        logger.info("Generated 'user_edited' notification for admin {} about edited user {}.", adminUserId, editedUserId);
    }

    public void notifyAdminUserActivated(String adminUserId, String activatedUserId, String activatedUserEmail) throws InterruptedException, ExecutionException {
        String message = String.format("Admin activated user with ID '%s' (Email: %s).", activatedUserId, activatedUserEmail);
        String link = "/admin-dashboard/users/edit/" + activatedUserId; // Link to the activated user's page
        createNotification(adminUserId, "user_activated", message, link, activatedUserId);
        logger.info("Generated 'user_activated' notification for admin {} about activated user {}.", adminUserId, activatedUserId);
    }

    public void notifyAdminUserDeactivated(String adminUserId, String deactivatedUserId, String deactivatedUserEmail) throws InterruptedException, ExecutionException {
        String message = String.format("Admin deactivated user with ID '%s' (Email: %s).", deactivatedUserId, deactivatedUserEmail);
        String link = "/admin-dashboard/users/edit/" + deactivatedUserId; // Link to the deactivated user's page
        createNotification(adminUserId, "user_deactivated", message, link, deactivatedUserId);
        logger.info("Generated 'user_deactivated' notification for admin {} about deactivated user {}.", adminUserId, deactivatedUserId);
    }

    // Removed notifyAdminUserAdded as its logic is now within createNotification called by UsersService

    public List<RestNotification> getAdminNotifications(String adminId) throws InterruptedException, ExecutionException, TimeoutException {
        logger.info("Retrieving admin notifications for adminId: {}", adminId);
        CollectionReference notificationsCollection = firestore.collection(NOTIFICATIONS_COLLECTION);
        Query query = notificationsCollection.whereEqualTo("userId", adminId)
                .orderBy("timestamp", Query.Direction.DESCENDING);

        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        List<QueryDocumentSnapshot> documents;
        try {
            documents = querySnapshot.get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS).getDocuments();
        } catch (TimeoutException e) {
            logger.error("Timeout occurred while getting admin notifications for admin {}: {}", adminId, e.getMessage(), e);
            throw e;
        }
        List<RestNotification> notifications = new ArrayList<>();
        for (QueryDocumentSnapshot doc : documents) {
            notifications.add(doc.toObject(RestNotification.class));
        }
        logger.info("Retrieved {} admin notifications for admin {}", notifications.size(), adminId);
        return notifications;
    }

    public List<RestNotification> getNotificationsForUser(String userId) throws InterruptedException, ExecutionException, TimeoutException {
        return getUserNotifications(userId);
    }
}