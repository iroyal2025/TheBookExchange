package edu.famu.thebookexchange.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.FirebaseApp;
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
public class FirebaseUserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(FirebaseUserDetailsService.class);

    public String getUserRoleByEmail(String email) {
        logger.info("Retrieving role for email: {}", email);

        Firestore dbFirestore = getFirestore();
        if (dbFirestore == null) {
            return null;
        }

        try {
            logger.debug("Firestore query: Users where email == {}", email);

            ApiFuture<QuerySnapshot> future = dbFirestore.collection("Users").whereEqualTo("email", email).get();
            QuerySnapshot querySnapshot = future.get(10, TimeUnit.SECONDS);

            logger.debug("Query returned {} documents.", querySnapshot.size());

            if (!querySnapshot.isEmpty()) {
                DocumentSnapshot document = querySnapshot.getDocuments().get(0);
                logger.debug("DocumentSnapshot: {}", document.getData());

                String role = document.getString("role");
                logger.info("Role found for email: {}", role);
                return role;
            } else {
                logger.warn("User not found with email: {}", email);
                return null;
            }
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.error("Error retrieving user role from Firestore", e);
            return null;
        }
    }

    public Map<String, String> getUserRoleAndIdByEmail(String email) {
        logger.info("Retrieving role and id for email: {}", email);

        Firestore dbFirestore = getFirestore();
        if (dbFirestore == null) {
            return null;
        }

        try {
            logger.debug("Firestore query: Users where email == {}", email);

            ApiFuture<QuerySnapshot> future = dbFirestore.collection("Users").whereEqualTo("email", email).get();
            QuerySnapshot querySnapshot = future.get(10, TimeUnit.SECONDS);

            logger.debug("Query returned {} documents.", querySnapshot.size());

            if (!querySnapshot.isEmpty()) {
                DocumentSnapshot document = querySnapshot.getDocuments().get(0);
                logger.debug("DocumentSnapshot: {}", document.getData());

                String role = document.getString("role");
                String userId = document.getId(); // Firestore document ID as user ID

                logger.info("Role and User ID found for email: {}", email);

                Map<String, String> result = new HashMap<>();
                result.put("role", role);
                result.put("userId", userId);  // Consistent with frontend
                return result;

            } else {
                logger.warn("User not found with email: {}", email);
                return null;
            }
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.error("Error retrieving user role and id from Firestore", e);
            return null;
        }
    }

    public List<RestUsers> getStudentsByParentEmail(String parentEmail) {
        logger.info("Retrieving students for parent email: {}", parentEmail);

        Firestore dbFirestore = getFirestore();
        if (dbFirestore == null) {
            return null;
        }

        try {
            CollectionReference studentsCollection = dbFirestore.collection("Users").document(parentEmail).collection("students");
            ApiFuture<QuerySnapshot> query = studentsCollection.get();
            QuerySnapshot querySnapshot = query.get(10, TimeUnit.SECONDS);

            List<RestUsers> students = new ArrayList<>();
            for (QueryDocumentSnapshot document : querySnapshot.getDocuments()) {
                Map<String, Object> data = document.getData();

                RestUsers student = new RestUsers();
                student.setEmail((String) data.get("email"));
                student.setRole((String) data.get("role"));
                student.setUserId(document.getId());
                student.setMajor((String) data.get("major"));
                student.setProfilePicture((String) data.get("profilePicture"));
                student.setActive((Boolean) data.get("isActive"));
                student.setBalance(((Number) data.get("balance")).doubleValue());
                // Add more RestUsers properties as needed from Firebase data.

                students.add(student);
            }

            logger.info("Retrieved {} students for parent email: {}", students.size(), parentEmail);
            return students;

        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.error("Error retrieving students from Firestore", e);
            return null;
        }
    }

    public Firestore getFirestore() {
        FirebaseApp firebaseApp = FirebaseApp.getInstance();
        if (firebaseApp == null) {
            logger.error("Firebase App is null");
            return null;
        }
        return FirestoreClient.getFirestore(firebaseApp);
    }
}