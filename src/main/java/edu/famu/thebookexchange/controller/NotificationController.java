package edu.famu.thebookexchange.controller;

import edu.famu.thebookexchange.model.Rest.RestNotification;
import edu.famu.thebookexchange.model.Rest.RestUsers;
import edu.famu.thebookexchange.service.NotificationService;
import edu.famu.thebookexchange.service.UsersService; // Import UsersService
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
@RequestMapping("/Notifications")
public class NotificationController {

    private static final Logger logger = LoggerFactory.getLogger(NotificationController.class);

    private final NotificationService notificationService;
    private final UsersService usersService; // Inject UsersService

    @Autowired
    public NotificationController(NotificationService notificationService, UsersService usersService) {
        this.notificationService = notificationService;
        this.usersService = usersService;
    }

    @GetMapping("/")
    public ResponseEntity<ApiResponse<List<RestNotification>>> getUserNotifications(@RequestParam String userId) {
        logger.info("Received GET request to /Notifications/ with userId: {}", userId);
        try {
            List<RestNotification> notifications = notificationService.getUserNotifications(userId);
            return ResponseEntity.ok(new ApiResponse<>(true, "User notifications retrieved", notifications, null));
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.error("Error retrieving notifications for user {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false, "Error retrieving notifications", null, e.getMessage()));
        }
    }

    @PostMapping("/add")
    public ResponseEntity<ApiResponse<String>> addNotification(@RequestBody RestNotification notification) {
        logger.info("Received POST request to /Notifications/add with data: {}", notification);
        try {
            String notificationId = notificationService.addNotification(notification);
            return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(true, "Notification created", notificationId, null));
        } catch (InterruptedException | ExecutionException e) {
            logger.error("Error creating notification: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false, "Error creating notification", null, e.getMessage()));
        }
    }

    @PutMapping("/{notificationId}/read")
    public ResponseEntity<ApiResponse<String>> markNotificationAsRead(@PathVariable String notificationId) {
        logger.info("Received PUT request to /Notifications/{}/read", notificationId);
        try {
            boolean updated = notificationService.markNotificationAsRead(notificationId);
            if (updated) {
                return ResponseEntity.ok(new ApiResponse<>(true, "Notification marked as read", null, null));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(false, "Notification not found", null, null));
            }
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.error("Error marking notification {} as read: {}", notificationId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false, "Error marking notification as read", null, e.getMessage()));
        }
    }

    @DeleteMapping("/byMessage")
    public ResponseEntity<ApiResponse<Boolean>> deleteNotificationByMessage(@RequestParam String message) {
        logger.info("Received DELETE request to /Notifications/byMessage with message: {}", message);
        try {
            boolean deleted = notificationService.deleteNotificationByMessage(message);
            return ResponseEntity.ok(new ApiResponse<>(true, "Notifications deleted successfully", deleted, null));
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.error("Error deleting notifications with message {}: {}", message, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false, "Error deleting notifications", null, e.getMessage()));
        }
    }

    @DeleteMapping("/{notificationId}")
    public ResponseEntity<ApiResponse<String>> deleteNotification(@PathVariable String notificationId) {
        logger.warn("DELETE request to /Notifications/{} is deprecated. Use /Notifications/byMessage instead.", notificationId);
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(new ApiResponse<>(false, "This endpoint is deprecated. Use /Notifications/byMessage to delete by message.", null, null));
    }

    // Endpoint to trigger a new notification (for testing or manual actions)
    @PostMapping("/trigger")
    public ResponseEntity<ApiResponse<String>> triggerNotification(@RequestBody Map<String, Object> payload) {
        logger.info("Received POST request to /Notifications/trigger with payload: {}", payload);
        try {
            String userId = (String) payload.get("userId");
            String type = (String) payload.get("type");
            String message = (String) payload.get("message");
            String link = (String) payload.get("link");
            String relatedItemId = (String) payload.get("relatedItemId");

            String notificationId = notificationService.createNotification(userId, type, message, link, relatedItemId);
            return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(true, "Notification triggered", notificationId, null));
        } catch (InterruptedException | ExecutionException e) {
            logger.error("Error triggering notification: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false, "Error triggering notification", null, e.getMessage()));
        }
    }

    @GetMapping("/admin")
    public ResponseEntity<ApiResponse<List<RestNotification>>> getAdminNotifications(@RequestParam String userId) {
        logger.info("Received GET request to /Notifications/admin for userId: {}", userId);
        try {
            List<RestNotification> adminNotifications = notificationService.getNotificationsForUser(userId); // Use a new service method
            return ResponseEntity.ok(new ApiResponse<>(true, "Admin Notifications", adminNotifications, null));
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.error("Error retrieving notifications for user {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false, "Error retrieving notifications", null, e.getMessage()));
        }
    }
}