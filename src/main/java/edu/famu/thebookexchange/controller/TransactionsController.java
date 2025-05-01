package edu.famu.thebookexchange.controller;

import edu.famu.thebookexchange.model.Rest.RestPurchasedBook;
import edu.famu.thebookexchange.model.Rest.RestTransactions;
import edu.famu.thebookexchange.service.TransactionsService;
import edu.famu.thebookexchange.util.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

@RestController
@RequestMapping("/Transactions")
public class TransactionsController {

    private static final Logger logger = LoggerFactory.getLogger(TransactionsController.class);

    @Autowired
    private TransactionsService transactionService;

    @GetMapping("/")
    public ResponseEntity<ApiResponse<List<RestTransactions>>> getAllTransactions() {
        try {
            List<RestTransactions> restTransactions = transactionService.getAllTransactions();
            if (!restTransactions.isEmpty()) {
                return ResponseEntity.ok(new ApiResponse<>(true, "Transactions List", restTransactions, null));
            } else {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body(new ApiResponse<>(true, "No transactions found", null, null));
            }
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false, "Error retrieving transactions", null, e.getMessage()));
        }
    }

    @PostMapping("/add")
    public ResponseEntity<ApiResponse<String>> addTransaction(@RequestBody RestTransactions transaction) {
        try {
            String transactionId = transactionService.addTransaction(transaction);
            return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(true, "Transaction created", transactionId, null));
        } catch (InterruptedException | ExecutionException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false, "Error creating transaction", null, e.getMessage()));
        }
    }

    @DeleteMapping("/{transactionId}")
    public ResponseEntity<ApiResponse<String>> deleteTransactionById(@PathVariable String transactionId) {
        try {
            boolean deleted = transactionService.deleteTransactionById(transactionId);
            if (deleted) {
                return ResponseEntity.ok(new ApiResponse<>(true, "Transaction deleted successfully", null, null));
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false, "Failed to delete transaction", null, null));
            }
        } catch (ExecutionException | InterruptedException | TimeoutException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false, "Error deleting transaction", null, e.getMessage()));
        }
    }

    @PutMapping("/{transactionId}")
    public ResponseEntity<ApiResponse<String>> updateTransaction(@PathVariable String transactionId, @RequestBody RestTransactions updatedTransaction) {
        try {
            String updateTime = transactionService.updateTransaction(transactionId, updatedTransaction);
            return ResponseEntity.ok(new ApiResponse<>(true, "Transaction updated", updateTime, null));
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false, "Error updating transaction", null, e.getMessage()));
        }
    }

    @GetMapping("/purchased/{userId}")
    public ResponseEntity<ApiResponse<List<RestPurchasedBook>>> getPurchasedBooksByUser(@PathVariable String userId) {
        try {
            List<RestPurchasedBook> purchasedBooks = transactionService.getPurchasedBooksDetails(userId);
            if (!purchasedBooks.isEmpty()) {
                return ResponseEntity.ok(new ApiResponse<>(true, "Purchased Books Details", purchasedBooks, null));
            } else {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body(new ApiResponse<>(true, "No purchased books found for this user", null, null));
            }
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false, "Error retrieving purchased books details", null, e.getMessage()));
        }
    }



    @GetMapping("/student/email/{email}")
    public ResponseEntity<?> getTransactionsByStudentEmail(@PathVariable String email) {
        try {
            List<Map<String, Object>> transactions = TransactionsService.getTransactionsByStudentEmail(email);
            if (transactions.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No transactions found for this email.");
            }
            return ResponseEntity.ok(transactions);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error fetching transactions: " + e.getMessage());
        }
    }
}

