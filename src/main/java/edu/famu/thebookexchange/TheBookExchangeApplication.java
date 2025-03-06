package edu.famu.thebookexchange;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.io.InputStream;

@SpringBootApplication
public class TheBookExchangeApplication {

    public static void main(String[] args) throws IOException {
        // Firebase Initialization
        InputStream serviceAccount = TheBookExchangeApplication.class.getClassLoader().getResourceAsStream("serviceAccountKey.json");

        if (serviceAccount == null) {
            System.err.println("serviceAccountKey.json not found in resources.");
            return; // Exit if the file is not found
        }

        FirebaseOptions options = new FirebaseOptions.Builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build();

        if (FirebaseApp.getApps().isEmpty()) {
            FirebaseApp.initializeApp(options);
        }

        // Spring Boot Application Run
        SpringApplication.run(TheBookExchangeApplication.class, args);
    }
}