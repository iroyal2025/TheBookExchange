package edu.famu.thebookexchange.service;

import org.springframework.stereotype.Service;

@Service
public class AuthService {

    public String authenticate(String email, String password) {
        // Authentication is now handled by PlainTextAuthFilter
        // Return a success message
        return "Login successful";
    }
}