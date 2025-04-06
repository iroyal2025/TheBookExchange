package edu.famu.thebookexchange.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import edu.famu.thebookexchange.model.Rest.RestForums;
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
public class FeedbackService {

    private static final Logger logger = LoggerFactory.getLogger(FeedbackService.class);
    private Firestore firestore;

    private static final String FORUMS_COLLECTION = "Forums";
    private static final String FEEDBACK_COLLECTION = "Feedback"; // New collection for feedback
    private static final long FIRESTORE_TIMEOUT = 5; // Timeout in seconds

    public FeedbackService() {
        this.firestore = FirestoreClient.getFirestore();
    }

    public List<RestForums> getAllForums() throws InterruptedException, ExecutionException, TimeoutException {
        CollectionReference forumsCollection = firestore.collection(FORUMS_COLLECTION);
        ApiFuture<QuerySnapshot> querySnapshot = forumsCollection.get();
        List<QueryDocumentSnapshot> documents = querySnapshot.get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS).getDocuments();

        List<RestForums> forums = new ArrayList<>();

        for (QueryDocumentSnapshot document : documents) {
            if (document.exists()) {
                RestForums forum = new RestForums(
                        document.getString("topic"),
                        document.getTimestamp("createdAt") != null ? new Timestamp(document.getTimestamp("createdAt").toDate().getTime()) : null,
                        document.getReference()
                );
                forums.add(forum);
            }
        }
        return forums;
    }

    public String addForum(RestForums forum) throws InterruptedException, ExecutionException {
        logger.info("Adding forum with details: {}", forum);

        Map<String, Object> forumData = new HashMap<>();
        forumData.put("topic", forum.getTopic());
        forumData.put("createdAt", forum.getCreatedAt());

        ApiFuture<DocumentReference> writeResult = firestore.collection(FORUMS_COLLECTION).add(forumData);
        DocumentReference rs = writeResult.get();
        logger.info("Forum added with ID: {}", rs.getId());
        return rs.getId();
    }

    public boolean deleteForumByTopic(String topic) throws ExecutionException, InterruptedException, TimeoutException {
        try {
            Query query = firestore.collection(FORUMS_COLLECTION).whereEqualTo("topic", topic);
            ApiFuture<QuerySnapshot> querySnapshot = query.get();
            List<QueryDocumentSnapshot> documents = querySnapshot.get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS).getDocuments();

            if (!documents.isEmpty()) {
                for (QueryDocumentSnapshot document : documents) {
                    firestore.collection(FORUMS_COLLECTION).document(document.getId()).delete().get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS);
                    logger.info("Forum deleted successfully with ID: {} and topic: {}", document.getId(), topic);
                }
                return true;
            } else {
                logger.warn("Forum not found for deletion with topic: {}", topic);
                return false;
            }
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.error("Error deleting forum with topic: {}", topic, e);
            throw e;
        }
    }

    public String updateForum(String forumId, RestForums updatedForum) throws InterruptedException, ExecutionException, TimeoutException {
        DocumentReference forumRef = firestore.collection(FORUMS_COLLECTION).document(forumId);

        Map<String, Object> updatedForumData = new HashMap<>();
        updatedForumData.put("topic", updatedForum.getTopic());
        updatedForumData.put("createdAt", updatedForum.getCreatedAt());

        ApiFuture<WriteResult> writeResult = forumRef.update(updatedForumData);
        logger.info("Forum updated at: {}", writeResult.get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS).getUpdateTime().toString());

        return writeResult.get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS).getUpdateTime().toString();
    }

    // New methods for feedback

    public String addFeedback(String bookId, String feedback) throws InterruptedException, ExecutionException {
        logger.info("Adding feedback for book ID: {} with feedback: {}", bookId, feedback);

        Map<String, Object> feedbackData = new HashMap<>();
        feedbackData.put("bookId", bookId);
        feedbackData.put("feedback", feedback);
        feedbackData.put("createdAt", new Timestamp(System.currentTimeMillis())); // Add timestamp

        ApiFuture<DocumentReference> writeResult = firestore.collection(FEEDBACK_COLLECTION).add(feedbackData);
        DocumentReference rs = writeResult.get();
        logger.info("Feedback added with ID: {}", rs.getId());
        return rs.getId();
    }

    public List<Map<String, Object>> getFeedbackForBook(String bookId) throws InterruptedException, ExecutionException, TimeoutException {
        logger.info("Retrieving feedback for book ID: {}", bookId);

        CollectionReference feedbackCollection = firestore.collection(FEEDBACK_COLLECTION);
        Query query = feedbackCollection.whereEqualTo("bookId", bookId);
        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        List<QueryDocumentSnapshot> documents = querySnapshot.get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS).getDocuments();

        List<Map<String, Object>> feedbackList = new ArrayList<>();
        for (QueryDocumentSnapshot document : documents) {
            if (document.exists()) {
                Map<String, Object> feedbackData = new HashMap<>();
                feedbackData.put("feedbackId", document.getId());
                feedbackData.put("feedback", document.getString("feedback"));
                feedbackData.put("createdAt", document.getTimestamp("createdAt") != null ? new Timestamp(document.getTimestamp("createdAt").toDate().getTime()) : null);
                feedbackList.add(feedbackData);
            }
        }
        return feedbackList;
    }

    public boolean deleteFeedback(String feedbackId) throws ExecutionException, InterruptedException, TimeoutException {
        try {
            firestore.collection(FEEDBACK_COLLECTION).document(feedbackId).delete().get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS);
            logger.info("Feedback deleted successfully with ID: {}", feedbackId);
            return true;
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.error("Error deleting feedback with ID: {}", feedbackId, e);
            throw e;
        }
    }

    public String updateFeedback(String feedbackId, String updatedFeedback) throws InterruptedException, ExecutionException, TimeoutException {
        DocumentReference feedbackRef = firestore.collection(FEEDBACK_COLLECTION).document(feedbackId);

        Map<String, Object> updatedFeedbackData = new HashMap<>();
        updatedFeedbackData.put("feedback", updatedFeedback);
        updatedFeedbackData.put("createdAt", new Timestamp(System.currentTimeMillis()));

        ApiFuture<WriteResult> writeResult = feedbackRef.update(updatedFeedbackData);
        logger.info("Feedback updated at: {}", writeResult.get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS).getUpdateTime().toString());

        return writeResult.get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS).getUpdateTime().toString();
    }
}