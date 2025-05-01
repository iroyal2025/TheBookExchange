package edu.famu.thebookexchange.controller;

import edu.famu.thebookexchange.model.Default.Wishlist;
import edu.famu.thebookexchange.model.Rest.RestWishlist;
import edu.famu.thebookexchange.service.WishlistService;
import edu.famu.thebookexchange.util.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

@RestController
@RequestMapping("/wishlist") // Changed mapping to lowercase 'wishlist' for consistency
public class WishlistController {

    @Autowired
    private WishlistService wishlistService;

    @GetMapping("/")
    public ResponseEntity<ApiResponse<List<RestWishlist>>> getAllWishlists() {
        try {
            List<RestWishlist> restWishlists = wishlistService.getAllWishlists();

            if (!restWishlists.isEmpty()) {
                return ResponseEntity.ok(new ApiResponse<>(true, "Wishlists retrieved successfully", restWishlists, null));
            } else {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body(new ApiResponse<>(true, "No wishlists found", null, null));
            }
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false, "Error retrieving wishlists", null, e.getMessage()));
        }
    }

    @PostMapping("/add")
    public ResponseEntity<ApiResponse<String>> addWishlist(@RequestBody RestWishlist wishlist) {
        try {
            String wishlistId = wishlistService.addWishlist(wishlist);
            return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(true, "Wishlist created successfully", wishlistId, null));
        } catch (InterruptedException | ExecutionException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false, "Error creating wishlist", null, e.getMessage()));
        }
    }

    @DeleteMapping("/{wishlistId}")
    public ResponseEntity<ApiResponse<String>> deleteWishlistById(@PathVariable String wishlistId) {
        try {
            boolean deleted = wishlistService.deleteWishlistById(wishlistId);

            if (deleted) {
                return ResponseEntity.ok(new ApiResponse<>(true, "Wishlist deleted successfully", null, null));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(false, "Wishlist not found", null, null));
            }
        } catch (ExecutionException | InterruptedException | TimeoutException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false, "Error deleting wishlist", null, e.getMessage()));
        }
    }

    @PutMapping("/{wishlistId}")
    public ResponseEntity<ApiResponse<String>> updateWishlist(@PathVariable String wishlistId, @RequestBody RestWishlist updatedWishlist) {
        try {
            String updateTime = wishlistService.updateWishlist(wishlistId, updatedWishlist);
            return ResponseEntity.ok(new ApiResponse<>(true, "Wishlist updated successfully", updateTime, null));
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false, "Error updating wishlist", null, e.getMessage()));
        }
    }

    @PostMapping("/addBook/{userId}/{bookId}")
    public ResponseEntity<ApiResponse<Boolean>> addBookToWishlist(@PathVariable String userId, @PathVariable String bookId) {
        try {
            boolean added = wishlistService.addBookToWishlist(userId, bookId);
            if (added) {
                return ResponseEntity.ok(new ApiResponse<>(true, "Book added to wishlist successfully", true, null));
            } else {
                return ResponseEntity.ok(new ApiResponse<>(false, "Book already in wishlist or wishlist not found", false, null));
            }
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false, "Error adding book to wishlist", null, e.getMessage()));
        }
    }

    @DeleteMapping("/removeBook/{userId}/{bookId}")
    public ResponseEntity<ApiResponse<Boolean>> removeBookFromWishlist(@PathVariable String userId, @PathVariable String bookId) {
        try {
            boolean removed = wishlistService.removeBookFromWishlist(userId, bookId);
            if (removed) {
                return ResponseEntity.ok(new ApiResponse<>(true, "Book removed from wishlist successfully", true, null));
            } else {
                return ResponseEntity.ok(new ApiResponse<>(false, "Book not found in wishlist or wishlist not found", false, null));
            }
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false, "Error removing book from wishlist", null, e.getMessage()));
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<RestWishlist>> getWishlistByUserId(@PathVariable String userId) {
        try {
            RestWishlist wishlist = wishlistService.getWishlistByUserId(userId);
            if (wishlist != null) {
                return ResponseEntity.ok(new ApiResponse<>(true, "Wishlist retrieved for user successfully", wishlist, null));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(false, "Wishlist not found for user", null, null));
            }
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false, "Error retrieving wishlist for user", null, e.getMessage()));
        }
    }

    @DeleteMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<String>> deleteWishlistByUserId(@PathVariable String userId) {
        try {
            boolean deleted = wishlistService.deleteWishlistByUserId(userId);
            if (deleted) {
                return ResponseEntity.ok(new ApiResponse<>(true, "Wishlist deleted for user successfully", null, null));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(false, "Wishlist not found for user", null, null));
            }
        } catch (ExecutionException | InterruptedException | TimeoutException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false, "Error deleting wishlist for user", null, e.getMessage()));
        }
    }

    @GetMapping("/user/{userId}/books")
    public ResponseEntity<ApiResponse<RestWishlist>> getWishlistWithBooksByUserId(@PathVariable String userId) {
        try {
            RestWishlist wishlistWithBooks = wishlistService.getWishlistWithBooksByUserId(userId);
            if (wishlistWithBooks != null) {
                return ResponseEntity.ok(new ApiResponse<>(true, "Wishlist with book details retrieved successfully", wishlistWithBooks, null));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(false, "Wishlist not found for user", null, null));
            }
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false, "Error retrieving wishlist with book details", null, e.getMessage()));
        }
    }


}