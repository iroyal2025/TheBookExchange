// Abstract Class (AProfiles.java)
package edu.famu.thebookexchange.model.Abstracts;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public abstract class AProfiles {

    @JsonProperty("preferences")
    private String preferences;

    @JsonProperty("wishlistId")
    private Integer wishlistId;

    public AProfiles(String preferences, Integer wishlistId) {
        this.preferences = preferences;
        this.wishlistId = wishlistId;
    }

    // Manual Getters
    public String getPreferences() {
        return preferences;
    }

    public Integer getWishlistId() {
        return wishlistId;
    }

    // Manual Setters
    public void setPreferences(String preferences) {
        this.preferences = preferences;
    }

    public void setWishlistId(Integer wishlistId) {
        this.wishlistId = wishlistId;
    }
}