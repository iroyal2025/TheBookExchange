package edu.famu.thebookexchange.controller;

import edu.famu.thebookexchange.model.Rest.RestUsers;
import edu.famu.thebookexchange.service.UsersService;
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
@RequestMapping("/Users")
public class UsersController {

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
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false, "Error retrieving users", null, e.getMessage()));
        }
    }

    @PostMapping("/add")
    public ResponseEntity<ApiResponse<String>> addUser(@RequestBody RestUsers user) {
        try {
            String userId = userService.addUser(user);
            return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(true, "User created", userId, null));
        } catch (InterruptedException | ExecutionException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false, "Error creating user", null, e.getMessage()));
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<ApiResponse<String>> deleteUserByEmail(@RequestParam String email) {
        try {
            boolean deleted = userService.deleteUserByEmail(email);

            if (deleted) {
                return ResponseEntity.ok(new ApiResponse<>(true, "User deleted successfully", null, null));
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false, "Failed to delete user", null, null));
            }
        } catch (ExecutionException | InterruptedException | TimeoutException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false, "Error deleting user", null, e.getMessage()));
        }
    }

    @PutMapping("/{userId}")
    public ResponseEntity<ApiResponse<String>> updateUser(@PathVariable String userId, @RequestBody RestUsers updatedUser) {
        try {
            String updateTime = userService.updateUser(userId, updatedUser);
            return ResponseEntity.ok(new ApiResponse<>(true, "User updated", updateTime, null));
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false, "Error updating user", null, e.getMessage()));
        }
    }
}