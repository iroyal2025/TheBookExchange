package edu.famu.thebookexchange; // Adjust the package if needed


public class JwtResponse {
    private String token;
    private String role;

    public JwtResponse(String token, String role) {
        this.token = token;
        this.role = role;
    }

    public String getToken() {
        return token;
    }

    public String getRole() {
        return role;
    }
}