package edu.famu.thebookexchange.model.Abstracts;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Data
@NoArgsConstructor
public abstract class AUsers {

    @JsonProperty("email")
    private String email;

    @JsonProperty("password")
    private String password;

    @JsonProperty("major")
    private String major;

    @JsonProperty("profilePicture")
    private String profilePicture;

    public AUsers(String email, String password, String major, String profilePicture) {
        this.email = email;
        this.password = password;
        this.major = major;
        this.profilePicture = profilePicture;
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

    // Manual Setters (if needed, though @Setter already provides them)
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
}