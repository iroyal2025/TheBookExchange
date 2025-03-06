package edu.famu.thebookexchange.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @PostMapping("/login")
    public ResponseEntity<String> login() {
        // Authentication is handled by PlainTextAuthFilter
        // Return a success message
        return ResponseEntity.ok("Login successful");
    }
}