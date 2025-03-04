package edu.famu.thebookexchange.model.Default;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Users {

    private int userId;
    private String email;
    private String password;
    private String major;
    private String profilePicture;

    public Users(int userId, String email, String password, String major, String profilePicture) {
        this.userId = userId;
        this.email = email;
        this.password = password;
        this.major = major;
        this.profilePicture = profilePicture;
    }

    // Manual Getters (If Lombok is not working)
    public int getUserId() {
        return userId;
    }

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

    // Manual Setters (If needed)
    public void setUserId(int userId) {
        this.userId = userId;
    }

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