package edu.famu.thebookexchange;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Objects;

@SpringBootApplication
public class TheBookExchangeApplication {

    public static void main(String[] args) throws IOException {
        // Firebase Initialization
        ClassLoader loader = TheBookExchangeApplication.class.getClassLoader();
        File file = new File(Objects.requireNonNull(loader.getResource("TheBookExchangeFrontend/server for app/serviceAccountKey.json")).getFile());
        FileInputStream serviceAccount = new FileInputStream(file.getAbsolutePath());

        FirebaseOptions options = new FirebaseOptions.Builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build();
        if (FirebaseApp.getApps().isEmpty())
            FirebaseApp.initializeApp(options);

        // Spring Boot Application Run
        SpringApplication.run(TheBookExchangeApplication.class, args);
    }
}
