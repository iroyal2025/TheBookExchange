package edu.famu.thebookexchange.model.Default;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.cloud.firestore.DocumentReference;
import edu.famu.thebookexchange.model.Abstracts.AProfiles;
import edu.famu.thebookexchange.model.Rest.DocumentReferenceDeserializer;
import edu.famu.thebookexchange.model.Rest.DocumentReferenceSerializer;

public class Profiles extends AProfiles {
    @JsonSerialize(using = DocumentReferenceSerializer.class)
    @JsonDeserialize(using = DocumentReferenceDeserializer.class)
    private DocumentReference userId;

    public Profiles(String preferences, Integer wishlistId, DocumentReference userId) {
        super(preferences, wishlistId);
        this.userId = userId;
    }

    // Manual Getters
    public DocumentReference getUserId() {
        return userId;
    }

    // Manual Setters
    public void setUserId(DocumentReference userId) {
        this.userId = userId;
    }

    // Manual Getters from AProfiles
    public String getPreferences() {
        return super.getPreferences();
    }

    public Integer getWishlistId() {
        return super.getWishlistId();
    }

    // Manual Setters from AProfiles
    public void setPreferences(String preferences) {
        super.setPreferences(preferences);
    }

    public void setWishlistId(Integer wishlistId) {
        super.setWishlistId(wishlistId);
    }
}
