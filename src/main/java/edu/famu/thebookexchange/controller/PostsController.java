package edu.famu.thebookexchange.controller;

import edu.famu.thebookexchange.model.Rest.RestPosts;
import edu.famu.thebookexchange.service.PostsService;
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
@RequestMapping("/Posts")
public class PostsController {

    @Autowired
    private PostsService postService;

    @GetMapping("/")
    public ResponseEntity<ApiResponse<List<RestPosts>>> getAllPosts() {
        try {
            List<RestPosts> restPosts = postService.getAllPosts();

            if (!restPosts.isEmpty()) {
                return ResponseEntity.ok(new ApiResponse<>(true, "Posts List", restPosts, null));
            } else {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body(new ApiResponse<>(true, "No posts found", null, null));
            }
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false, "Error retrieving posts", null, e.getMessage()));
        }
    }

    @PostMapping("/add")
    public ResponseEntity<ApiResponse<String>> addPost(@RequestBody RestPosts post) {
        try {
            String postId = postService.addPost(post);
            return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(true, "Post created", postId, null));
        } catch (InterruptedException | ExecutionException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false, "Error creating post", null, e.getMessage()));
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<ApiResponse<String>> deletePostByContent(@RequestParam String content) {
        try {
            boolean deleted = postService.deletePostByContent(content);

            if (deleted) {
                return ResponseEntity.ok(new ApiResponse<>(true, "Post deleted successfully", null, null));
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false, "Failed to delete post", null, null));
            }
        } catch (ExecutionException | InterruptedException | TimeoutException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false, "Error deleting post", null, e.getMessage()));
        }
    }

    @PutMapping("/{postId}")
    public ResponseEntity<ApiResponse<String>> updatePost(@PathVariable String postId, @RequestBody RestPosts updatedPost) {
        try {
            String updateTime = postService.updatePost(postId, updatedPost);
            return ResponseEntity.ok(new ApiResponse<>(true, "Post updated", updateTime, null));
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false, "Error updating post", null, e.getMessage()));
        }
    }
}