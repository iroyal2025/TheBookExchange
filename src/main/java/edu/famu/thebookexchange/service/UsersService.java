package edu.famu.thebookexchange.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import edu.famu.thebookexchange.model.Rest.RestUsers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
public class UsersService {

    private static final Logger logger = LoggerFactory.getLogger(UsersService.class);
    private Firestore firestore;
    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private static final String USERS_COLLECTION = "Users";
    private static final long FIRESTORE_TIMEOUT = 5;

    public UsersService() {
        this.firestore = FirestoreClient.getFirestore();
    }

    public List<RestUsers> getAllUsers() throws InterruptedException, ExecutionException, TimeoutException {
        CollectionReference usersCollection = firestore.collection(USERS_COLLECTION);
        ApiFuture<QuerySnapshot> querySnapshot = usersCollection.get();
        List<QueryDocumentSnapshot> documents = querySnapshot.get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS).getDocuments();

        List<RestUsers> users = new ArrayList<>();

        for (QueryDocumentSnapshot document : documents) {
            if (document.exists()) {
                String userId = document.getId();
                RestUsers user = new RestUsers(
                        document.getString("email"),
                        document.getString("password"),
                        document.getString("major"),
                        document.getString("profilePicture"),
                        document.getString("role"),
                        userId,
                        document.getBoolean("isActive") != null ? document.getBoolean("isActive") : true
                );
                users.add(user);
            }
        }

        return users;
    }

    public String addUser(RestUsers user) throws InterruptedException, ExecutionException {
        logger.info("Adding user with details: {}", user);

        Map<String, Object> userData = new HashMap<>();
        userData.put("email", user.getEmail());
        userData.put("password", passwordEncoder.encode(user.getPassword()));
        userData.put("major", user.getMajor());
        userData.put("profilePicture", user.getProfilePicture());
        userData.put("role", user.getRole());
        userData.put("isActive", true);

        ApiFuture<DocumentReference> writeResult = firestore.collection(USERS_COLLECTION).add(userData);
        DocumentReference rs = writeResult.get();
        logger.info("User added with ID: {}", rs.getId());
        return rs.getId();
    }

    public boolean deleteUserByEmail(String email) throws ExecutionException, InterruptedException, TimeoutException {
        logger.info("Deleting user with email: {}", email);
        try {
            Query query = firestore.collection(USERS_COLLECTION).whereEqualTo("email", email);
            ApiFuture<QuerySnapshot> querySnapshot = query.get();
            List<QueryDocumentSnapshot> documents = querySnapshot.get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS).getDocuments();

            if (!documents.isEmpty()) {
                for (QueryDocumentSnapshot document : documents) {
                    firestore.collection(USERS_COLLECTION).document(document.getId()).delete().get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS);
                    logger.info("User deleted successfully with ID: {} and email: {}", document.getId(), email);
                }
                return true;
            } else {
                logger.warn("User not found for deletion with email: {}", email);
                return false;
            }
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.error("Error deleting user with email: {}", email, e);
            throw e;
        }
    }

    public boolean deleteUser(String userId) throws ExecutionException, InterruptedException, TimeoutException {
        logger.info("Deleting user with userID: {}", userId);
        try {
            ApiFuture<WriteResult> writeResult = firestore.collection(USERS_COLLECTION).document(userId).delete();
            writeResult.get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS);
            logger.info("User deleted successfully with ID: {}", userId);
            return true;
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.error("Error deleting user with userID: {}", userId, e);
            throw e;
        }
    }

    public String updateUser(String userId, RestUsers updatedUser) throws InterruptedException, ExecutionException, TimeoutException {
        logger.info("Updating user with userId: {}", userId);
        try {
            DocumentReference userRef = firestore.collection(USERS_COLLECTION).document(userId);

            Map<String, Object> updatedUserData = new HashMap<>();
            updatedUserData.put("email", updatedUser.getEmail());
            updatedUserData.put("major", updatedUser.getMajor());
            updatedUserData.put("profilePicture", updatedUser.getProfilePicture());
            updatedUserData.put("role", updatedUser.getRole());

            if (updatedUser.getPassword() != null && !updatedUser.getPassword().isEmpty()) {
                updatedUserData.put("password", passwordEncoder.encode(updatedUser.getPassword()));
            }
            updatedUserData.put("isActive", updatedUser.isActive());

            ApiFuture<WriteResult> writeResult = userRef.update(updatedUserData);
            logger.info("User updated at: {}", writeResult.get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS).getUpdateTime().toString());

            return writeResult.get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS).getUpdateTime().toString();
        } catch (Exception e) {
            logger.error("Error updating user: {}", e.getMessage(), e);
            throw e;
        }
    }

    public boolean toggleUserActivation(String userId) throws ExecutionException, InterruptedException, TimeoutException {
        logger.info("Toggling activation for user with userId: {}", userId);
        try {
            DocumentReference userRef = firestore.collection(USERS_COLLECTION).document(userId);
            DocumentSnapshot document = userRef.get().get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS);

            if (document.exists()) {
                boolean currentActiveStatus = document.getBoolean("isActive") != null ? document.getBoolean("isActive") : true;
                userRef.update("isActive", !currentActiveStatus).get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS);
                logger.info("User activation toggled successfully for ID: {}", userId);
                return !currentActiveStatus;
            } else {
                logger.warn("User not found for activation toggle with ID: {}", userId);
                return false;
            }
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.error("Error toggling user activation: {}", userId, e);
            throw e;
        }
    }

    public DocumentSnapshot getUserDocumentSnapshot(String userID) throws ExecutionException, InterruptedException, TimeoutException {
        DocumentReference userRef = firestore.collection(USERS_COLLECTION).document(userID);
        return userRef.get().get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS);
    }

    public boolean setUserActivation(String userId, boolean active) throws InterruptedException, ExecutionException, TimeoutException {
        logger.info("Setting activation for user with userId: {} to: {}", userId, active);
        try {
            DocumentReference userRef = firestore.collection(USERS_COLLECTION).document(userId);
            DocumentSnapshot document = userRef.get().get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS);

            if (document.exists()) {
                userRef.update("isActive", active).get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS);
                logger.info("User activation set to {} for ID: {}", active, userId);
                return active;
            } else {
                logger.warn("User not found for activation change with ID: {}", userId);
                return false;
            }
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.error("Error setting user activation: {}", userId, e);
            throw e;
        }
    }
}