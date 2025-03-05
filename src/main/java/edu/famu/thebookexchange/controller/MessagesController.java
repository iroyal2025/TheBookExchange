
package edu.famu.thebookexchange.controller;

import edu.famu.thebookexchange.model.Rest.RestMessages;
import edu.famu.thebookexchange.service.MessagesService;
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
@RequestMapping("/Messages")
public class MessagesController {

    @Autowired
    private MessagesService messageService;

    @GetMapping("/")
    public ResponseEntity<ApiResponse<List<RestMessages>>> getAllMessages() {
        try {
            List<RestMessages> restMessages = messageService.getAllMessages();

            if (!restMessages.isEmpty()) {
                return ResponseEntity.ok(new ApiResponse<>(true, "Messages List", restMessages, null));
            } else {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body(new ApiResponse<>(true, "No messages found", null, null));
            }
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false, "Error retrieving messages", null, e.getMessage()));
        }
    }

    @PostMapping("/add")
    public ResponseEntity<ApiResponse<String>> addMessage(@RequestBody RestMessages message) {
        try {
            String messageId = messageService.addMessage(message);
            return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(true, "Message created", messageId, null));
        } catch (InterruptedException | ExecutionException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false, "Error creating message", null, e.getMessage()));
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<ApiResponse<String>> deleteMessageByContent(@RequestParam String context) {
        try {
            boolean deleted = messageService.deleteMessageByContent(context);

            if (deleted) {
                return ResponseEntity.ok(new ApiResponse<>(true, "Message deleted successfully", null, null));
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false, "Failed to delete message", null, null));
            }
        } catch (ExecutionException | InterruptedException | TimeoutException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false, "Error deleting message", null, e.getMessage()));
        }
    }

    @PutMapping("/{messageId}")
    public ResponseEntity<ApiResponse<String>> updateMessage(@PathVariable String messageId, @RequestBody RestMessages updatedMessage) {
        try {
            String updateTime = messageService.updateMessage(messageId, updatedMessage);
            return ResponseEntity.ok(new ApiResponse<>(true, "Message updated", updateTime, null));
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false, "Error updating message", null, e.getMessage()));
        }
    }
}