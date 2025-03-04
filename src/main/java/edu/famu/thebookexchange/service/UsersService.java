package edu.famu.thebookexchange.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import edu.famu.thebookexchange.model.Rest.RestUsers;
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
public class UsersService {

    private static final Logger logger = LoggerFactory.getLogger(UsersService.class);
    private Firestore firestore;

    private static final String USERS_COLLECTION = "Users";
    private static final long FIRESTORE_TIMEOUT = 5; // Timeout in seconds

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
                RestUsers user = new RestUsers(
                        document.getString("email"),
                        document.getString("password"),
                        document.getString("major"),
                        document.getString("profilePicture"),
                        document.getReference() // Correctly get the DocumentReference
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
        userData.put("password", user.getPassword());
        userData.put("major", user.getMajor());
        userData.put("profilePicture", user.getProfilePicture());

        ApiFuture<DocumentReference> writeResult = firestore.collection(USERS_COLLECTION).add(userData);
        DocumentReference rs = writeResult.get();
        logger.info("User added with ID: {}", rs.getId());
        return rs.getId();
    }

    public boolean deleteUserByEmail(String email) throws ExecutionException, InterruptedException, TimeoutException {
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

    public String updateUser(String userId, RestUsers updatedUser) throws InterruptedException, ExecutionException, TimeoutException {
        DocumentReference userRef = firestore.collection(USERS_COLLECTION).document(userId);

        Map<String, Object> updatedUserData = new HashMap<>();
        updatedUserData.put("email", updatedUser.getEmail());
        updatedUserData.put("password", updatedUser.getPassword());
        updatedUserData.put("major", updatedUser.getMajor());
        updatedUserData.put("profilePicture", updatedUser.getProfilePicture());

        ApiFuture<WriteResult> writeResult = userRef.update(updatedUserData);
        logger.info("User updated at: {}", writeResult.get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS).getUpdateTime().toString());

        return writeResult.get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS).getUpdateTime().toString();
    }
}