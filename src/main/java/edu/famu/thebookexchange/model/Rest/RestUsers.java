package edu.famu.thebookexchange.model.Rest;

import edu.famu.thebookexchange.model.Abstracts.AUsers;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class RestUsers extends AUsers {

    private String userId;
    private boolean isActive = true; // Added isActive field, default to true

    public RestUsers(String email, String password, String major, String profilePicture, String role, String userId, boolean isActive) {
        super(email, password, major, profilePicture, role);
        this.userId = userId;
        this.isActive = isActive;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }
}