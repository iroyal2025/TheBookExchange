package edu.famu.thebookexchange.controller;

import edu.famu.thebookexchange.model.Rest.RestForums;
import edu.famu.thebookexchange.service.ForumsService;
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
@RequestMapping("/Forums")
public class ForumsController {

    @Autowired
    private ForumsService forumService;

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
}