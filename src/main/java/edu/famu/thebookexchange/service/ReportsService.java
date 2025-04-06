package edu.famu.thebookexchange.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import edu.famu.thebookexchange.model.Rest.RestReports;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
public class ReportsService {

    private static final Logger logger = LoggerFactory.getLogger(ReportsService.class);
    private Firestore firestore;

    private static final String REPORTS_COLLECTION = "Reports"; // Changed collection name
    private static final long FIRESTORE_TIMEOUT = 5; // Timeout in seconds

    public ReportsService() {
        this.firestore = FirestoreClient.getFirestore();
    }

    public List<RestReports> getAllReports() throws InterruptedException, ExecutionException, TimeoutException { // Changed method name
        CollectionReference reportsCollection = firestore.collection(REPORTS_COLLECTION); // Changed collection reference
        ApiFuture<QuerySnapshot> querySnapshot = reportsCollection.get();
        List<QueryDocumentSnapshot> documents = querySnapshot.get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS).getDocuments();

        List<RestReports> reports = new ArrayList<>();

        for (QueryDocumentSnapshot document : documents) {
            if (document.exists()) {
                RestReports report = new RestReports(
                        document.getString("content"),
                        document.getTimestamp("createdAt") != null ? new Timestamp(document.getTimestamp("createdAt").toDate().getTime()) : null,
                        document.getString("bookTitle"), // Changed to bookTitle
                        document.getString("userEmail")  // Changed to userEmail
                );
                reports.add(report);
            }
        }
        return reports;
    }

    public String addReport(RestReports report) throws InterruptedException, ExecutionException {
        logger.info("Adding report with details: {}", report);

        Map<String, Object> reportData = new HashMap<>();
        reportData.put("content", report.getContent());
        // Set createdAt to the current timestamp
        reportData.put("createdAt", FieldValue.serverTimestamp()); // Use Firestore serverTimestamp()
        reportData.put("bookTitle", report.getBookTitle());
        reportData.put("userEmail", report.getUserEmail());

        ApiFuture<DocumentReference> writeResult = firestore.collection(REPORTS_COLLECTION).add(reportData);
        DocumentReference rs = writeResult.get();
        logger.info("Report added with ID: {}", rs.getId());
        return rs.getId();
    }

    public boolean deleteReportByContent(String content) throws ExecutionException, InterruptedException, TimeoutException { // changed method name and content.
        try {
            Query query = firestore.collection(REPORTS_COLLECTION).whereEqualTo("content", content); // Changed collection reference
            ApiFuture<QuerySnapshot> querySnapshot = query.get();
            List<QueryDocumentSnapshot> documents = querySnapshot.get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS).getDocuments();

            if (!documents.isEmpty()) {
                for (QueryDocumentSnapshot document : documents) {
                    firestore.collection(REPORTS_COLLECTION).document(document.getId()).delete().get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS); // Changed collection reference
                    logger.info("Report deleted successfully with ID: {} and content: {}", document.getId(), content);
                }
                return true;
            } else {
                logger.warn("Report not found for deletion with content: {}", content);
                return false;
            }
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.error("Error deleting report with content: {}", content, e);
            throw e;
        }
    }

    public String updateReport(String reportId, RestReports updatedReport) throws InterruptedException, ExecutionException, TimeoutException { // changed Method name.
        DocumentReference reportRef = firestore.collection(REPORTS_COLLECTION).document(reportId); // Changed collection reference

        Map<String, Object> updatedReportData = new HashMap<>();
        updatedReportData.put("content", updatedReport.getContent());
        updatedReportData.put("createdAt", updatedReport.getCreatedAt());
        updatedReportData.put("bookTitle", updatedReport.getBookTitle()); // Changed to bookTitle
        updatedReportData.put("userEmail", updatedReport.getUserEmail()); // Changed to userEmail

        ApiFuture<WriteResult> writeResult = reportRef.update(updatedReportData);
        logger.info("Report updated at: {}", writeResult.get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS).getUpdateTime().toString());

        return writeResult.get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS).getUpdateTime().toString();
    }
}