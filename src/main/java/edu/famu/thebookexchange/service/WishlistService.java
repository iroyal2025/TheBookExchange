// Service Class (WishlistService.java)
package edu.famu.thebookexchange.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import edu.famu.thebookexchange.model.Rest.RestWishlist;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
public class WishlistService {

    private static final Logger logger = LoggerFactory.getLogger(WishlistService.class);
    private Firestore firestore;

    private static final String WISHLIST_COLLECTION = "Wishlist";
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
                RestWishlist wishlist = new RestWishlist(
                        document.getString("book requests"),
                        document.get("userId", DocumentReference.class)
                );
                wishlists.add(wishlist);
            }
        }
        return wishlists;
    }

    public String addWishlist(RestWishlist wishlist) throws InterruptedException, ExecutionException {
        logger.info("Adding wishlist with details: {}", wishlist);

        Map<String, Object> wishlistData = new HashMap<>();
        wishlistData.put("book requests", wishlist.getBookRequests());
        wishlistData.put("userId", wishlist.getUserId());

        ApiFuture<DocumentReference> writeResult = firestore.collection(WISHLIST_COLLECTION).add(wishlistData);
        DocumentReference rs = writeResult.get();
        logger.info("Wishlist added with ID: {}", rs.getId());
        return rs.getId();
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
        updatedWishlistData.put("book requests", updatedWishlist.getBookRequests());
        updatedWishlistData.put("userId", updatedWishlist.getUserId());

        ApiFuture<WriteResult> writeResult = wishlistRef.update(updatedWishlistData);
        logger.info("Wishlist updated at: {}", writeResult.get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS).getUpdateTime().toString());

        return writeResult.get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS).getUpdateTime().toString();
    }
}