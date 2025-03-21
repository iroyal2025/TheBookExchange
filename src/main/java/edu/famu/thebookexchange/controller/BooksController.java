package edu.famu.thebookexchange.controller;

import edu.famu.thebookexchange.service.BooksService;
import edu.famu.thebookexchange.service.UsersService;
import edu.famu.thebookexchange.util.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import edu.famu.thebookexchange.model.Rest.RestBooks;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

@RestController
@RequestMapping("/Books")
public class BooksController {

    private static final Logger logger = LoggerFactory.getLogger(BooksController.class);

    private final BooksService booksService;
    private final UsersService usersService;

    public BooksController(BooksService booksService, UsersService usersService) {
        this.booksService = booksService;
        this.usersService = usersService;
    }

    @GetMapping("/")
    public ResponseEntity<ApiResponse<List<RestBooks>>> getAllBooks() {
        logger.info("getAllBooks endpoint was hit");
        try {
            List<RestBooks> books = booksService.getAllBooks();
            if (!books.isEmpty()) {
                return ResponseEntity.ok(new ApiResponse<>(true, "Books retrieved successfully", books, null));
            } else {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body(new ApiResponse<>(true, "No books found", null, null));
            }
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.error("Error retrieving books: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false, "Error retrieving books", null, e.getMessage()));
        }
    }

    @PostMapping("/add")
    public ResponseEntity<ApiResponse<String>> addBook(@RequestBody RestBooks book) {
        try {
            String bookId = booksService.addBook(book);
            return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(true, "Book added successfully", bookId, null));
        } catch (InterruptedException | ExecutionException e) {
            logger.error("Error adding book: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false, "Error adding book", null, e.getMessage()));
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<ApiResponse<Boolean>> deleteBookByTitle(@RequestParam String title) {
        try {
            boolean deleted = booksService.deleteBookByTitle(title);
            if (deleted) {
                return ResponseEntity.ok(new ApiResponse<>(true, "Book deleted successfully", deleted, null));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(false, "Book not found or failed to delete", deleted, null));
            }
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.error("Error deleting book: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false, "Error deleting book", false, e.getMessage()));
        }
    }

    @PutMapping("/{bookId}")
    public ResponseEntity<ApiResponse<String>> updateBook(@PathVariable String bookId, @RequestBody RestBooks updatedBook) {
        try {
            String updateResult = booksService.updateBook(bookId, updatedBook);
            if (updateResult != null) {
                return ResponseEntity.ok(new ApiResponse<>(true, "Book updated successfully", updateResult, null));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(false, "Book not found or failed to update", null, null));
            }
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.error("Error updating book: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false, "Error updating book", null, e.getMessage()));
        }
    }

    @PutMapping("/{bookId}/purchase/{userId}")
    public ResponseEntity<ApiResponse<Boolean>> purchaseBook(@PathVariable String bookId, @PathVariable String userId) {
        try {
            RestBooks book = booksService.getBookById(bookId);
            if(book == null){
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(false, "Book not found", false, null));
            }
            double bookPrice = book.getPrice();
            double userBalance = usersService.getUserBalance(userId);

            if (userBalance >= bookPrice) {
                usersService.updateUserBalance(userId, userBalance - bookPrice);

                boolean purchaseResult = booksService.purchaseBook(bookId, userId);

                if (purchaseResult) {
                    return ResponseEntity.ok(new ApiResponse<>(true, "Book purchased successfully", purchaseResult, null));
                } else {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false, "Failed to purchase book", purchaseResult, null));
                }
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse<>(false, "Insufficient funds", false, "Insufficient funds"));
            }

        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.error("Error purchasing book: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false, "Error purchasing book", false, e.getMessage()));
        }
    }

    @GetMapping("/owned/{userId}")
    public ResponseEntity<ApiResponse<List<RestBooks>>> findBooksOwnedByUser(@PathVariable String userId) {
        try {
            List<RestBooks> ownedBooks = booksService.findBooksOwnedByUser(userId);
            if (!ownedBooks.isEmpty()) {
                return ResponseEntity.ok(new ApiResponse<>(true, "Books owned by user retrieved successfully", ownedBooks, null));
            } else {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body(new ApiResponse<>(true, "No books found owned by user", null, null));
            }
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.error("Error finding books owned by user: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false, "Error finding books owned by user", null, e.getMessage()));
        }
    }
}