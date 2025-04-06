package edu.famu.thebookexchange.controller;

import edu.famu.thebookexchange.model.Rest.RestReports;
import edu.famu.thebookexchange.service.ReportsService;
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
@RequestMapping("/Reports") // Changed endpoint to /Reports
public class ReportsController {

    @Autowired
    private ReportsService postService;

    @GetMapping("/")
    public ResponseEntity<ApiResponse<List<RestReports>>> getAllReports() { // Changed method name
        try {
            List<RestReports> restPosts = postService.getAllReports(); // Changed method call

            if (!restPosts.isEmpty()) {
                return ResponseEntity.ok(new ApiResponse<>(true, "Reports List", restPosts, null)); // Changed message
            } else {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body(new ApiResponse<>(true, "No reports found", null, null)); // Changed message
            }
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false, "Error retrieving reports", null, e.getMessage())); // Changed message
        }
    }

    @PostMapping("/add")
    public ResponseEntity<ApiResponse<String>> addReport(@RequestBody RestReports report) { // Changed method name and parameter name
        try {
            String reportId = postService.addReport(report); // Changed method call
            return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(true, "Report created", reportId, null)); // Changed message
        } catch (InterruptedException | ExecutionException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false, "Error creating report", null, e.getMessage())); // Changed message
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<ApiResponse<String>> deleteReportByContent(@RequestParam String content) { // Changed method name
        try {
            boolean deleted = postService.deleteReportByContent(content); // Changed method call

            if (deleted) {
                return ResponseEntity.ok(new ApiResponse<>(true, "Report deleted successfully", null, null)); // Changed message
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false, "Failed to delete report", null, null)); // Changed message
            }
        } catch (ExecutionException | InterruptedException | TimeoutException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false, "Error deleting report", null, e.getMessage())); // Changed message
        }
    }

    @PutMapping("/{reportId}") // Changed path variable name
    public ResponseEntity<ApiResponse<String>> updateReport(@PathVariable String reportId, @RequestBody RestReports updatedReport) { // Changed method name and parameter names
        try {
            String updateTime = postService.updateReport(reportId, updatedReport); // Changed method call
            return ResponseEntity.ok(new ApiResponse<>(true, "Report updated", updateTime, null)); // Changed message
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false, "Error updating report", null, e.getMessage())); // Changed message
        }
    }
}