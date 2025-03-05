package edu.famu.thebookexchange.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import edu.famu.thebookexchange.model.Rest.RestMessages;
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
public class MessagesService {

    private static final Logger logger = LoggerFactory.getLogger(MessagesService.class);
    private Firestore firestore;

    private static final String MESSAGES_COLLECTION = "Messages";
    private static final long FIRESTORE_TIMEOUT = 5; // Timeout in seconds

    public MessagesService() {
        this.firestore = FirestoreClient.getFirestore();
    }

    public List<RestMessages> getAllMessages() throws InterruptedException, ExecutionException, TimeoutException {
        CollectionReference messagesCollection = firestore.collection(MESSAGES_COLLECTION);
        ApiFuture<QuerySnapshot> querySnapshot = messagesCollection.get();
        List<QueryDocumentSnapshot> documents = querySnapshot.get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS).getDocuments();

        List<RestMessages> messages = new ArrayList<>();

        for (QueryDocumentSnapshot document : documents) {
            if (document.exists()) {
                RestMessages message = new RestMessages(
                        document.getString("context"),
                        document.getTimestamp("timestamp") != null ? new Timestamp(document.getTimestamp("timestamp").toDate().getTime()) : null,
                        document.getReference()
                );
                messages.add(message);
            }
        }
        return messages;
    }

    public String addMessage(RestMessages message) throws InterruptedException, ExecutionException {
        logger.info("Adding message with details: {}", message);

        Map<String, Object> messageData = new HashMap<>();
        messageData.put("context", message.getContent());
        messageData.put("timestamp", message.getTimestamp());

        ApiFuture<DocumentReference> writeResult = firestore.collection(MESSAGES_COLLECTION).add(messageData);
        DocumentReference rs = writeResult.get();
        logger.info("Message added with ID: {}", rs.getId());
        return rs.getId();
    }

    public boolean deleteMessageByContent(String context) throws ExecutionException, InterruptedException, TimeoutException {
        try {
            Query query = firestore.collection(MESSAGES_COLLECTION).whereEqualTo("context", context);
            ApiFuture<QuerySnapshot> querySnapshot = query.get();
            List<QueryDocumentSnapshot> documents = querySnapshot.get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS).getDocuments();

            if (!documents.isEmpty()) {
                for (QueryDocumentSnapshot document : documents) {
                    firestore.collection(MESSAGES_COLLECTION).document(document.getId()).delete().get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS);
                    logger.info("Message deleted successfully with ID: {} and content: {}", document.getId(), context);
                }
                return true;
            } else {
                logger.warn("Message not found for deletion with content: {}", context);
                return false;
            }
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.error("Error deleting message with content: {}", context, e);
            throw e;
        }
    }

    public String updateMessage(String messageId, RestMessages updatedMessage) throws InterruptedException, ExecutionException, TimeoutException {
        DocumentReference messageRef = firestore.collection(MESSAGES_COLLECTION).document(messageId);

        Map<String, Object> updatedMessageData = new HashMap<>();
        updatedMessageData.put("context", updatedMessage.getContent());
        updatedMessageData.put("timestamp", updatedMessage.getTimestamp());

        ApiFuture<WriteResult> writeResult = messageRef.update(updatedMessageData);
        logger.info("Message updated at: {}", writeResult.get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS).getUpdateTime().toString());

        return writeResult.get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS).getUpdateTime().toString();
    }
}