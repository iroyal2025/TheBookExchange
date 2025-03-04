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
public class BooksController {

    @Autowired
    private BooksService bookService;

    @GetMapping("/")
    public ResponseEntity<ApiResponse<List<RestBooks>>> getAllBooks() {
        try {
            List<RestBooks> restBooks = bookService.getAllBooks();

            if (!restBooks.isEmpty()) {
                return ResponseEntity.ok(new ApiResponse<>(true, "Books List", restBooks, null));
            } else {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body(new ApiResponse<>(true, "No books found", null, null));
            }
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false, "Error retrieving books", null, e.getMessage()));
        }
    }

    @PostMapping("/add")
    public ResponseEntity<ApiResponse<String>> addBook(@RequestBody RestBooks book) {
        try {
            String bookId = bookService.addBook(book);
            return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(true, "Book created", bookId, null));
        } catch (InterruptedException | ExecutionException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false, "Error creating book", null, e.getMessage()));
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

    @PutMapping("/{bookId}")
    public ResponseEntity<ApiResponse<String>> updateBook(@PathVariable String bookId, @RequestBody RestBooks updatedBook) {
        try {
            String updateTime = bookService.updateBook(bookId, updatedBook);
            return ResponseEntity.ok(new ApiResponse<>(true, "Book updated", updateTime, null));
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false, "Error updating book", null, e.getMessage()));
        }
    }

    @GetMapping("/Users/{userId}")
    public ResponseEntity<ApiResponse<List<RestBooks>>> getBooksByUserId(@PathVariable String userId) {
        // Implement getBooksByUserId logic if needed.
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(new ApiResponse<>(false, "Not implemented", null, null));
    }

    @GetMapping("/Courses/{courseId}")
    public ResponseEntity<ApiResponse<List<RestBooks>>> getBooksByCourseId(@PathVariable String courseId) {
        // Implement getBooksByCourseId logic if needed.
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(new ApiResponse<>(false, "Not implemented", null, null));
    }
}