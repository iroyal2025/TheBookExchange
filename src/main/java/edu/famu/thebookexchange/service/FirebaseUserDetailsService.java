package edu.famu.thebookexchange.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.firebase.FirebaseApp;
import com.google.firebase.cloud.FirestoreClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
public class FirebaseUserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(FirebaseUserDetailsService.class);

    public String getUserRole(String uid) {
        logger.info("Retrieving role for UID: {}", uid);

        FirebaseApp firebaseApp = FirebaseApp.getInstance();
        if (firebaseApp == null) {
            logger.error("Firebase App is null");
            return null;
        }

        Firestore dbFirestore = FirestoreClient.getFirestore(firebaseApp);
        try {
            logger.debug("Firestore query: Users where uid == {}", uid);

            ApiFuture<QuerySnapshot> future = dbFirestore.collection("Users").whereEqualTo("uid", uid).get();
            QuerySnapshot querySnapshot = future.get(10, TimeUnit.SECONDS);

            logger.debug("Query returned {} documents.", querySnapshot.size());

            if (!querySnapshot.isEmpty()) {
                DocumentSnapshot document = querySnapshot.getDocuments().get(0);
                logger.debug("DocumentSnapshot: {}", document.getData());

                String role = document.getString("role");
                logger.info("Role found: {}", role);
                return role;
            } else {
                logger.warn("User not found with UID: {}", uid);
                return null;
            }
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.error("Error retrieving user role from Firestore", e);
            return null;
        }
    }

    public String getUserRoleByEmail(String email) {
        logger.info("Retrieving role for email: {}", email);

        FirebaseApp firebaseApp = FirebaseApp.getInstance();
        if (firebaseApp == null) {
            logger.error("Firebase App is null");
            return null;
        }

        Firestore dbFirestore = FirestoreClient.getFirestore(firebaseApp);
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

    public void afterTokenVerificationSuccess(String uid) {
        logger.info("Token verified successfully for UID: {}", uid);
        // Add any post-verification logic here (e.g., logging)
    }

    public DocumentSnapshot getUserDocumentSnapshot(String uid) {
        logger.info("Retrieving DocumentSnapshot for UID: {}", uid);

        FirebaseApp firebaseApp = FirebaseApp.getInstance();
        if (firebaseApp == null) {
            logger.error("Firebase App is null");
            return null;
        }

        Firestore dbFirestore = FirestoreClient.getFirestore(firebaseApp);
        try {
            DocumentSnapshot document = dbFirestore.collection("Users").document(uid).get().get(10, TimeUnit.SECONDS);
            if (document.exists()) {
                return document;
            } else {
                logger.warn("Document not found with UID: {}", uid);
                return null;
            }

        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.error("Error retrieving DocumentSnapshot from Firestore", e);
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