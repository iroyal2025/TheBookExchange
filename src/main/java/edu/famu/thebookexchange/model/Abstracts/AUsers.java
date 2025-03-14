package edu.famu.thebookexchange.model.Abstracts;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public abstract class AUsers {

    @JsonProperty("email")
    protected String email;

    @JsonProperty("password")
    protected String password;

    @JsonProperty("major")
    protected String major;

    @JsonProperty("profilePicture")
    protected String profilePicture;

    @JsonProperty("role")
    protected String role;

    public AUsers(String email, String password, String major, String profilePicture, String role) {
        this.email = email;
        this.password = password;
        this.major = major;
        this.profilePicture = profilePicture;
        this.role = role;
    }

    // Manual Getters
    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getMajor() {
        return major;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public String getRole() {
        return role;
    }

    // Manual Setters
    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setMajor(String major) {
        this.major = major;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    public void setRole(String role) {
        this.role = role;
    }
}