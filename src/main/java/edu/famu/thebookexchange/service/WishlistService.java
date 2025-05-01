package edu.famu.thebookexchange.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import edu.famu.thebookexchange.model.Rest.RestWishlist;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

@Service
public class WishlistService {

    private static final Logger logger = LoggerFactory.getLogger(WishlistService.class);
    private Firestore firestore;

    private static final String WISHLIST_COLLECTION = "Wishlist";
    private static final String BOOKS_COLLECTION = "Books"; // Assuming your books are in a "Books" collection
    private static final String NOTIFICATIONS_COLLECTION = "Notifications";
    private static final long FIRESTORE_TIMEOUT = 5; // Timeout in seconds

    public WishlistService() {
        this.firestore = FirestoreClient.getFirestore();
    }

    public List<RestWishlist> getAllWishlists() throws InterruptedException, ExecutionException, TimeoutException {
        CollectionReference wishlistCollection = firestore.collection(WISHLIST_COLLECTION);
        ApiFuture<QuerySnapshot> querySnapshot = wishlistCollection.get();
        List<QueryDocumentSnapshot> documents = querySnapshot.get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS).getDocuments();

        List<RestWishlist> wishlists = new ArrayList<>();

        for (QueryDocumentSnapshot document : documents) {
            if (document.exists()) {
                List<String> bookIds = (List<String>) document.get("bookRequests");
                String userId = document.getString("userId");
                RestWishlist wishlist = new RestWishlist(bookIds, userId);
                wishlists.add(wishlist);
            }
        }
        return wishlists;
    }

    public String addWishlist(RestWishlist wishlist) throws InterruptedException, ExecutionException {
        logger.info("Adding wishlist with details: {}", wishlist);

        Map<String, Object> wishlistData = new HashMap<>();
        wishlistData.put("bookRequests", wishlist.getBookRequests() != null ? wishlist.getBookRequests() : new ArrayList<>());
        wishlistData.put("userId", wishlist.getUserId());

        ApiFuture<DocumentReference> writeResult = firestore.collection(WISHLIST_COLLECTION).add(wishlistData);
        DocumentReference rs = writeResult.get();
        logger.info("Wishlist added with ID: {}", rs.getId());
        return rs.getId();
    }


    public boolean addBookToWishlist(String userId, String bookId) throws InterruptedException, ExecutionException, TimeoutException {
        Query query = firestore.collection(WISHLIST_COLLECTION)
                .whereEqualTo("userId", userId);
        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        List<QueryDocumentSnapshot> documents = querySnapshot.get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS).getDocuments();

        if (!documents.isEmpty()) {
            DocumentReference wishlistDocRef = documents.get(0).getReference();
            return firestore.runTransaction(transaction -> {
                DocumentSnapshot wishlistSnapshot = transaction.get(wishlistDocRef).get();
                if (wishlistSnapshot.exists()) {
                    List<String> bookRequests = (List<String>) wishlistSnapshot.get("bookRequests");
                    if (bookRequests == null) {
                        bookRequests = new ArrayList<>();
                    }
                    if (!bookRequests.contains(bookId)) {
                        bookRequests.add(bookId);
                        transaction.update(wishlistDocRef, "bookRequests", bookRequests);
                        generateWishlistNotification(userId, bookId, "added");
                        logger.info("Book with ID {} added to wishlist for user {}", bookId, userId);
                        return true;
                    } else {
                        logger.warn("Book with ID {} already in wishlist for user {}", bookId, userId);
                        return false; // Book already in wishlist
                    }
                } else {
                    logger.warn("Wishlist not found for user {} to add book {}", userId, bookId);
                    return false; // Wishlist doesn't exist
                }
            }).get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS);
        } else {
            logger.warn("No wishlist found for user {} to add book {}", userId, bookId);
            return false; // No wishlist for the user
        }
    }

    public boolean removeBookFromWishlist(String userId, String bookId) throws InterruptedException, ExecutionException, TimeoutException {
        Query query = firestore.collection(WISHLIST_COLLECTION)
                .whereEqualTo("userId", userId);
        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        List<QueryDocumentSnapshot> documents = querySnapshot.get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS).getDocuments();

        if (!documents.isEmpty()) {
            DocumentReference wishlistDocRef = documents.get(0).getReference();
            return firestore.runTransaction(transaction -> {
                DocumentSnapshot wishlistSnapshot = transaction.get(wishlistDocRef).get();
                if (wishlistSnapshot.exists()) {
                    List<String> bookRequests = (List<String>) wishlistSnapshot.get("bookRequests");
                    if (bookRequests != null && bookRequests.contains(bookId)) {
                        bookRequests.remove(bookId);
                        transaction.update(wishlistDocRef, "bookRequests", bookRequests);
                        generateWishlistNotification(userId, bookId, "removed");
                        logger.info("Book with ID {} removed from wishlist for user {}", bookId, userId);
                        return true;
                    } else {
                        logger.warn("Book with ID {} not found in wishlist for user {}", bookId, userId);
                        return false; // Book not in the wishlist
                    }
                } else {
                    logger.warn("Wishlist not found for user {} to remove book {}", userId, bookId);
                    return false; // Wishlist doesn't exist
                }
            }).get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS);
        } else {
            logger.warn("No wishlist found for user {} to remove book {}", userId, bookId);
            return false; // No wishlist for the user
        }
    }

    public RestWishlist getWishlistByUserId(String userId) throws InterruptedException, ExecutionException, TimeoutException {
        Query query = firestore.collection(WISHLIST_COLLECTION)
                .whereEqualTo("userId", userId);
        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        List<QueryDocumentSnapshot> documents = querySnapshot.get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS).getDocuments();

        if (!documents.isEmpty()) {
            QueryDocumentSnapshot document = documents.get(0); // Assuming one wishlist per user
            List<String> bookIds = (List<String>) document.get("bookRequests");
            return new RestWishlist(bookIds, document.getString("userId"));
        } else {
            return null; // Wishlist not found for the user
        }
    }

    public boolean deleteWishlistByUserId(String userId) throws ExecutionException, InterruptedException, TimeoutException {
        Query query = firestore.collection(WISHLIST_COLLECTION)
                .whereEqualTo("userId", userId);
        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        List<QueryDocumentSnapshot> documents = querySnapshot.get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS).getDocuments();

        if (!documents.isEmpty()) {
            documents.get(0).getReference().delete().get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS);
            logger.info("Wishlist deleted successfully for user ID: {}", userId);
            return true;
        } else {
            logger.warn("Wishlist not found for deletion for user ID: {}", userId);
            return false;
        }
    }

    public boolean deleteWishlistById(String wishlistId) throws ExecutionException, InterruptedException, TimeoutException {
        try {
            DocumentReference wishlistRef = firestore.collection(WISHLIST_COLLECTION).document(wishlistId);
            ApiFuture<DocumentSnapshot> future = wishlistRef.get();
            DocumentSnapshot document = future.get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS);

            if (document.exists()) {
                wishlistRef.delete().get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS);
                logger.info("Wishlist deleted successfully with ID: {}", wishlistId);
                return true;
            } else {
                logger.warn("Wishlist not found for deletion with ID: {}", wishlistId);
                return false;
            }
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.error("Error deleting wishlist with ID: {}", wishlistId, e);
            throw e;
        }
    }

    public String updateWishlist(String wishlistId, RestWishlist updatedWishlist) throws InterruptedException, ExecutionException, TimeoutException {
        DocumentReference wishlistRef = firestore.collection(WISHLIST_COLLECTION).document(wishlistId);

        Map<String, Object> updatedWishlistData = new HashMap<>();
        updatedWishlistData.put("bookRequests", updatedWishlist.getBookRequests() != null ? updatedWishlist.getBookRequests() : new ArrayList<>());
        updatedWishlistData.put("userId", updatedWishlist.getUserId());

        ApiFuture<WriteResult> writeResult = wishlistRef.update(updatedWishlistData);
        logger.info("Wishlist updated at: {}", writeResult.get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS).getUpdateTime().toString());

        return writeResult.get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS).getUpdateTime().toString();
    }

    private void generateWishlistNotification(String userId, String bookId, String action) {
        try {
            Map<String, Object> notificationData = new HashMap<>();
            notificationData.put("userId", userId); // Store userId as String
            notificationData.put("type", "wishlist");
            notificationData.put("message", String.format("Book %s to your wishlist.", action));
            notificationData.put("timestamp", FieldValue.serverTimestamp());
            notificationData.put("relatedBook", firestore.collection(BOOKS_COLLECTION).document(bookId));
            notificationData.put("isRead", false);

            firestore.collection(NOTIFICATIONS_COLLECTION).add(notificationData).get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS);
            logger.info("Wishlist notification generated for user {} (book ID: {}, action: {})", userId, bookId, action);

        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.error("Error generating wishlist notification for user {} (book ID: {}, action: {})", userId, bookId, action, e);
        }
    }

    public RestWishlist getWishlistWithBooksByUserId(String userId) throws InterruptedException, ExecutionException, TimeoutException {
        RestWishlist wishlist = getWishlistByUserId(userId);
        if (wishlist != null && wishlist.getBookRequests() != null) {
            List<String> bookIds = wishlist.getBookRequests();
            List<Map<String, Object>> bookDetails = new ArrayList<>();
            for (String bookId : bookIds) {
                ApiFuture<DocumentSnapshot> future = firestore.collection(BOOKS_COLLECTION).document(bookId).get();
                DocumentSnapshot bookSnapshot = future.get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS); // Get the DocumentSnapshot from the future
                if (bookSnapshot.exists()) {
                    Map<String, Object> data = bookSnapshot.getData();
                    if (data != null) {
                        data.put("bookId", bookSnapshot.getId());
                        bookDetails.add(data);
                    }
                }
            }
            return new RestWishlist(bookDetails, userId, true);
        }
        return wishlist;
    }
}