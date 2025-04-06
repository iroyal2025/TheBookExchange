package edu.famu.thebookexchange.controller;

import edu.famu.thebookexchange.model.Rest.RestForums;
import edu.famu.thebookexchange.service.FeedbackService;
import edu.famu.thebookexchange.util.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map; // Import for feedback response
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.lang.InterruptedException;

@RestController
@RequestMapping("/Forums")
public class FeedbackController {

    @Autowired
    private FeedbackService forumService; // Changed to ForumsService

    @GetMapping("/")
    public ResponseEntity<ApiResponse<List<RestForums>>> getAllForums() {
        try {
            List<RestForums> restForums = forumService.getAllForums();

            if (!restForums.isEmpty()) {
                return ResponseEntity.ok(new ApiResponse<>(true, "Forums List", restForums, null));
            } else {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body(new ApiResponse<>(true, "No forums found", null, null));
            }
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false, "Error retrieving forums", null, e.getMessage()));
        }
    }

    @PostMapping("/add")
    public ResponseEntity<ApiResponse<String>> addForum(@RequestBody RestForums forum) {
        try {
            String forumId = forumService.addForum(forum);
            return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(true, "Forum created", forumId, null));
        } catch (InterruptedException | ExecutionException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false, "Error creating forum", null, e.getMessage()));
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<ApiResponse<String>> deleteForumByTopic(@RequestParam String topic) {
        try {
            boolean deleted = forumService.deleteForumByTopic(topic);

            if (deleted) {
                return ResponseEntity.ok(new ApiResponse<>(true, "Forum deleted successfully", null, null));
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false, "Failed to delete forum", null, null));
            }
        } catch (ExecutionException | InterruptedException | TimeoutException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false, "Error deleting forum", null, e.getMessage()));
        }
    }

    @PutMapping("/{forumId}")
    public ResponseEntity<ApiResponse<String>> updateForum(@PathVariable String forumId, @RequestBody RestForums updatedForum) {
        try {
            String updateTime = forumService.updateForum(forumId, updatedForum);
            return ResponseEntity.ok(new ApiResponse<>(true, "Forum updated", updateTime, null));
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false, "Error updating forum", null, e.getMessage()));
        }
    }

    // New Feedback Endpoints

    @PostMapping("/feedback/book/{bookId}")
    public ResponseEntity<ApiResponse<String>> addFeedback(@PathVariable String bookId, @RequestBody Map<String, String> body) {
        try {
            String feedback = body.get("feedback");
            String feedbackId = forumService.addFeedback(bookId, feedback);
            return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(true, "Feedback added", feedbackId, null));
        } catch (InterruptedException | ExecutionException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false, "Error adding feedback", null, e.getMessage()));
        }
    }

    @GetMapping("/feedback/book/{bookId}")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getFeedbackForBook(@PathVariable String bookId) {
        try {
            List<Map<String, Object>> feedbackList = forumService.getFeedbackForBook(bookId);
            return ResponseEntity.ok(new ApiResponse<>(true, "Feedback retrieved", feedbackList, null));
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false, "Error retrieving feedback", null, e.getMessage()));
        }
    }

    @DeleteMapping("/feedback/{feedbackId}")
    public ResponseEntity<ApiResponse<String>> deleteFeedback(@PathVariable String feedbackId) {
        try {
            boolean deleted = forumService.deleteFeedback(feedbackId);
            if (deleted) {
                return ResponseEntity.ok(new ApiResponse<>(true, "Feedback deleted", null, null));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(false, "Feedback not found", null, null));
            }

        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false, "Error deleting feedback", null, e.getMessage()));
        }
    }

    @PutMapping("/feedback/{feedbackId}")
    public ResponseEntity<ApiResponse<String>> updateFeedback(@PathVariable String feedbackId, @RequestBody Map<String, String> body) {
        try {
            String updatedFeedback = body.get("feedback");
            String updateTime = forumService.updateFeedback(feedbackId, updatedFeedback);
            return ResponseEntity.ok(new ApiResponse<>(true, "Feedback updated", updateTime, null));
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false, "Error updating feedback", null, e.getMessage()));
        }
    }
}