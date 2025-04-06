// UsersService.java
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
import java.util.stream.Collectors;

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
                        document.getBoolean("isActive") != null ? document.getBoolean("isActive") : true,
                        document.getDouble("balance") != null ? document.getDouble("balance") : 0.0
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
        userData.put("balance", 0.0); // Initialize balance to 0

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
            updatedUserData.put("balance", updatedUser.getBalance());

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

    public double getUserBalance(String userId) throws InterruptedException, ExecutionException, TimeoutException {
        DocumentSnapshot document = getUserDocumentSnapshot(userId);
        if (document.exists()) {
            Double balance = document.getDouble("balance");
            return balance != null ? balance : 0.0;
        } else {
            return 0.0;
        }
    }

    public double getUserBalanceByEmail(String email) throws InterruptedException, ExecutionException, TimeoutException {
        logger.info("Getting user balance for email: {}", email);
        try {
            QuerySnapshot querySnapshot = firestore.collection("Users").whereEqualTo("email", email).get().get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS);

            if (!querySnapshot.isEmpty()) {
                DocumentSnapshot document = querySnapshot.getDocuments().get(0);
                Double balance = document.getDouble("balance");
                return balance != null ? balance : 0.0; // Return 0.0 if balance is null
            } else {
                logger.warn("User not found with email: {}", email);
                return 0.0; // Return 0.0 if user not found
            }
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.error("Error getting user balance for email: {}", email, e);
            throw e;
        }
    }


    public void updateUserBalanceByEmail(String email, double newBalance) throws InterruptedException, ExecutionException, TimeoutException {
        logger.info("Updating user balance for email: {} to: {}", email, newBalance);
        try {
            QuerySnapshot querySnapshot = firestore.collection("Users").whereEqualTo("email", email).get().get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS);

            if (!querySnapshot.isEmpty()) {
                DocumentSnapshot document = querySnapshot.getDocuments().get(0);
                firestore.collection("Users").document(document.getId()).update("balance", newBalance).get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS);
                logger.info("User balance updated for email: {}", email);
            } else {
                logger.warn("User not found with email: {}", email);
            }
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.error("Error updating user balance for email: {}", email, e);
            throw e;
        }
    }

    public void removeStudentFromParent(String parentEmail, String studentEmail) throws InterruptedException, ExecutionException, TimeoutException {
        logger.info("Removing student {} from parent {}", studentEmail, parentEmail);
        try {
            // Find the parent document using parentEmail
            QuerySnapshot parentQuery = firestore.collection(USERS_COLLECTION).whereEqualTo("email", parentEmail).get().get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS);

            if (parentQuery.isEmpty()) {
                logger.warn("Parent with email {} not found.", parentEmail);
                throw new IllegalArgumentException("Parent not found.");
            }

            DocumentSnapshot parentDoc = parentQuery.getDocuments().get(0);
            DocumentReference parentDocRef = parentDoc.getReference();

            // Find the student document within the parent's "students" subcollection
            QuerySnapshot studentQuery = parentDocRef.collection("students").whereEqualTo("email", studentEmail).get().get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS);

            if (!studentQuery.isEmpty()) {
                DocumentSnapshot studentDoc = studentQuery.getDocuments().get(0);
                parentDocRef.collection("students").document(studentDoc.getId()).delete().get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS);
                logger.info("Student {} removed from parent {} successfully.", studentEmail, parentEmail);
            } else {
                logger.warn("Student {} not found in parent {}'s subcollection.", studentEmail, parentEmail);
                throw new IllegalArgumentException("Student not found.");
            }
        } catch (Exception e) {
            logger.error("Error removing student from parent: {}", e.getMessage(), e);
            throw e;
        }
    }

    public void addStudentToParent(String parentEmail, String studentEmail) throws InterruptedException, ExecutionException, TimeoutException {
        logger.info("Adding student {} to parent {}", studentEmail, parentEmail);

        try {
            // Find the parent document using parentEmail
            QuerySnapshot parentQuery = firestore.collection(USERS_COLLECTION).whereEqualTo("email", parentEmail).get().get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS);

            if (parentQuery.isEmpty()) {
                logger.warn("Parent with email {} not found.", parentEmail);
                throw new IllegalArgumentException("Parent not found.");
            }

            DocumentSnapshot parentDoc = parentQuery.getDocuments().get(0);
            DocumentReference parentDocRef = parentDoc.getReference();

            // Find the student document using studentEmail
            QuerySnapshot studentQuery = firestore.collection(USERS_COLLECTION).whereEqualTo("email", studentEmail).get().get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS);

            if (studentQuery.isEmpty()) {
                logger.warn("Student with email {} not found.", studentEmail);
                throw new IllegalArgumentException("Student not found.");
            }

            DocumentSnapshot studentDoc = studentQuery.getDocuments().get(0);

            // Create a map with student data
            Map<String, Object> studentData = new HashMap<>();
            studentData.put("email", studentDoc.getString("email"));
            studentData.put("role", studentDoc.getString("role"));
            studentData.put("major", studentDoc.getString("major"));
            studentData.put("profilePicture", studentDoc.getString("profilePicture"));
            studentData.put("isActive", studentDoc.getBoolean("isActive"));
            studentData.put("balance", studentDoc.getDouble("balance"));

            // Add the student data to the parent's "students" subcollection
            parentDocRef.collection("students").document(studentDoc.getId()).set(studentData).get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS);

            logger.info("Student {} added to parent {} successfully.", studentEmail, parentEmail);

        } catch (Exception e) {
            logger.error("Error adding student to parent: {}", e.getMessage(), e);
            throw e;
        }
    }

    public List<RestUsers> getStudentsByParentEmail(String parentEmail) throws InterruptedException, ExecutionException, TimeoutException {
        logger.info("Retrieving students for parent email: {}", parentEmail);

        try {
            // Query for the parent document based on email
            Query query = firestore.collection(USERS_COLLECTION).whereEqualTo("email", parentEmail);
            ApiFuture<QuerySnapshot> querySnapshot = query.get();

            List<QueryDocumentSnapshot> documents = querySnapshot.get().getDocuments();

            if (documents.isEmpty()) {
                logger.warn("Parent document not found for email: {}", parentEmail);
                return new ArrayList<>();
            }

            // Get the parent document
            DocumentSnapshot parentDoc = documents.get(0);
            DocumentReference parentDocRef = parentDoc.getReference();

            // Retrieve students from the "students" subcollection
            ApiFuture<QuerySnapshot> studentsQuery = parentDocRef.collection("students").get();
            List<QueryDocumentSnapshot> studentDocs = studentsQuery.get().getDocuments();

            List<RestUsers> students = new ArrayList<>();
            for (QueryDocumentSnapshot studentDoc : studentDocs) {
                Map<String, Object> data = studentDoc.getData();

                RestUsers student = new RestUsers();
                student.setEmail((String) data.get("email"));
                student.setRole((String) data.get("role"));
                student.setUserId(studentDoc.getId());
                student.setMajor((String) data.get("major"));
                student.setProfilePicture((String) data.get("profilePicture"));
                student.setActive((Boolean) data.get("isActive"));
                student.setBalance(((Number) data.get("balance")).doubleValue());

                students.add(student);
                logger.info("Student added to list: {}", student);
            }

            logger.info("Retrieved {} students for parent email: {}", students.size(), parentEmail);
            return students;

        } catch (InterruptedException | ExecutionException e) {
            logger.error("Error retrieving students from Firestore", e);
            return new ArrayList<>();
        }
    }

}