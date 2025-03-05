// Service Class (TransactionsService.java)
package edu.famu.thebookexchange.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import edu.famu.thebookexchange.model.Rest.RestTransactions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
public class TransactionsService {

    private static final Logger logger = LoggerFactory.getLogger(TransactionsService.class);
    private Firestore firestore;

    private static final String TRANSACTIONS_COLLECTION = "Transactions";
    private static final long FIRESTORE_TIMEOUT = 5; // Timeout in seconds

    public TransactionsService() {
        this.firestore = FirestoreClient.getFirestore();
    }

    public List<RestTransactions> getAllTransactions() throws InterruptedException, ExecutionException, TimeoutException {
        CollectionReference transactionsCollection = firestore.collection(TRANSACTIONS_COLLECTION);
        ApiFuture<QuerySnapshot> querySnapshot = transactionsCollection.get();
        List<QueryDocumentSnapshot> documents = querySnapshot.get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS).getDocuments();

        List<RestTransactions> transactions = new ArrayList<>();

        for (QueryDocumentSnapshot document : documents) {
            if (document.exists()) {
                RestTransactions transaction = new RestTransactions(
                        document.getString("order status"),
                        document.get("bookId", DocumentReference.class),
                        document.get("userId", DocumentReference.class)
                );
                transactions.add(transaction);
            }
        }
        return transactions;
    }

    public String addTransaction(RestTransactions transaction) throws InterruptedException, ExecutionException {
        logger.info("Adding transaction with details: {}", transaction);

        Map<String, Object> transactionData = new HashMap<>();
        transactionData.put("order status", transaction.getOrderStatus());
        transactionData.put("bookId", transaction.getBookId());
        transactionData.put("userId", transaction.getUserId());

        ApiFuture<DocumentReference> writeResult = firestore.collection(TRANSACTIONS_COLLECTION).add(transactionData);
        DocumentReference rs = writeResult.get();
        logger.info("Transaction added with ID: {}", rs.getId());
        return rs.getId();
    }

    public boolean deleteTransactionById(String transactionId) throws ExecutionException, InterruptedException, TimeoutException {
        try {
            DocumentReference transactionRef = firestore.collection(TRANSACTIONS_COLLECTION).document(transactionId);
            ApiFuture<DocumentSnapshot> future = transactionRef.get();
            DocumentSnapshot document = future.get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS);

            if (document.exists()) {
                transactionRef.delete().get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS);
                logger.info("Transaction deleted successfully with ID: {}", transactionId);
                return true;
            } else {
                logger.warn("Transaction not found for deletion with ID: {}", transactionId);
                return false;
            }
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.error("Error deleting transaction with ID: {}", transactionId, e);
            throw e;
        }
    }

    public String updateTransaction(String transactionId, RestTransactions updatedTransaction) throws InterruptedException, ExecutionException, TimeoutException {
        DocumentReference transactionRef = firestore.collection(TRANSACTIONS_COLLECTION).document(transactionId);

        Map<String, Object> updatedTransactionData = new HashMap<>();
        updatedTransactionData.put("order status", updatedTransaction.getOrderStatus());
        updatedTransactionData.put("bookId", updatedTransaction.getBookId());
        updatedTransactionData.put("userId", updatedTransaction.getUserId());

        ApiFuture<WriteResult> writeResult = transactionRef.update(updatedTransactionData);
        logger.info("Transaction updated at: {}", writeResult.get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS).getUpdateTime().toString());

        return writeResult.get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS).getUpdateTime().toString();
    }
}