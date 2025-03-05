package edu.famu.thebookexchange.controller;

import edu.famu.thebookexchange.model.Rest.RestProfiles;
import edu.famu.thebookexchange.service.ProfilesService;
import edu.famu.thebookexchange.util.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.lang.InterruptedException;

@RestController
@RequestMapping("/Profiles")
public class ProfilesController {

    @Autowired
    private ProfilesService profileService;

    @GetMapping("/")
    public ResponseEntity<ApiResponse<List<RestProfiles>>> getAllProfiles() {
        try {
            List<RestProfiles> restProfiles = profileService.getAllProfiles();

            if (!restProfiles.isEmpty()) {
                return ResponseEntity.ok(new ApiResponse<>(true, "Profiles List", restProfiles, null));
            } else {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body(new ApiResponse<>(true, "No profiles found", null, null));
            }
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false, "Error retrieving profiles", null, e.getMessage()));
        }
    }

    @PostMapping("/add")
    public ResponseEntity<ApiResponse<String>> addProfile(@RequestBody RestProfiles profile) {
        try {
            String profileId = profileService.addProfile(profile);
            return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(true, "Profile created", profileId, null));
        } catch (InterruptedException | ExecutionException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false, "Error creating profile", null, e.getMessage()));
        }
    }

    @DeleteMapping("/{profileId}")
    public ResponseEntity<ApiResponse<String>> deleteProfileById(@PathVariable String profileId) {
        try {
            boolean deleted = profileService.deleteProfileById(profileId);

            if (deleted) {
                return ResponseEntity.ok(new ApiResponse<>(true, "Profile deleted successfully", null, null));
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false, "Failed to delete profile", null, null));
            }
        } catch (ExecutionException | InterruptedException | TimeoutException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false, "Error deleting profile", null, e.getMessage()));
        }
    }

    @PutMapping("/{profileId}")
    public ResponseEntity<ApiResponse<String>> updateProfile(@PathVariable String profileId, @RequestBody RestProfiles updatedProfile) {
        try {
            String updateTime = profileService.updateProfile(profileId, updatedProfile);
            return ResponseEntity.ok(new ApiResponse<>(true, "Profile updated", updateTime, null));
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false, "Error updating profile", null, e.getMessage()));
        }
    }
}