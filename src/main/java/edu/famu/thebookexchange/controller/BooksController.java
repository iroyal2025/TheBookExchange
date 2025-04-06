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
import java.util.Map;
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
            logger.debug("Retrieved books: {}", books); // Log the retrieved books
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
        logger.info("addBook endpoint was hit");
        logger.debug("Adding book: {}", book); // Log the book being added
        try {
            String bookId = booksService.addBook(book);
            logger.debug("Book added with ID: {}", bookId); // Log the added book ID
            return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(true, "Book added successfully", bookId, null));
        } catch (InterruptedException | ExecutionException e) {
            logger.error("Error adding book: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false, "Error adding book", null, e.getMessage()));
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<ApiResponse<Boolean>> deleteBookByTitle(@RequestParam String title) {
        logger.info("deleteBookByTitle endpoint was hit with title: {}", title);
        logger.debug("Deleting book with title: {}", title); // Log the title being deleted
        try {
            boolean deleted = booksService.deleteBookByTitle(title);
            if (deleted) {
                logger.debug("Book deleted successfully with title: {}", title); // Log successful deletion
                return ResponseEntity.ok(new ApiResponse<>(true, "Book deleted successfully", deleted, null));
            } else {
                logger.debug("Book not found or failed to delete with title: {}", title); // Log failure
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(false, "Book not found or failed to delete", deleted, null));
            }
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.error("Error deleting book: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false, "Error deleting book", false, e.getMessage()));
        }
    }

    @PutMapping("/{bookId}")
    public ResponseEntity<ApiResponse<String>> updateBook(@PathVariable String bookId, @RequestBody RestBooks updatedBook) {
        logger.info("updateBook endpoint was hit with bookId: {}", bookId);
        logger.debug("Updating book with ID: {}, Updated book: {}", bookId, updatedBook); // Log update details
        try {
            String updateResult = booksService.updateBook(bookId, updatedBook);
            if (updateResult != null) {
                logger.debug("Book updated successfully with ID: {}", bookId); // Log successful update
                return ResponseEntity.ok(new ApiResponse<>(true, "Book updated successfully", updateResult, null));
            } else {
                logger.debug("Book not found or failed to update with ID: {}", bookId); // Log failure
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(false, "Book not found or failed to update", null, null));
            }
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.error("Error updating book: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false, "Error updating book", null, e.getMessage()));
        }
    }

    @PutMapping("/{bookId}/purchase/email/{email}")
    public ResponseEntity<ApiResponse<Double>> purchaseBook(@PathVariable String bookId, @PathVariable String email) {
        logger.info("purchaseBook endpoint was hit with bookId: {} and email: {}", bookId, email);
        try {
            RestBooks book = booksService.getBookById(bookId);
            if (book == null) {
                logger.error("Book not found for bookId: {}", bookId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(false, "Book not found", null, null));
            }
            double bookPrice = book.getPrice();
            double userBalance = usersService.getUserBalanceByEmail(email);
            logger.info("Book price: {}, User balance: {}", bookPrice, userBalance);

            if (userBalance >= bookPrice) {
                double updatedBalance = booksService.purchaseBook(bookId, email); // Get updated balance

                if (updatedBalance >= 0) { // Check if purchase was successful
                    logger.info("Book purchased successfully, updated balance: {}", updatedBalance);
                    return ResponseEntity.ok(new ApiResponse<>(true, "Book purchased successfully", updatedBalance, null));
                } else {
                    logger.error("Failed to purchase book");
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false, "Failed to purchase book", null, null));
                }
            } else {
                logger.warn("Insufficient funds for email: {}", email);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse<>(false, "Insufficient funds", null, "Insufficient funds"));
            }
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.error("Error purchasing book: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false, "Error purchasing book", null, e.getMessage()));
        }
    }
    @GetMapping("/owned/email/{email}") // Updated path variable
    public ResponseEntity<ApiResponse<List<RestBooks>>> findBooksOwnedByUser(@PathVariable String email) { // Updated parameter
        logger.info("findBooksOwnedByUser endpoint was hit with email: {}", email); // Updated log message
        logger.debug("Finding books owned by email: {}", email); // Updated log message
        try {
            List<RestBooks> ownedBooks = booksService.findBooksOwnedByUserEmail(email); // Updated method call
            logger.debug("Owned books retrieved: {}", ownedBooks); // Log retrieved books
            if (!ownedBooks.isEmpty()) {
                return ResponseEntity.ok(new ApiResponse<>(true, "Books owned by user retrieved successfully", ownedBooks, null));
            } else {
                logger.debug("No books found owned by email: {}", email); // Updated log message
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body(new ApiResponse<>(true, "No books found owned by user", null, null));
            }
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.error("Error finding books owned by user: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false, "Error finding books owned by user", null, e.getMessage()));
        }
    }
    @DeleteMapping("/remove/title/{bookTitle}")
    public ResponseEntity<ApiResponse<String>> removeBookByTitle(@PathVariable String bookTitle) {
        try {
            boolean removed = booksService.removeBookByTitle(bookTitle); // Correct call
            if (removed) {
                return ResponseEntity.ok(new ApiResponse<>(true, "Book removed successfully", null, null));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(false, "Book not found", null, null));
            }
        } catch (ExecutionException | InterruptedException | TimeoutException e) {
            logger.error("Error removing book: {}", bookTitle, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false, "Error removing book", null, e.getMessage()));
        }
    }

    @PostMapping("/rate")
    public ResponseEntity<ApiResponse<String>> rateBook(@RequestBody Map<String, Object> payload) {
        logger.info("rateBook endpoint was hit");
        try {
            String bookTitle = (String) payload.get("bookTitle");
            String userEmail = (String) payload.get("userEmail");
            int rating = ((Number) payload.get("rating")).intValue();

            booksService.rateBook(bookTitle, userEmail, rating);
            return ResponseEntity.ok(new ApiResponse<>(true, "Book rated successfully", null, null));
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.error("Error rating book: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false, "Error rating book", null, e.getMessage()));
        }
    }

}