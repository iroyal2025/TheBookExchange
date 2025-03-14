package edu.famu.thebookexchange;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

@SpringBootApplication
public class TheBookExchangeApplication {

    private static final Logger logger = LoggerFactory.getLogger(TheBookExchangeApplication.class);

    public static void main(String[] args) {
        // Firebase Initialization
        try {
            InputStream serviceAccount = TheBookExchangeApplication.class.getClassLoader()
                    .getResourceAsStream("serviceAccountKey.json");

            if (serviceAccount == null) {
                logger.error("serviceAccountKey.json not found in resources.");
                return; // Exit if the file is not found
            }

            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                logger.info("Firebase App initialized successfully.");
            }
        } catch (IOException e) {
            logger.error("Error initializing Firebase:", e);
            return; // Exit if Firebase initialization fails
        }

        // Spring Boot Application Run
        SpringApplication.run(TheBookExchangeApplication.class, args);
    }
}