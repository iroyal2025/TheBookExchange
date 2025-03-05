package edu.famu.thebookexchange.controller;

import edu.famu.thebookexchange.model.Rest.RestWishlist;
import edu.famu.thebookexchange.service.WishlistService;
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
@RequestMapping("/Wishlist")
public class WishlistController {

    @Autowired
    private WishlistService wishlistService;

    @GetMapping("/")
    public ResponseEntity<ApiResponse<List<RestWishlist>>> getAllWishlists() {
        try {
            List<RestWishlist> restWishlists = wishlistService.getAllWishlists();

            if (!restWishlists.isEmpty()) {
                return ResponseEntity.ok(new ApiResponse<>(true, "Wishlists List", restWishlists, null));
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
            return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(true, "Wishlist created", wishlistId, null));
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
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false, "Failed to delete wishlist", null, null));
            }
        } catch (ExecutionException | InterruptedException | TimeoutException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false, "Error deleting wishlist", null, e.getMessage()));
        }
    }

    @PutMapping("/{wishlistId}")
    public ResponseEntity<ApiResponse<String>> updateWishlist(@PathVariable String wishlistId, @RequestBody RestWishlist updatedWishlist) {
        try {
            String updateTime = wishlistService.updateWishlist(wishlistId, updatedWishlist);
            return ResponseEntity.ok(new ApiResponse<>(true, "Wishlist updated", updateTime, null));
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false, "Error updating wishlist", null, e.getMessage()));
        }
    }
}