package edu.famu.thebookexchange.controller;

import edu.famu.thebookexchange.model.Rest.RestUsers;
import edu.famu.thebookexchange.service.BooksService;
import edu.famu.thebookexchange.service.ExchangeService;
import edu.famu.thebookexchange.service.UsersService;
import edu.famu.thebookexchange.util.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import edu.famu.thebookexchange.model.Rest.RestBooks;

import java.awt.print.Book;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/Books")
public class BooksController {

    private static final Logger logger = LoggerFactory.getLogger(BooksController.class);

    private final BooksService booksService;
    private final UsersService usersService;

    @Autowired
    private ExchangeService exchangeService; // Inject ExchangeService

    public BooksController(BooksService booksService, UsersService usersService) {
        this.booksService = booksService;
        this.usersService = usersService;
    }


    @GetMapping("/")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getAllBooksWithSellerRating() {
        logger.info("getAllBooksWithSellerRating endpoint was hit");
        try {
            List<Map<String, Object>> booksWithSellerRating = booksService.getAllBooksWithSellerRating();
            logger.debug("Retrieved books with seller rating: {}", booksWithSellerRating);
            if (!booksWithSellerRating.isEmpty()) {
                return ResponseEntity.ok(new ApiResponse<>(true, "Books retrieved successfully with seller rating", booksWithSellerRating, null));
            } else {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body(new ApiResponse<>(true, "No books found", null, null));
            }
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.error("Error retrieving books with seller rating: {}", e.getMessage(), e);
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
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.error("Error adding book: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false, "Error adding book", null, e.getMessage()));
        }
        // If you throw a custom NotFoundException in BooksService:
        /*
        catch (NotFoundException e) {
            logger.error("Error adding book: User not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(false, "Error adding book: User not found", null, e.getMessage()));
        }
        */
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

    @DeleteMapping("/{bookId}")
    public ResponseEntity<ApiResponse<Void>> deleteBook(@PathVariable String bookId) {
        logger.info("Received request to delete book with ID: {}", bookId);
        try {
            boolean deleted = booksService.deleteBook(bookId); // Assuming you have this method
            if (deleted) {
                logger.debug("Book with ID {} deleted successfully.", bookId);
                return ResponseEntity.ok(new ApiResponse<>(true, "Book deleted successfully", null, null));
            } else {
                logger.warn("Book with ID {} not found or could not be deleted.", bookId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(false, "Book not found or could not be deleted", null, null));
            }
        } catch (Exception e) {
            logger.error("Error deleting book with ID {}: {}", bookId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false, "Error deleting book", null, e.getMessage()));
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

    @GetMapping("/{sellerEmail}/listings")
    public ResponseEntity<ApiResponse<List<RestBooks>>> getSellerListings(@PathVariable String sellerEmail) {
        logger.info("getSellerListings endpoint was hit for seller: {}", sellerEmail);
        logger.debug("Fetching listings for seller with email: {}", sellerEmail);
        try {
            // Use the method in BooksService designed to fetch seller listings
            List<RestBooks> sellerBooks = booksService.getSellerListings(sellerEmail);
            logger.debug("Retrieved listings for seller {}: {}", sellerEmail, sellerBooks);
            if (!sellerBooks.isEmpty()) {
                return ResponseEntity.ok(new ApiResponse<>(true, "Seller listings retrieved successfully", sellerBooks, null));
            } else {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body(new ApiResponse<>(true, "No listings found for this seller", null, null));
            }
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.error("Error retrieving listings for seller {}: {}", sellerEmail, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false, "Error retrieving seller listings", null, e.getMessage()));
        }
    }

    @DeleteMapping("/listing/bookId/{bookId}/email/{sellerEmail}")
    public ResponseEntity<Boolean> removeBookFromSellerListingByBookId(
            @PathVariable String bookId,
            @PathVariable String sellerEmail) {
        logger.info("Received request to remove book with ID '{}' from listing of seller with email: {}", bookId, sellerEmail);
        try {
            boolean removed = booksService.removeBookFromListingByEmailAndBookId(bookId, sellerEmail);
            if (removed) {
                return new ResponseEntity<>(true, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(false, HttpStatus.NOT_FOUND);
            }
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.error("Error removing book with ID '{}' for seller '{}': {}", bookId, sellerEmail, e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @GetMapping("/Books/{bookId}/owners")
    public ResponseEntity<Map<String, Object>> getBookOwners(@PathVariable String bookId, @RequestParam(required = false) String exclude) {
        logger.info("getBookOwners endpoint was hit with bookId: {} and exclude: {}", bookId, exclude);
        try {
            logger.debug("Attempting to retrieve book with ID: {}", bookId);
            RestBooks book = booksService.findBookById(bookId);
            logger.debug("Result of findBookById: {}", book);

            if (book != null) {
                logger.debug("Book found. Checking ownedBy list: {}", book.getOwnedBy());
                if (book.getOwnedBy() != null) {
                    List<String> owners = book.getOwnedBy().stream()
                            .filter(owner -> exclude == null || !owner.equals(exclude))
                            .collect(Collectors.toList());
                    logger.debug("Filtered owners: {}", owners);
                    return ResponseEntity.ok(Map.of("success", true, "data", owners));
                } else {
                    logger.warn("Book found with ID {}, but ownedBy list is null.", bookId);
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("success", false, "message", "Book found but has no owners."));
                }
            } else {
                logger.warn("Book not found with ID: {}", bookId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("success", false, "message", "Book not found."));
            }
        } catch (Exception e) {
            logger.error("Error fetching book owners for book ID {}: {}", bookId, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("success", false, "message", "Failed to fetch book owners: " + e.getMessage()));
        }
    }

    @PostMapping("/Exchanges/direct/request")
    public ResponseEntity<Map<String, Object>> sendDirectExchangeRequest(@RequestBody Map<String, String> payload) {
        String offeredBookId = payload.get("offeredBookId");
        String recipientEmail = payload.get("recipientEmail");
        String requesterEmail = payload.get("requesterEmail");

        if (offeredBookId == null || recipientEmail == null || requesterEmail == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Missing required fields."));
        }

        try {
            RestBooks offeredBook = booksService.findBookById(offeredBookId);
            if (offeredBook == null || !offeredBook.getOwnedBy().contains(requesterEmail)) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Offered book not found or not owned by the requester."));
            }

            exchangeService.createDirectExchangeRequest(offeredBookId, recipientEmail, requesterEmail); // Implement this service method
            return ResponseEntity.ok(Map.of("success", true, "message", "Direct exchange request sent successfully."));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("success", false, "message", "Failed to send direct exchange request: " + e.getMessage()));
        }
    }

    @PutMapping("/{bookId}/exchangeable")
    public ResponseEntity<ApiResponse<String>> markBookAsExchangeable(@PathVariable String bookId, @RequestParam boolean isExchangeable) {
        logger.info("markBookAsExchangeable endpoint hit for bookId: {} with isExchangeable: {}", bookId, isExchangeable);
        try {
            boolean updated = booksService.updateBookExchangeableStatus(bookId, isExchangeable);
            if (updated) {
                return ResponseEntity.ok(new ApiResponse<>(true, "Book exchangeable status updated successfully", null, null));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(false, "Book not found", null, null));
            }
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.error("Error updating book exchangeable status: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(new ApiResponse<>(false, "Error updating book exchangeable status", null, e.getMessage()));
        }
    }

    @GetMapping("/exchangeable")
    public ResponseEntity<Map<String, Object>> getExchangeableBooks(@RequestParam String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "User ID is required."));
        }
        try {
            List<RestBooks> exchangeableBooks = booksService.findExchangeableBooks(userId);
            return ResponseEntity.ok(Map.of("success", true, "data", exchangeableBooks));
        } catch (Exception e) {
            logger.error("Failed to fetch exchangeable books for user {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("success", false, "message", "Failed to fetch exchangeable books: " + e.getMessage()));
        }
    }

    @GetMapping("/{bookId}/owners/students")
    public ResponseEntity<Map<String, Object>> getStudentBookOwners(@PathVariable String bookId, @RequestParam(required = false) String exclude) {
        try {
            RestBooks book = booksService.findBookById(bookId);
            if (book != null && book.getOwnedBy() != null) {
                List<String> studentOwners = new ArrayList<>();
                for (String ownerEmail : book.getOwnedBy()) {
                    if (exclude == null || !ownerEmail.equals(exclude)) {
                        // Assuming you have a method in UsersService to get user details by email
                        RestUsers owner = usersService.getUserByEmail(ownerEmail);
                        if (owner != null && "student".equals(owner.getRole())) {
                            studentOwners.add(ownerEmail);
                        }
                    }
                }
                return ResponseEntity.ok(Map.of("success", true, "data", studentOwners));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("success", false, "message", "Book not found or has no owners."));
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("success", false, "message", "Failed to fetch student book owners: " + e.getMessage()));
        }
    }
}
