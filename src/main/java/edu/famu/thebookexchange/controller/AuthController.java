package edu.famu.thebookexchange.controller;

import edu.famu.thebookexchange.service.FirebaseUserDetailsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private FirebaseUserDetailsService firebaseUserDetailsService;

    @PostMapping("/verify")
    public ResponseEntity<?> verify(@RequestParam(value = "email", required = true) String email) { // Changed to email
        Map<String, String> response = new HashMap<>();

        logger.info("Received Email (Insecure): {}", email); // Log the received email

        String role = firebaseUserDetailsService.getUserRoleByEmail(email); // Updated method call

        if (role != null) {
            response.put("role", role);
        } else {
            logger.warn("Role not found for Email: {}", email);
            response.put("role", "unknown");
        }

        return ResponseEntity.ok(response);
    }
}