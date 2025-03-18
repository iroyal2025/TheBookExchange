package edu.famu.thebookexchange.controller;

import edu.famu.thebookexchange.model.Rest.RestUsers;
import edu.famu.thebookexchange.service.UsersService;
import edu.famu.thebookexchange.util.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.lang.InterruptedException;

@RestController
@RequestMapping("/Users")
public class UsersController {

    private static final Logger logger = LoggerFactory.getLogger(UsersController.class);

    @Autowired
    private UsersService userService;

    @GetMapping("/")
    public ResponseEntity<ApiResponse<List<RestUsers>>> getAllUsers() {
        try {
            List<RestUsers> restUsers = userService.getAllUsers();

            if (!restUsers.isEmpty()) {
                return ResponseEntity.ok(new ApiResponse<>(true, "Users List", restUsers, null));
            } else {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body(new ApiResponse<>(true, "No users found", null, null));
            }
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.error("Error retrieving users: {}", e.getMessage(), e); // Log the error
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false, "Error retrieving users", null, e.getMessage()));
        }
    }

    @PostMapping("/add")
    public ResponseEntity<ApiResponse<String>> addUser(@RequestBody RestUsers user) {
        logger.info("Received POST request to /Users/add with data: {}", user);
        try {
            String userId = userService.addUser(user);
            return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(true, "User created", userId, null));
        } catch (InterruptedException | ExecutionException e) {
            logger.error("Error creating user: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false, "Error creating user", null, e.getMessage()));
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<ApiResponse<String>> deleteUserByEmail(@RequestParam String email) {
        logger.info("Received DELETE request to /Users/delete with email: {}", email);
        try {
            boolean deleted = userService.deleteUserByEmail(email);

            if (deleted) {
                return ResponseEntity.ok(new ApiResponse<>(true, "User deleted successfully", null, null));
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false, "Failed to delete user", null, null));
            }
        } catch (ExecutionException | InterruptedException | TimeoutException e) {
            logger.error("Error deleting user: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false, "Error deleting user", null, e.getMessage()));
        }
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<ApiResponse<String>> deleteUser(@PathVariable String userId) {
        logger.info("Received DELETE request to /Users/{} with userId: {}", userId, userId);
        try {
            boolean deleted = userService.deleteUser(userId);

            if (deleted) {
                return ResponseEntity.ok(new ApiResponse<>(true, "User deleted successfully", null, null));
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false, "Failed to delete user", null, null));
            }
        } catch (ExecutionException | InterruptedException | TimeoutException e) {
            logger.error("Error deleting user: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false, "Error deleting user", null, e.getMessage()));
        }
    }

    @PutMapping("/{userId}")
    public ResponseEntity<ApiResponse<String>> updateUser(@PathVariable String userId, @RequestBody RestUsers updatedUser) {
        logger.info("Received PUT request to /Users/{} with data: {}", userId, updatedUser); // Log the request
        try {
            String updateTime = userService.updateUser(userId, updatedUser);
            return ResponseEntity.ok(new ApiResponse<>(true, "User updated", updateTime, null));
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.error("Error updating user: {}", e.getMessage(), e); // Log the error
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false, "Error updating user", null, e.getMessage()));
        }
    }

    @PutMapping("/{userId}/activate")
    public ResponseEntity<ApiResponse<Boolean>> activateUser(@PathVariable String userId) {
        logger.info("Received PUT request to /Users/{}/activate", userId);
        try {
            boolean activated = userService.setUserActivation(userId, true);
            return ResponseEntity.ok(new ApiResponse<>(true, "User activated", activated, null));
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.error("Error activating user: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false, "Error activating user", null, e.getMessage()));
        }
    }

    @PutMapping("/{userId}/deactivate")
    public ResponseEntity<ApiResponse<Boolean>> deactivateUser(@PathVariable String userId) {
        logger.info("Received PUT request to /Users/{}/deactivate", userId);
        try {
            boolean deactivated = userService.setUserActivation(userId, false);
            return ResponseEntity.ok(new ApiResponse<>(true, "User deactivated", deactivated, null));
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.error("Error deactivating user: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false, "Error deactivating user", null, e.getMessage()));
        }
    }
}