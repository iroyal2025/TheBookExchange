package edu.famu.thebookexchange.controller;

import edu.famu.thebookexchange.model.Rest.RestTransactions;
import edu.famu.thebookexchange.service.TransactionsService;
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
@RequestMapping("/Transactions")
public class TransactionsController {

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
}