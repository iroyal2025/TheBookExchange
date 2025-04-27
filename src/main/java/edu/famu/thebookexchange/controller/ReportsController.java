package edu.famu.thebookexchange.controller;

import edu.famu.thebookexchange.model.Rest.RestReports;
import edu.famu.thebookexchange.service.ReportsService;
import edu.famu.thebookexchange.util.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.lang.InterruptedException;

@RestController
@RequestMapping("/Reports")
public class ReportsController {

    @Autowired
    private ReportsService reportsService;

    @GetMapping("/")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getAllReports() {
        try {
            List<Map<String, Object>> formattedReports = reportsService.getAllReportsFormatted();

            if (!formattedReports.isEmpty()) {
                return ResponseEntity.ok(new ApiResponse<>(true, "Formatted Reports List", formattedReports, null));
            } else {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body(new ApiResponse<>(true, "No formatted reports found", null, null));
            }
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false, "Error retrieving formatted reports", null, e.getMessage()));
        }
    }

    @PostMapping("/add")
    public ResponseEntity<ApiResponse<String>> addReport(@RequestBody RestReports report) {
        try {
            String reportId = reportsService.addReport(report);
            return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(true, "Report created", reportId, null));
        } catch (InterruptedException | ExecutionException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false, "Error creating report", null, e.getMessage()));
        }
    }

    @PostMapping("/add/seller")
    public ResponseEntity<ApiResponse<String>> addSellerReport(@RequestBody Map<String, String> reportData) {
        String reportedBy = reportData.get("reportedBy");
        String sellerEmail = reportData.get("sellerEmail");
        String content = reportData.get("content");

        if (reportedBy == null || reportedBy.trim().isEmpty() || sellerEmail == null || sellerEmail.trim().isEmpty() || content == null || content.trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse<>(false, "Missing required fields for seller report", null, null));
        }

        RestReports sellerReport = new RestReports();
        sellerReport.setReportedBy(reportedBy);
        sellerReport.setSellerEmail(sellerEmail);
        sellerReport.setContent(content);
        sellerReport.setReportType("seller");

        try {
            String reportId = reportsService.addReport(sellerReport);
            return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(true, "Seller report created", reportId, null));
        } catch (InterruptedException | ExecutionException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false, "Error creating seller report", null, e.getMessage()));
        }
    }

    @PostMapping("/add/book")
    public ResponseEntity<ApiResponse<String>> addBookReport(@RequestBody Map<String, String> reportData) {
        String reportedBy = reportData.get("reportedBy");
        String userEmail = reportData.get("userEmail");
        String bookTitle = reportData.get("bookTitle");
        String content = reportData.get("content");

        if (reportedBy == null || reportedBy.trim().isEmpty() || userEmail == null || userEmail.trim().isEmpty() || bookTitle == null || bookTitle.trim().isEmpty() || content == null || content.trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse<>(false, "Missing required fields for book report", null, null));
        }

        RestReports bookReport = new RestReports();
        bookReport.setReportedBy(reportedBy);
        bookReport.setUserEmail(userEmail);
        bookReport.setBookTitle(bookTitle);
        bookReport.setContent(content);
        bookReport.setReportType("book");

        try {
            String reportId = reportsService.addReport(bookReport);
            return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(true, "Book report created", reportId, null));
        } catch (InterruptedException | ExecutionException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false, "Error creating book report", null, e.getMessage()));
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<ApiResponse<String>> deleteReportByContent(@RequestParam String content) {
        try {
            boolean deleted = reportsService.deleteReportByContent(content);

            if (deleted) {
                return ResponseEntity.ok(new ApiResponse<>(true, "Report deleted successfully", null, null));
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false, "Failed to delete report", null, null));
            }
        } catch (ExecutionException | InterruptedException | TimeoutException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false, "Error deleting report", null, e.getMessage()));
        }
    }

    @PutMapping("/{reportId}")
    public ResponseEntity<ApiResponse<String>> updateReport(@PathVariable String reportId, @RequestBody RestReports updatedReport) {
        try {
            String updateTime = reportsService.updateReport(reportId, updatedReport);
            return ResponseEntity.ok(new ApiResponse<>(true, "Report updated", updateTime, null));
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false, "Error updating report", null, e.getMessage()));
        }
    }
}