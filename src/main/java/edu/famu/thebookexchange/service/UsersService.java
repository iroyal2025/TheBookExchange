package edu.famu.thebookexchange.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import edu.famu.thebookexchange.model.Rest.RestUsers;
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
import java.util.stream.Collectors;

@Service
public class UsersService {

    private static final Logger logger = LoggerFactory.getLogger(UsersService.class);
    private static final String USERS_COLLECTION = "Users";
    private static final int FIRESTORE_TIMEOUT = 5; // seconds

    private final Firestore firestore;
    private final NotificationService notificationService;

    @Autowired
    public UsersService(Firestore firestore, NotificationService notificationService) {
        this.firestore = firestore;
        this.notificationService = notificationService;
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
                        document.getDouble("balance") != null ? document.getDouble("balance") : 0.0,
                        document.getDouble("sellerRating"), // Fetch sellerRating
                        document.getLong("sellerRatingCount") // Fetch sellerRatingCount
                );
                users.add(user);
            }
        }
        return users;
    }

    public String addUser(RestUsers user, String adminId) throws InterruptedException, ExecutionException {
        logger.info("Adding user with details: {}", user);

        Map<String, Object> userData = new HashMap<>();
        userData.put("email", user.getEmail());
        userData.put("password", user.getPassword()); // Store password (consider hashing)
        userData.put("major", user.getMajor());
        userData.put("profilePicture", user.getProfilePicture());
        userData.put("role", user.getRole());
        userData.put("isActive", true);
        userData.put("balance", 0.0);
        userData.put("sellerRating", 0.0);
        userData.put("sellerRatingCount", 0L);

        ApiFuture<DocumentReference> writeResult = firestore.collection(USERS_COLLECTION).add(userData);
        DocumentReference newUserRef = writeResult.get();
        String newUserId = newUserRef.getId();
        logger.info("User added with ID: {}", newUserId);

        if (adminId != null) {
            notificationService.createNotification(adminId, "user_added",
                    String.format("Admin added a new user with ID '%s' (Email: %s).", newUserId, user.getEmail()),
                    "/admin-dashboard/users/edit/" + newUserId, newUserId);
            logger.info("Generated 'user_added' notification for admin {} about added user {}.", adminId, newUserId);
        } else {
            logger.warn("Admin ID not provided for 'user_added' notification.");
        }

        return newUserId;
    }

    public boolean deleteUser(String userId, String adminId) throws ExecutionException, InterruptedException, TimeoutException {
        logger.info("Deleting user with userID: {}", userId);
        try {
            DocumentSnapshot userSnapshot = firestore.collection(USERS_COLLECTION).document(userId).get().get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS);
            if (userSnapshot.exists()) {
                String deletedUserEmail = userSnapshot.getString("email");
                ApiFuture<WriteResult> writeResult = firestore.collection(USERS_COLLECTION).document(userId).delete();
                writeResult.get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS);
                logger.info("User deleted successfully with ID: {}", userId);

                if (adminId != null && deletedUserEmail != null) {
                    notificationService.createNotification(adminId, "user_deleted",
                            String.format("Admin deleted user with ID '%s' (Email: %s).", userId, deletedUserEmail),
                            "/admin-dashboard/users", userId);
                    logger.info("Generated 'user_deleted' notification for admin {} about deleted user {}.", adminId, userId);
                } else {
                    logger.warn("Admin ID not provided for 'user_deleted' notification.");
                }
                return true;
            } else {
                logger.warn("User with ID {} not found for deletion.", userId);
                return false;
            }
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.error("Error deleting user with userID: {}", userId, e);
            throw e;
        }
    }

    public boolean deleteUserByEmail(String email, String adminId) throws ExecutionException, InterruptedException, TimeoutException {
        logger.info("Deleting user with email: {}", email);
        Query query = firestore.collection(USERS_COLLECTION).whereEqualTo("email", email);
        ApiFuture<QuerySnapshot> querySnapshotFuture = query.get();
        QuerySnapshot querySnapshot = querySnapshotFuture.get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS);

        if (!querySnapshot.isEmpty()) {
            QueryDocumentSnapshot document = querySnapshot.getDocuments().get(0);
            String userId = document.getId();
            String deletedUserEmail = document.getString("email");
            ApiFuture<WriteResult> deleteResult = firestore.collection(USERS_COLLECTION).document(userId).delete();
            deleteResult.get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS);
            logger.info("User with email {} deleted successfully with ID: {}", email, userId);

            if (adminId != null && deletedUserEmail != null) {
                notificationService.createNotification(adminId, "user_deleted",
                        String.format("Admin deleted user with ID '%s' (Email: %s).", userId, deletedUserEmail),
                        "/admin-dashboard/users", userId);
                logger.info("Generated 'user_deleted' notification for admin {} about deleted user with email {}.", adminId, email);
            } else {
                logger.warn("Admin ID not provided for 'user_deleted' notification (email: {}).", email);
            }
            return true;
        } else {
            logger.warn("User with email {} not found for deletion.", email);
            return false;
        }
    }

    public String updateUser(String userId, RestUsers updatedUser, String adminId) throws InterruptedException, ExecutionException, TimeoutException {
        logger.info("Updating user with ID: {}, data: {}", userId, updatedUser);
        DocumentReference userRef = firestore.collection(USERS_COLLECTION).document(userId);
        Map<String, Object> updates = new HashMap<>();
        if (updatedUser.getEmail() != null) {
            updates.put("email", updatedUser.getEmail());
        }
        if (updatedUser.getMajor() != null) {
            updates.put("major", updatedUser.getMajor());
        }
        if (updatedUser.getProfilePicture() != null) {
            updates.put("profilePicture", updatedUser.getProfilePicture());
        }
        if (updatedUser.getRole() != null) {
            updates.put("role", updatedUser.getRole());
        }
        // Do not allow password updates through this method for security reasons
        ApiFuture<WriteResult> updateResult = userRef.update(updates);
        updateResult.get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS);
        String updateTime = updateResult.get().getUpdateTime().toString();
        logger.info("User with ID {} updated at: {}", userId, updateTime);

        if (adminId != null) {
            notificationService.createNotification(adminId, "user_edited",
                    String.format("Admin updated user with ID '%s' (Email: %s).", userId, updatedUser.getEmail()),
                    "/admin-dashboard/users/edit/" + userId, userId);
            logger.info("Generated 'user_edited' notification for admin {} about updated user {}.", adminId, userId);
        } else {
            logger.warn("Admin ID not provided for 'user_edited' notification.");
        }

        return updateTime;
    }

    public String updateUserEmail(String userId, String newEmail, String adminId) throws InterruptedException, ExecutionException, TimeoutException {
        logger.info("Updating email for user ID: {} to: {}", userId, newEmail);
        DocumentReference userRef = firestore.collection(USERS_COLLECTION).document(userId);
        ApiFuture<WriteResult> updateResult = userRef.update("email", newEmail);
        updateResult.get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS);
        String updateTime = updateResult.get().getUpdateTime().toString();
        logger.info("Email for user with ID {} updated to {} at: {}", userId, newEmail, updateTime);

        if (adminId != null) {
            notificationService.createNotification(adminId, "user_edited",
                    String.format("Admin updated email for user with ID '%s' to '%s'.", userId, newEmail),
                    "/admin-dashboard/users/edit/" + userId, userId);
            logger.info("Generated 'user_edited' notification for admin {} about email update for user {}.", adminId, userId);
        } else {
            logger.warn("Admin ID not provided for 'user_edited' notification (email update).");
        }

        return updateTime;
    }

    public String updateUserPassword(String userId, String newPassword, String adminId) throws InterruptedException, ExecutionException, TimeoutException {
        logger.warn("Updating password for user ID: {} - Consider hashing before storing!", userId);
        DocumentReference userRef = firestore.collection(USERS_COLLECTION).document(userId);
        ApiFuture<WriteResult> updateResult = userRef.update("password", newPassword);
        updateResult.get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS);
        String updateTime = updateResult.get().getUpdateTime().toString();
        logger.info("Password for user with ID {} updated at: {}", userId, updateTime);

        if (adminId != null) {
            notificationService.createNotification(adminId, "user_password_reset",
                    String.format("Admin reset password for user with ID '%s'.", userId),
                    "/admin-dashboard/users/edit/" + userId, userId);
            logger.info("Generated 'user_password_reset' notification for admin {} about password reset for user {}.", adminId, userId);
        } else {
            logger.warn("Admin ID not provided for 'user_password_reset' notification.");
        }
        return updateTime;
    }

    public boolean setUserActivation(String userId, boolean isActive, String adminId) throws InterruptedException, ExecutionException, TimeoutException {
        logger.info("Setting activation status for user ID: {} to: {}", userId, isActive);
        DocumentReference userRef = firestore.collection(USERS_COLLECTION).document(userId);
        ApiFuture<WriteResult> updateResult = userRef.update("isActive", isActive);
        updateResult.get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS);
        logger.info("Activation status for user with ID {} set to: {}", userId, isActive);

        String notificationType = isActive ? "user_activated" : "user_deactivated";
        String message = String.format("Admin %s user with ID '%s'.", isActive ? "activated" : "deactivated", userId);

        DocumentSnapshot userSnapshot = userRef.get().get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS);
        String userEmail = userSnapshot.getString("email");

        if (adminId != null && userEmail != null) {
            notificationService.createNotification(adminId, notificationType, message,
                    "/admin-dashboard/users/edit/" + userId, userId);
            logger.info("Generated '{}' notification for admin {} about user {} (Email: {}).", notificationType, adminId, userId, userEmail);
        } else {
            logger.warn("Admin ID not provided for '{}' notification (user ID: {}).", notificationType, userId);
        }
        return true;
    }

    public double getUserBalanceByEmail(String email) throws InterruptedException, ExecutionException, TimeoutException {
        Query query = firestore.collection(USERS_COLLECTION).whereEqualTo("email", email).limit(1);
        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        List<QueryDocumentSnapshot> documents = querySnapshot.get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS).getDocuments();
        if (!documents.isEmpty()) {
            return documents.get(0).getDouble("balance");
        } else {
            logger.warn("User with email {} not found.", email);
            return 0.0; // Or throw an exception
        }
    }

    public void updateUserBalanceByEmail(String email, double balance) throws InterruptedException, ExecutionException, TimeoutException {
        Query query = firestore.collection(USERS_COLLECTION).whereEqualTo("email", email).limit(1);
        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        List<QueryDocumentSnapshot> documents = querySnapshot.get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS).getDocuments();
        if (!documents.isEmpty()) {
            DocumentReference userRef = documents.get(0).getReference();
            ApiFuture<WriteResult> updateResult = userRef.update("balance", balance);
            updateResult.get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS);
            logger.info("Balance for user with email {} updated to: {}", email, balance);
        } else {
            logger.warn("User with email {} not found for balance update.", email);
        }
    }

    public void addStudentToParent(String parentEmail, String studentEmail, String adminId) throws InterruptedException, ExecutionException, TimeoutException {
        Query parentQuery = firestore.collection(USERS_COLLECTION).whereEqualTo("email", parentEmail).limit(1);
        ApiFuture<QuerySnapshot> parentSnapshotFuture = parentQuery.get();
        QuerySnapshot parentSnapshot = parentSnapshotFuture.get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS);

        Query studentQuery = firestore.collection(USERS_COLLECTION).whereEqualTo("email", studentEmail).limit(1);
        ApiFuture<QuerySnapshot> studentSnapshotFuture = studentQuery.get();
        QuerySnapshot studentSnapshot = studentSnapshotFuture.get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS);

        if (!parentSnapshot.isEmpty() && !studentSnapshot.isEmpty()) {
            DocumentReference parentRef = parentSnapshot.getDocuments().get(0).getReference();
            DocumentReference studentRef = studentSnapshot.getDocuments().get(0).getReference();

            ApiFuture<WriteResult> updateParentFuture = parentRef.update("students", FieldValue.arrayUnion(studentRef.getId()));
            updateParentFuture.get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS);
            logger.info("Student with email {} added to parent with email {}.", studentEmail, parentEmail);

            if (adminId != null) {
                notificationService.createNotification(adminId, "student_added_to_parent",
                        String.format("Admin added student '%s' to parent '%s'.", studentEmail, parentEmail),
                        "/admin-dashboard/users/edit/" + parentRef.getId(), parentRef.getId());
                logger.info("Generated 'student_added_to_parent' notification for admin {} about adding student {} to parent {}.", adminId, studentEmail, parentEmail);
            } else {
                logger.warn("Admin ID not provided for 'student_added_to_parent' notification.");
            }

        } else {
            logger.warn("Parent with email {} or student with email {} not found.", parentEmail, studentEmail);
        }
    }

    public void removeStudentFromParent(String parentEmail, String studentEmail, String adminId) throws InterruptedException, ExecutionException, TimeoutException {
        Query parentQuery = firestore.collection(USERS_COLLECTION).whereEqualTo("email", parentEmail).limit(1);
        ApiFuture<QuerySnapshot> parentSnapshotFuture = parentQuery.get();
        QuerySnapshot parentSnapshot = parentSnapshotFuture.get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS);

        Query studentQuery = firestore.collection(USERS_COLLECTION).whereEqualTo("email", studentEmail).limit(1);
        ApiFuture<QuerySnapshot> studentSnapshotFuture = studentQuery.get();
        QuerySnapshot studentSnapshot = studentSnapshotFuture.get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS);

        if (!parentSnapshot.isEmpty() && !studentSnapshot.isEmpty()) {
            DocumentReference parentRef = parentSnapshot.getDocuments().get(0).getReference();
            DocumentReference studentRef = studentSnapshot.getDocuments().get(0).getReference();

            ApiFuture<WriteResult> updateParentFuture = parentRef.update("students", FieldValue.arrayRemove(studentRef.getId()));
            updateParentFuture.get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS);
            logger.info("Student with email {} removed from parent with email {}.", studentEmail, parentEmail);

            if (adminId != null) {
                notificationService.createNotification(adminId, "student_removed_from_parent",
                        String.format("Admin removed student '%s' from parent '%s'.", studentEmail, parentEmail),
                        "/admin-dashboard/users/edit/" + parentRef.getId(), parentRef.getId());
                logger.info("Generated 'student_removed_from_parent' notification for admin {} about removing student {} from parent {}.", adminId, studentEmail, parentEmail);
            } else {
                logger.warn("Admin ID not provided for 'student_removed_from_parent' notification.");
            }

        } else {
            logger.warn("Parent with email {} or student with email {} not found.", parentEmail, studentEmail);
        }
    }

    public List<RestUsers> getStudentsByParentEmail(String email) throws InterruptedException, ExecutionException, TimeoutException {
        Query parentQuery = firestore.collection(USERS_COLLECTION).whereEqualTo("email", email).limit(1);
        ApiFuture<QuerySnapshot> parentSnapshotFuture = parentQuery.get();
        QuerySnapshot parentSnapshot = parentSnapshotFuture.get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS);

        if (!parentSnapshot.isEmpty()) {
            DocumentSnapshot parentDoc = parentSnapshot.getDocuments().get(0);
            List<String> studentIds = (List<String>) parentDoc.get("students");
            if (studentIds != null && !studentIds.isEmpty()) {
                return studentIds.stream()
                        .map(studentId -> {
                            try {
                                ApiFuture<DocumentSnapshot> studentSnapshotFuture = firestore.collection(USERS_COLLECTION).document(studentId).get();
                                DocumentSnapshot studentSnapshot = studentSnapshotFuture.get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS);
                                if (studentSnapshot.exists()) {
                                    RestUsers student = studentSnapshot.toObject(RestUsers.class);
                                    student.setUserId(studentSnapshot.getId());
                                    return student;
                                }
                                return null;
                            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                                logger.error("Error retrieving student data: {}", e.getMessage(), e);
                                return null;
                            }
                        })
                        .filter(student -> student != null)
                        .collect(Collectors.toList());
            } else {
                logger.info("No students found for parent with email: {}", email);
                return List.of();
            }
        } else {
            logger.warn("Parent with email {} not found.", email);
            return List.of();
        }
    }

    public String getUserIdByEmail(String email) throws InterruptedException, ExecutionException, TimeoutException {
        Query query = firestore.collection(USERS_COLLECTION).whereEqualTo("email", email).limit(1);
        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        List<QueryDocumentSnapshot> documents = querySnapshot.get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS).getDocuments();
        if (!documents.isEmpty()) {
            return documents.get(0).getId();
        } else {
            logger.warn("User with email {} not found.", email);
            return null;
        }
    }

    public void rateSeller(String sellerId, String raterEmail, int rating) throws InterruptedException, ExecutionException, TimeoutException {
        DocumentReference sellerRef = firestore.collection(USERS_COLLECTION).document(sellerId);
        ApiFuture<DocumentSnapshot> sellerSnapshotFuture = sellerRef.get();
        DocumentSnapshot sellerSnapshot = sellerSnapshotFuture.get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS);

        if (sellerSnapshot.exists()) {
            Double currentRating = sellerSnapshot.getDouble("sellerRating");
            Long ratingCount = sellerSnapshot.getLong("sellerRatingCount");

            if (currentRating == null) currentRating = 0.0;
            if (ratingCount == null) ratingCount = 0L;

            double newRatingSum = currentRating * ratingCount + rating;
            long newRatingCount = ratingCount + 1;
            double newAverageRating = newRatingSum / newRatingCount;

            ApiFuture<WriteResult> updateResult = sellerRef.update("sellerRating", newAverageRating, "sellerRatingCount", newRatingCount);
            updateResult.get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS);
            logger.info("Seller with ID {} rated by {} with rating {}. New average rating: {}", sellerId, raterEmail, rating, newAverageRating);
        } else {
            logger.warn("Seller with ID {} not found for rating.", sellerId);
        }
    }

    public RestUsers getUserByEmail(String email) throws InterruptedException, ExecutionException, TimeoutException {
        logger.info("Fetching user with email: {}", email);
        CollectionReference users = firestore.collection(USERS_COLLECTION);
        Query query = users.whereEqualTo("email", email);
        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        QuerySnapshot snapshot = querySnapshot.get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS);
        if (!snapshot.isEmpty()) {
            DocumentSnapshot document = snapshot.getDocuments().get(0); // Assuming email is unique
            RestUsers user = document.toObject(RestUsers.class); // You'll need a fromDocument method or proper mapping
            logger.debug("User found: {}", user);
            return user;
        } else {
            logger.warn("No user found with email: {}", email);
            return null;
        }
    }
}