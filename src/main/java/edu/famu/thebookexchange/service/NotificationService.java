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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);
    private Firestore firestore;

    private static final String NOTIFICATIONS_COLLECTION = "notifications";
    private static final int FIRESTORE_TIMEOUT = 5; // seconds

    public NotificationService() {
        this.firestore = FirestoreClient.getFirestore();
    }

    public String addNotification(RestNotification notification) throws InterruptedException, ExecutionException {
        logger.info("Adding notification with details: {}", notification);
        DocumentReference docRef = firestore.collection(NOTIFICATIONS_COLLECTION).document();
        notification.setNotificationId(docRef.getId());
        // Set the timestamp to the current time when the notification is added
        notification.setTimestamp(LocalDateTime.now().toEpochSecond(ZoneOffset.UTC));
        ApiFuture<WriteResult> result = docRef.set(notification);
        logger.info("Notification added with ID: {} at timestamp: {}", docRef.getId(), notification.getTimestamp());
        return docRef.getId();
    }

    public List<RestNotification> getUserNotifications(String userId) throws InterruptedException, ExecutionException, TimeoutException {
        logger.info("Retrieving notifications for userId: {}", userId);
        CollectionReference notificationsCollection = firestore.collection(NOTIFICATIONS_COLLECTION);
        Query query = notificationsCollection.whereEqualTo("userId", userId).orderBy("timestamp", Query.Direction.DESCENDING);
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
        ApiFuture<WriteResult> result = docRef.update("isRead", true);
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
        RestNotification notification = new RestNotification();
        notification.setUserId(userId);
        notification.setType(type);
        notification.setMessage(message);
        // Set the timestamp to the current time when the notification is created
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

    // Example of how you might call this from other services (e.g., BooksService)
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

    public void notifyAdminUserDeleted(String adminId, String deletedUserId, String deletedUserEmail) throws InterruptedException, ExecutionException {
        String message = String.format("Admin '%s' deleted user with ID '%s' (Email: %s).", adminId, deletedUserId, deletedUserEmail);
        String link = "/admin-dashboard/users"; // Link to the user management page
        createNotification(deletedUserId, "user_deleted", message, link, deletedUserId); // Changed userId to deletedUserId
        logger.info("Generated 'user_deleted' notification for admin {} about deleted user {}.", adminId, deletedUserId);
    }

    public void notifyAdminUserEdited(String adminId, String editedUserId, String editedUserEmail, String changes) throws InterruptedException, ExecutionException {
        String message = String.format("Admin '%s' edited user with ID '%s' (Email: %s). Changes: %s", adminId, editedUserId, editedUserEmail, changes);
        String link = "/admin-dashboard/users/edit/" + editedUserId; // Link to the edited user's page
        createNotification(editedUserId, "user_edited", message, link, editedUserId); // Changed userId to editedUserId
        logger.info("Generated 'user_edited' notification for admin {} about edited user {}.", adminId, editedUserId);
    }

    public void notifyAdminUserActivated(String adminId, String activatedUserId, String activatedUserEmail) throws InterruptedException, ExecutionException {
        String message = String.format("Admin '%s' activated user with ID '%s' (Email: %s).", adminId, activatedUserId, activatedUserEmail);
        String link = "/admin-dashboard/users/edit/" + activatedUserId; // Link to the activated user's page
        createNotification(activatedUserId, "user_activated", message, link, activatedUserId); // Changed userId to activatedUserId
        logger.info("Generated 'user_activated' notification for admin {} about activated user {}.", adminId, activatedUserId);
    }

    public void notifyAdminUserDeactivated(String adminId, String deactivatedUserId, String deactivatedUserEmail) throws InterruptedException, ExecutionException {
        String message = String.format("Admin '%s' deactivated user with ID '%s' (Email: %s).", adminId, deactivatedUserId, deactivatedUserEmail);
        String link = "/admin-dashboard/users/edit/" + deactivatedUserId; // Link to the deactivated user's page
        createNotification(deactivatedUserId, "user_deactivated", message, link, deactivatedUserId); // Changed userId to deactivatedUserId
        logger.info("Generated 'user_deactivated' notification for admin {} about deactivated user {}.", adminId, deactivatedUserId);
    }

    public void notifyAdminUserAdded(String adminId, String addedUserId, String addedUserEmail) throws InterruptedException, ExecutionException {
        String message = String.format("Admin '%s' added a new user with ID '%s' (Email: %s).", adminId, addedUserId, addedUserEmail);
        String link = "/admin-dashboard/users/edit/" + addedUserId; // Link to the newly added user's edit page
        createNotification(addedUserId, "user_added", message, link, addedUserId); // Changed userId to addedUserId
        logger.info("Generated 'user_added' notification for admin {} about added user {}.", adminId, addedUserId);
    }

    public List<RestNotification> getAdminNotifications() throws InterruptedException, ExecutionException, TimeoutException {
        logger.info("Retrieving admin notifications");
        CollectionReference notificationsCollection = firestore.collection(NOTIFICATIONS_COLLECTION);
        Query query = notificationsCollection
                .whereIn("type", List.of("user_added", "user_deleted", "user_edited", "user_activated", "user_deactivated"))
                .orderBy("timestamp", Query.Direction.DESCENDING);

        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        List<QueryDocumentSnapshot> documents;
        try {
            documents = querySnapshot.get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS).getDocuments();
        } catch (TimeoutException e) {
            logger.error("Timeout occurred while getting admin notifications: {}", e.getMessage(), e);
            throw e;
        }
        List<RestNotification> notifications = new ArrayList<>();
        for (QueryDocumentSnapshot doc : documents) {
            notifications.add(doc.toObject(RestNotification.class));
        }
        logger.info("Retrieved {} admin notifications", notifications.size());
        return notifications;
    }

    public List<RestNotification> getNotificationsForUser(String userId) throws InterruptedException, ExecutionException, TimeoutException {
        logger.info("Retrieving notifications specifically for userId: {}", userId);
        CollectionReference notificationsCollection = firestore.collection(NOTIFICATIONS_COLLECTION);
        Query query = notificationsCollection.whereEqualTo("userId", userId).orderBy("timestamp", Query.Direction.DESCENDING);
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

}