package edu.famu.thebookexchange.controller;

import edu.famu.thebookexchange.model.Rest.RestBooks;
import edu.famu.thebookexchange.service.BooksService;
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
@RequestMapping("/Books")
// For UI: http://localhost:8080/
// For Postman: http://localhost:8080/Books
public class BooksController {

    @Autowired
    private BooksService bookService;

    @GetMapping("/")
    public ResponseEntity<ApiResponse<List<RestBooks>>> getAllBooks() {
        ApiResponse<List<RestBooks>> response = bookService.getAllBooks().getBody();
        if (response != null && response.data() != null && !response.data().isEmpty()) {
            return ResponseEntity.ok(new ApiResponse<>(true, "Books List", response.data(), null));
        } else {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(new ApiResponse<>(true, "No books found", null, null));
        }
    }

    @PostMapping("/add")
    public ResponseEntity<ApiResponse<RestBooks>> addBook(@RequestBody RestBooks book) {
        ApiResponse<RestBooks> response = bookService.addBook(book).getBody();
        if (response != null && response.data() != null) {
            return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(true, "Book created", response.data(), null));
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false, "Error creating book", null, null));
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<ApiResponse<String>> deleteBookByTitle(@RequestParam String title) {
        try {
            boolean deleted = bookService.deleteBookByTitle(title);

            if (deleted) {
                return ResponseEntity.ok(new ApiResponse<>(true, "Book deleted successfully", null, null));
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false, "Failed to delete book", null, null));
            }
        } catch (ExecutionException | InterruptedException | TimeoutException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false, "Error deleting book", null, e.getMessage()));
        }
    }

    @GetMapping("/Users/{userId}")
    public ResponseEntity<ApiResponse<List<RestBooks>>> getBooksByUserId(@PathVariable String userId) {
        ApiResponse<List<RestBooks>> response = bookService.getBooksByUserId(userId).getBody();
        if (response != null && response.data() != null && !response.data().isEmpty()) {
            return ResponseEntity.ok(new ApiResponse<>(true, "Books by User", response.data(), null));
        } else {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(new ApiResponse<>(true, "No books found for user", null, null));
        }
    }

    @GetMapping("/Courses/{courseId}")
    public ResponseEntity<ApiResponse<List<RestBooks>>> getBooksByCourseId(@PathVariable String courseId) {
        ApiResponse<List<RestBooks>> response = bookService.getBooksByCourseId(courseId).getBody();
        if (response != null && response.data() != null && !response.data().isEmpty()) {
            return ResponseEntity.ok(new ApiResponse<>(true, "Books by Course", response.data(), null));
        } else {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(new ApiResponse<>(true, "No books found for course", null, null));
        }
    }
}