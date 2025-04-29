// AuthController.java
package edu.famu.thebookexchange.controller;

import edu.famu.thebookexchange.service.FirebaseUserDetailsService;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.api.core.ApiFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private FirebaseUserDetailsService firebaseUserDetailsService;

    private static final String USERS_COLLECTION = "Users";
    private static final long FIRESTORE_TIMEOUT = 5;

    @PostMapping("/verify")
    public ResponseEntity<?> verify(@RequestParam(value = "email", required = true) String email,
                                    @RequestParam(value = "password", required = true) String password) {
        logger.info("Received Email (Plain Text): {}", email);

        try {
            Query query = firebaseUserDetailsService.getFirestore().collection(USERS_COLLECTION).whereEqualTo("email", email);
            ApiFuture<QuerySnapshot> querySnapshot = query.get();
            List<QueryDocumentSnapshot> documents = querySnapshot.get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS).getDocuments();

            if (!documents.isEmpty()) {
                QueryDocumentSnapshot document = documents.get(0);
                String storedPassword = document.getString("password");
                Boolean isActive = document.getBoolean("isActive");

                if (isActive == null || !isActive) {
                    logger.warn("User account is disabled for email: {}", email);
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User account is disabled.");
                }

                // Directly compare the plain text passwords
                if (storedPassword != null && password.equals(storedPassword)) {
                    String role = document.getString("role");
                    if (role != null) {
                        Map<String, String> response = new HashMap<>();
                        response.put("email", email);
                        response.put("role", role);
                        response.put("userId", document.getId()); // Add the userId here
                        return ResponseEntity.ok(response);
                    } else {
                        logger.warn("Role not found for Email: {}", email);
                        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Role not found.");
                    }
                } else {
                    logger.warn("Invalid password for email: {}", email);
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials.");
                }

            } else {
                logger.warn("User not found in Firestore for email: {}", email);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
            }

        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.error("Error verifying credentials for email: {}", email, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error.");
        }
    }
}