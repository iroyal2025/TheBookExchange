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
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

@RestController
@RequestMapping("/Users")
public class UsersController {

    private static final Logger logger = LoggerFactory.getLogger(UsersController.class);

    private final UsersService userService;

    @Autowired
    public UsersController(UsersService userService) {
        this.userService = userService;
    }

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
            logger.error("Error retrieving users: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false, "Error retrieving users", null, e.getMessage()));
        }
    }

    @PostMapping("/add")
    public ResponseEntity<ApiResponse<String>> addUser(@RequestBody RestUsers user, @RequestParam(required = false) String adminIdFromFrontend) {
        logger.info("Received POST request to /Users/add with data: {}, adminId from frontend: {}", user, adminIdFromFrontend);
        try {
            String userId = userService.addUser(user, adminIdFromFrontend);
            return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(true, "User created", userId, null));
        } catch (InterruptedException | ExecutionException e) {
            logger.error("Error creating user: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false, "Error creating user", null, e.getMessage()));
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<ApiResponse<String>> deleteUserByEmail(@RequestParam String email, @RequestParam(required = false) String adminIdFromFrontend) {
        logger.info("Received DELETE request to /Users/delete with email: {}, adminId from frontend: {}", email, adminIdFromFrontend);
        try {
            boolean deleted = userService.deleteUserByEmail(email, adminIdFromFrontend);
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
    public ResponseEntity<ApiResponse<String>> deleteUser(
            @PathVariable String userId,
            @RequestParam(value = "adminIdFromFrontend", required = false) String adminIdFromFrontend) {
        logger.info("Received DELETE request to /Users/{} with userId: {}, adminId from frontend: {}", userId, adminIdFromFrontend);
        try {
            boolean deleted = userService.deleteUser(userId, adminIdFromFrontend);
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
    public ResponseEntity<ApiResponse<String>> updateUser(@PathVariable String userId, @RequestBody RestUsers updatedUser, @RequestParam(required = false) String adminIdFromFrontend) {
        logger.info("Received PUT request to /Users/{} with data: {}, adminId from frontend: {}", userId, updatedUser, adminIdFromFrontend);
        try {
            String updateTime = userService.updateUser(userId, updatedUser, adminIdFromFrontend);
            return ResponseEntity.ok(new ApiResponse<>(true, "User updated", updateTime, null));
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.error("Error updating user: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false, "Error updating user", null, e.getMessage()));
        }
    }

    @PutMapping("/{userId}/email")
    public ResponseEntity<ApiResponse<String>> updateUserEmail(@PathVariable String userId, @RequestBody Map<String, String> requestBody, @RequestParam(required = false) String adminIdFromFrontend) {
        logger.info("Received PUT request to /Users/{}/email with data: {}, adminId from frontend: {}", userId, requestBody, adminIdFromFrontend);
        String newEmail = requestBody.get("email");
        if (newEmail == null || newEmail.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse<>(false, "Invalid request", null, "Email cannot be empty"));
        }
        try {
            String updateTime = userService.updateUserEmail(userId, newEmail, adminIdFromFrontend);
            return ResponseEntity.ok(new ApiResponse<>(true, "Email updated successfully", updateTime, null));
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.error("Error updating user email: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false, "Error updating user email", null, e.getMessage()));
        }
    }

    @PutMapping("/{userId}/password")
    public ResponseEntity<ApiResponse<String>> updateUserPassword(
            @PathVariable String userId,
            @RequestBody Map<String, String> requestBody,
            @RequestParam(value = "adminIdFromFrontend", required = false) String adminIdFromFrontend) {
        String newPassword = requestBody.get("newPassword");
        if (newPassword == null || newPassword.isEmpty()) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, "New password cannot be empty.", null, null));
        }
        try {
            String updateTime = userService.updateUserPassword(userId, newPassword, adminIdFromFrontend);
            return ResponseEntity.ok(new ApiResponse<>(true, "Password updated successfully.", updateTime, null));
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.error("Error updating password for user {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(new ApiResponse<>(false, "Error updating password.", null, e.getMessage()));
        }
    }

    @PutMapping("/{userId}/activate")
    public ResponseEntity<ApiResponse<Boolean>> activateUser(@PathVariable String userId, @RequestParam(required = false) String adminIdFromFrontend) {
        logger.info("Received PUT request to /Users/{}/activate, adminId from frontend: {}", userId, adminIdFromFrontend);
        try {
            boolean activated = userService.setUserActivation(userId, true, adminIdFromFrontend);
            return ResponseEntity.ok(new ApiResponse<>(true, "User activated", activated, null));
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.error("Error activating user: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false, "Error activating user", null, e.getMessage()));
        }
    }

    @PutMapping("/{userId}/deactivate")
    public ResponseEntity<ApiResponse<Boolean>> deactivateUser(@PathVariable String userId, @RequestParam(required = false) String adminIdFromFrontend) {
        logger.info("Received PUT request to /Users/{}/deactivate, adminId from frontend: {}", userId, adminIdFromFrontend);
        try {
            boolean deactivated = userService.setUserActivation(userId, false, adminIdFromFrontend);
            return ResponseEntity.ok(new ApiResponse<>(true, "User deactivated", deactivated, null));
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.error("Error deactivating user: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false, "Error deactivating user", null, e.getMessage()));
        }
    }

    @GetMapping("/balance/email/{email}")
    public ResponseEntity<ApiResponse<Double>> getUserBalanceByEmail(@PathVariable String email) {
        logger.info("Received GET request to /Users/balance/email/{}", email);
        try {
            double balance = userService.getUserBalanceByEmail(email);
            return ResponseEntity.ok(new ApiResponse<>(true, "User balance retrieved", balance, null));
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.error("Error retrieving user balance: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false, "Error retrieving user balance", null, e.getMessage()));
        }
    }

    @PutMapping("/balance/email/{email}")
    public ResponseEntity<ApiResponse<Boolean>> updateUserBalanceByEmail(@PathVariable String email, @RequestParam double balance) {
        logger.info("Received PUT request to /Users/balance/email/{} with balance: {}", email, balance);
        try {
            userService.updateUserBalanceByEmail(email, balance);
            return ResponseEntity.ok(new ApiResponse<>(true, "User balance updated", true, null));
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.error("Error updating user balance: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false, "Error updating user balance", null, e.getMessage()));
        }
    }

    @PostMapping("/students/email/{email}/student/{addStudentEmail}")
    public ResponseEntity<ApiResponse<String>> addStudentToParent(
            @PathVariable String email,
            @PathVariable String addStudentEmail,
            @RequestParam(required = false) String adminIdFromFrontend) {
        logger.info("Received POST request to /Users/students/email/{}/student/{}, adminId from frontend: {}", email, addStudentEmail, adminIdFromFrontend);
        try {
            userService.addStudentToParent(email, addStudentEmail, adminIdFromFrontend);
            return ResponseEntity.ok(new ApiResponse<>(true, "Student added successfully", null, null));
        } catch (Exception e) {
            logger.error("Error adding student: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false, "Error adding student", null, e.getMessage()));
        }
    }

    @DeleteMapping("/students/email/{parentEmail}/student/{studentEmail}")
    public ResponseEntity<ApiResponse<String>> removeStudentFromParent(
            @PathVariable String parentEmail,
            @PathVariable String studentEmail,
            @RequestParam(required = false) String adminIdFromFrontend) {
        logger.info("Received DELETE request to /Users/students/email/{}/student/{}, adminId from frontend: {}", parentEmail, studentEmail, adminIdFromFrontend);
        try {
            userService.removeStudentFromParent(parentEmail, studentEmail, adminIdFromFrontend);
            return ResponseEntity.ok(new ApiResponse<>(true, "Student removed successfully", null, null));
        } catch (Exception e) {
            logger.error("Error removing student: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false, "Error removing student", null, e.getMessage()));
        }
    }

    @GetMapping("/students/email/{email}")
    public ResponseEntity<ApiResponse<List<RestUsers>>> getStudentsByParentEmail(@PathVariable String email) {
        try {
            List<RestUsers> students = userService.getStudentsByParentEmail(email);
            ApiResponse<List<RestUsers>> response = new ApiResponse<>(true, "Students retrieved successfully", students, null);
            return ResponseEntity.ok(response);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            ApiResponse<List<RestUsers>> errorResponse = new ApiResponse<>(false, "Error retrieving students: " + e.getMessage(), null, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/id/email/{email}")
    public ResponseEntity<ApiResponse<String>> getUserIdByEmail(@PathVariable String email) {
        logger.info("Received GET request to /Users/id/email/{}", email);
        try {
            String userId = userService.getUserIdByEmail(email);
            if (userId != null) {
                return ResponseEntity.ok(new ApiResponse<>(true, "User ID found", userId, null));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(false, "User not found", null, "No user found with email: " + email));
            }
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.error("Error retrieving user ID by email: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false, "Error retrieving user ID", null, e.getMessage()));
        }
    }

    @PostMapping("/rate/seller")
    public ResponseEntity<ApiResponse<String>> rateSeller(@RequestBody Map<String, Object> payload) {
        logger.info("rateSeller endpoint was hit");
        try {
            String sellerId = (String) payload.get("sellerEmail"); // Frontend is sending seller's User ID as sellerEmail
            String raterEmail = (String) payload.get("raterEmail"); // Email of the user doing the rating
            int rating = ((Number) payload.get("rating")).intValue();

            userService.rateSeller(sellerId, raterEmail, rating); // Updated to use sellerId
            return ResponseEntity.ok(new ApiResponse<>(true, "Seller rated successfully", null, null));
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.error("Error rating seller: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false, "Error rating seller", null, e.getMessage()));
        }
    }
}