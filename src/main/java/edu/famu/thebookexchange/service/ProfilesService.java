package edu.famu.thebookexchange.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import edu.famu.thebookexchange.model.Rest.RestProfiles;
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
public class ProfilesService {

    private static final Logger logger = LoggerFactory.getLogger(ProfilesService.class);
    private Firestore firestore;

    private static final String PROFILES_COLLECTION = "Profiles";
    private static final long FIRESTORE_TIMEOUT = 5; // Timeout in seconds

    public ProfilesService() {
        this.firestore = FirestoreClient.getFirestore();
    }

    public List<RestProfiles> getAllProfiles() throws InterruptedException, ExecutionException, TimeoutException {
        CollectionReference profilesCollection = firestore.collection(PROFILES_COLLECTION);
        ApiFuture<QuerySnapshot> querySnapshot = profilesCollection.get();
        List<QueryDocumentSnapshot> documents = querySnapshot.get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS).getDocuments();

        List<RestProfiles> profiles = new ArrayList<>();

        for (QueryDocumentSnapshot document : documents) {
            if (document.exists()) {
                Object wishlistIdObject = document.get("wishlistId");
                Long wishlistIdLong = null;

                if (wishlistIdObject instanceof Long) {
                    wishlistIdLong = (Long) wishlistIdObject;
                } else {
                    logger.warn("wishlistId is not a Long for document: " + document.getId());
                    logger.warn("wishlistId object: " + wishlistIdObject);
                }

                RestProfiles profile = new RestProfiles(
                        document.getString("preferences"),
                        wishlistIdLong != null ? wishlistIdLong.intValue() : null,
                        document.get("userId", DocumentReference.class)
                );
                profiles.add(profile);
            }
        }
        return profiles;
    }

    public String addProfile(RestProfiles profile) throws InterruptedException, ExecutionException {
        logger.info("Adding profile with details: {}", profile);

        Map<String, Object> profileData = new HashMap<>();
        profileData.put("preferences", profile.getPreferences());
        profileData.put("wishlistId", profile.getWishlistId());
        profileData.put("userId", profile.getUserId());

        ApiFuture<DocumentReference> writeResult = firestore.collection(PROFILES_COLLECTION).add(profileData);
        DocumentReference rs = writeResult.get();
        logger.info("Profile added with ID: {}", rs.getId());
        return rs.getId();
    }

    public boolean deleteProfileById(String profileId) throws ExecutionException, InterruptedException, TimeoutException {
        try {
            DocumentReference profileRef = firestore.collection(PROFILES_COLLECTION).document(profileId);
            ApiFuture<DocumentSnapshot> future = profileRef.get();
            DocumentSnapshot document = future.get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS);

            if (document.exists()) {
                profileRef.delete().get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS);
                logger.info("Profile deleted successfully with ID: {}", profileId);
                return true;
            } else {
                logger.warn("Profile not found for deletion with ID: {}", profileId);
                return false;
            }
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.error("Error deleting profile with ID: {}", profileId, e);
            throw e;
        }
    }

    public String updateProfile(String profileId, RestProfiles updatedProfile) throws InterruptedException, ExecutionException, TimeoutException {
        DocumentReference profileRef = firestore.collection(PROFILES_COLLECTION).document(profileId);

        Map<String, Object> updatedProfileData = new HashMap<>();
        updatedProfileData.put("preferences", updatedProfile.getPreferences());
        updatedProfileData.put("wishlistId", updatedProfile.getWishlistId());
        updatedProfileData.put("userId", updatedProfile.getUserId());

        ApiFuture<WriteResult> writeResult = profileRef.update(updatedProfileData);
        logger.info("Profile updated at: {}", writeResult.get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS).getUpdateTime().toString());

        return writeResult.get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS).getUpdateTime().toString();
    }
}