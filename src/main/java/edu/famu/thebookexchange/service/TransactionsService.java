package edu.famu.thebookexchange.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import edu.famu.thebookexchange.model.Rest.RestPurchasedBook;
import edu.famu.thebookexchange.model.Rest.RestTransactions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static edu.famu.thebookexchange.service.BooksService.BOOKS_COLLECTION;
import static edu.famu.thebookexchange.service.NotificationService.NOTIFICATIONS_COLLECTION;

@Service
public class TransactionsService {

    private static final Logger logger = LoggerFactory.getLogger(TransactionsService.class);
    private static Firestore firestore;  // Make this static

    public static final String TRANSACTIONS_COLLECTION = "Transactions";
    private static final long FIRESTORE_TIMEOUT = 5; // Timeout in seconds

    // Static initializer block to initialize the firestore variable
    static {
        firestore = FirestoreClient.getFirestore();  // Initialize static field
    }

    public TransactionsService() {
        // No need to initialize firestore here anymore, as it's static
    }

    public List<RestTransactions> getAllTransactions() throws InterruptedException, ExecutionException, TimeoutException {
        CollectionReference transactionsCollection = firestore.collection(TRANSACTIONS_COLLECTION);
        ApiFuture<QuerySnapshot> querySnapshot = transactionsCollection.get();
        List<QueryDocumentSnapshot> documents = querySnapshot.get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS).getDocuments();

        List<RestTransactions> transactions = new ArrayList<>();
        for (QueryDocumentSnapshot document : documents) {
            if (document.exists()) {
                transactions.add(documentSnapshotToRestTransaction(document));
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

    public List<RestPurchasedBook> getPurchasedBooksDetails(String userId) throws InterruptedException, ExecutionException, TimeoutException {
        logger.info("Fetching purchased book details for user: {}", userId);
        List<RestPurchasedBook> purchasedBooksDetails = new ArrayList<>();

        ApiFuture<QuerySnapshot> querySnapshot = firestore.collection(TRANSACTIONS_COLLECTION)
                .whereEqualTo("userId", userId)
                .get();

        for (QueryDocumentSnapshot transactionDoc : querySnapshot.get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS).getDocuments()) {
            String bookId = transactionDoc.getString("bookId");
            if (bookId != null) {
                ApiFuture<DocumentSnapshot> bookFuture = firestore.collection(BOOKS_COLLECTION).document(bookId).get();
                DocumentSnapshot bookSnapshot = bookFuture.get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS);
                if (bookSnapshot.exists()) {
                    purchasedBooksDetails.add(documentSnapshotToRestPurchasedBook(bookSnapshot));
                } else {
                    logger.warn("Book not found with ID: {}", bookId);
                }
            }
        }
        return purchasedBooksDetails;
    }

    public static List<Map<String, Object>> getTransactionsByStudentEmail(String email) throws InterruptedException, ExecutionException, TimeoutException {
        // Find user by email
        QuerySnapshot userQuery = firestore.collection("Users")
                .whereEqualTo("email", email)
                .get()
                .get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS);

        if (userQuery.isEmpty()) {
            logger.warn("No user found with email: {}", email);
            return Collections.emptyList();
        }

        String userId = userQuery.getDocuments().get(0).getId();

        // Find transactions by userId
        QuerySnapshot transactionQuery = firestore.collection("Transactions")
                .whereEqualTo("userId", userId)
                .get()
                .get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS);

        List<Map<String, Object>> transactions = new ArrayList<>();

        for (DocumentSnapshot transactionDoc : transactionQuery.getDocuments()) {
            Map<String, Object> transactionData = transactionDoc.getData();
            if (transactionData != null && transactionData.containsKey("bookId")) {
                String bookId = (String) transactionData.get("bookId");

                // Lookup the book details
                DocumentSnapshot bookSnapshot = firestore.collection(BOOKS_COLLECTION)
                        .document(bookId)
                        .get()
                        .get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS);

                if (bookSnapshot.exists()) {
                    Map<String, Object> bookData = bookSnapshot.getData();
                    if (bookData != null) {
                        transactionData.put("bookDetails", bookData);
                    }
                } else {
                    logger.warn("Book not found for ID: {}", bookId);
                }
            }
            transactions.add(transactionData);
        }

        logger.info("Found {} transactions for userId {}", transactions.size(), userId);
        return transactions;
    }

    private RestPurchasedBook documentSnapshotToRestPurchasedBook(DocumentSnapshot document) {
        return new RestPurchasedBook(
                document.getString("title"),
                document.getString("author"),
                document.getDouble("price") != null ? document.getDouble("price") : 0.0
        );
    }

    private RestTransactions documentSnapshotToRestTransaction(DocumentSnapshot document) {
        return new RestTransactions(
                document.getString("order status"),
                document.getString("bookId"),
                document.getString("userId")
        );
    }
}
