package edu.famu.thebookexchange.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import edu.famu.thebookexchange.model.Rest.RestBooks;
import edu.famu.thebookexchange.model.Rest.RestUsers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.print.Book;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import static edu.famu.thebookexchange.service.TransactionsService.TRANSACTIONS_COLLECTION;


@Service
public class BooksService {

    private static final Logger logger = LoggerFactory.getLogger(BooksService.class);
    private Firestore firestore;

    public static final String BOOKS_COLLECTION = "Books";
    private static final String USERS_COLLECTION = "Users";
    private static final String COURSES_COLLECTION = "Courses";
    private static final long FIRESTORE_TIMEOUT = 5;

    @Autowired
    private NotificationService notificationService; // Inject NotificationService

    public BooksService() {
        this.firestore = FirestoreClient.getFirestore();
    }

    public List<Map<String, Object>> getAllBooksWithSellerRating() throws InterruptedException, ExecutionException, TimeoutException {
        CollectionReference booksCollection = firestore.collection(BOOKS_COLLECTION);
        ApiFuture<QuerySnapshot> querySnapshot = booksCollection.get();
        List<QueryDocumentSnapshot> bookDocuments = querySnapshot.get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS).getDocuments();
        List<Map<String, Object>> booksWithSellerRating = new ArrayList<>();

        for (QueryDocumentSnapshot bookDoc : bookDocuments) {
            Map<String, Object> bookData = bookDoc.getData();
            String sellerId = bookDoc.getString("userId"); // Assuming userId in Books collection is the seller's DOCUMENT ID

            if (sellerId != null) {
                DocumentReference userRef = firestore.collection(USERS_COLLECTION).document(sellerId);
                ApiFuture<DocumentSnapshot> userSnapshotFuture = userRef.get();
                DocumentSnapshot userDoc = userSnapshotFuture.get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS);

                if (userDoc.exists()) {
                    bookData.put("sellerRating", userDoc.getDouble("sellerRating"));
                    bookData.put("sellerRatingCount", userDoc.getLong("sellerRatingCount"));
                    // If you need the email later, you can access it with userDoc.getString("email")
                } else {
                    bookData.put("sellerRating", null);
                    bookData.put("sellerRatingCount", 0L);
                }
            } else {
                bookData.put("sellerRating", null);
                bookData.put("sellerRatingCount", 0L);
            }
            bookData.put("bookId", bookDoc.getId()); // Add bookId
            booksWithSellerRating.add(bookData);
        }
        return booksWithSellerRating;
    }

    public List<RestBooks> getAllBooks() throws InterruptedException, ExecutionException, TimeoutException {
        CollectionReference booksCollection = firestore.collection(BOOKS_COLLECTION);
        ApiFuture<QuerySnapshot> querySnapshot = booksCollection.get();
        List<QueryDocumentSnapshot> documents = querySnapshot.get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS).getDocuments();

        List<RestBooks> books = new ArrayList<>();

        for (QueryDocumentSnapshot document : documents) {
            if (document.exists()) {
                books.add(documentSnapshotToRestBooks(document));
            }
        }
        return books;
    }


    public String addBook(RestBooks book) throws InterruptedException, ExecutionException, TimeoutException {
        logger.info("Adding book with details: {}", book);

        Map<String, Object> bookData = new HashMap<>();
        bookData.put("title", book.getTitle());
        bookData.put("author", book.getAuthor());
        bookData.put("edition", book.getEdition());
        bookData.put("ISBN", book.getISBN());
        bookData.put("condition", book.getCondition());
        bookData.put("description", book.getDescription());
        bookData.put("price", book.getPrice());
        bookData.put("isDigital", book.isDigital());
        bookData.put("digitalCopyPath", book.getDigitalCopyPath());
        bookData.put("ownedBy", book.getOwnedBy() != null ? book.getOwnedBy() : new ArrayList<>());
        bookData.put("addedBy", book.getUserId()); // Storing seller's email in 'addedBy'
        bookData.put("courseId", book.getCourseId());
        bookData.put("rating", 0.0);
        bookData.put("ratingCount", 0L);
        // We will set userId to the seller's document ID later
        String sellerId = null;

        logger.debug("Attempting to add book to Firestore...");
        ApiFuture<DocumentReference> writeResult = firestore.collection(BOOKS_COLLECTION).add(bookData);
        DocumentReference bookRef = null;
        String bookId = null;
        try {
            bookRef = writeResult.get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS);
            bookId = bookRef.getId();
            logger.info("Book added to Firestore with ID: {}", bookId);

            String sellerEmail = book.getUserId();
            if (sellerEmail != null && !sellerEmail.isEmpty()) {
                logger.debug("Attempting to find user document ID for seller: {}", sellerEmail);
                QuerySnapshot userQuerySnapshot = firestore.collection(USERS_COLLECTION)
                        .whereEqualTo("email", sellerEmail)
                        .get()
                        .get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS);

                if (!userQuerySnapshot.isEmpty()) {
                    sellerId = userQuerySnapshot.getDocuments().get(0).getId();
                    // Now set the userId in the Books document to the seller's document ID
                    bookRef.update("userId", sellerId).get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS);
                    logger.debug("Book ID {} updated with seller ID: {}", bookId, sellerId);

                    // Update the seller's listings using the seller's document ID
                    DocumentReference userRef = firestore.collection(USERS_COLLECTION).document(sellerId);
                    ApiFuture<WriteResult> updateFuture = userRef.update("listings", FieldValue.arrayUnion(bookId));
                    updateFuture.get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS);
                    logger.debug("Book ID {} added to listings for seller ID: {}", bookId, sellerId);

                    // --- Generate notification for ALL students ---
                    List<String> allStudentIds = getAllStudentIds();
                    logger.info("Found {} student users to notify for book ID: {}", allStudentIds.size(), bookId);

                    if (sellerEmail != null) {
                        try {
                            notificationService.notifyStudentNewBookAdded(allStudentIds, book.getTitle(), bookId, sellerEmail);
                            logger.info("'new_book_added' notification generated for {} students for book ID: {}", allStudentIds.size(), bookId);
                        } catch (InterruptedException | ExecutionException e) {
                            logger.error("Error sending 'new_book_added' notifications for book ID {}: {}", bookId, e.getMessage(), e);
                        }
                    }

                } else {
                    logger.warn("User document not found for email: {}, cannot update listings or set seller ID in Book.", sellerEmail);
                }
            } else {
                logger.warn("Seller email is null or empty, cannot update listings or set seller ID in Book.");
            }
            return bookId;

        } catch (TimeoutException e) {
            logger.error("Timeout occurred while adding book: {}", e.getMessage(), e);
            throw e;
        } catch (ExecutionException | InterruptedException e) {
            logger.error("Error occurred while adding book: {}", e.getMessage(), e);
            throw e;
        }
    }

    private List<String> getAllStudentIds() throws InterruptedException, ExecutionException, TimeoutException {
        List<String> studentIds = new ArrayList<>();
        logger.debug("Fetching all student user IDs...");
        QuerySnapshot userQuerySnapshot = firestore.collection(USERS_COLLECTION)
                .whereEqualTo("role", "student")
                .get()
                .get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS);

        for (QueryDocumentSnapshot doc : userQuerySnapshot.getDocuments()) {
            studentIds.add(doc.getId());
            logger.debug("Found student user ID: {}", doc.getId());
        }
        logger.info("Retrieved {} student user IDs.", studentIds.size());
        return studentIds;
    }

    public boolean deleteBook(String bookId) throws ExecutionException, InterruptedException, TimeoutException {
        logger.info("Deleting book with bookId: {}", bookId);
        String bookTitle = null; // To store the title for the notification
        try {
            // Get the book to find the seller and the title for the notification
            DocumentSnapshot bookSnapshot = firestore.collection(BOOKS_COLLECTION).document(bookId).get().get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS);
            if (bookSnapshot.exists()) {
                String sellerEmail = bookSnapshot.getString("userId");
                bookTitle = bookSnapshot.getString("title");

                // Remove the bookId from the seller's listings
                if (sellerEmail != null) {
                    DocumentReference userRef = firestore.collection(USERS_COLLECTION).document(sellerEmail);
                    ApiFuture<DocumentSnapshot> userSnapshotFuture = userRef.get();
                    DocumentSnapshot userSnapshot = userSnapshotFuture.get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS);
                    if (userSnapshot.exists()) {
                        userRef.update("listings", FieldValue.arrayRemove(bookId)).get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS);
                        logger.debug("Book ID {} removed from listings of seller: {}", bookId, sellerEmail);
                    } else {
                        logger.warn("User with email {} not found, cannot remove book ID {} from listings.", sellerEmail, bookId);
                        // Consider if you still want to delete the book itself in this case
                    }
                }
            } else {
                logger.warn("Book with ID {} not found.", bookId);
                return false; // Book not found, deletion failed
            }

            // Delete the book itself
            ApiFuture<WriteResult> writeResult = firestore.collection(BOOKS_COLLECTION).document(bookId).delete();
            writeResult.get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS);
            logger.info("Book deleted successfully with ID: {}", bookId);

            // --- Generate notification for ALL students about the deletion ---
            if (bookTitle != null) {
                List<String> allStudentIds = getAllStudentIds();
                logger.info("Notifying {} students about the deletion of book: {}", allStudentIds.size(), bookTitle);
                try {
                    notificationService.notifyStudentBookDeleted(allStudentIds, bookTitle, bookId);
                } catch (InterruptedException | ExecutionException e) {
                    logger.error("Error sending 'book_deleted' notifications for book ID {}: {}", bookId, e.getMessage(), e);
                }
            }

            return true;
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.error("Error deleting book with bookId: {}", bookId, e);
            throw e;
        }
    }

    public String updateBook(String bookId, RestBooks updatedBook) throws InterruptedException, ExecutionException, TimeoutException {
        logger.info("Updating book with bookId: {}", bookId);
        String originalBookTitle = null;
        try {
            DocumentReference bookRef = firestore.collection(BOOKS_COLLECTION).document(bookId);

            // Get the original book title for the notification
            DocumentSnapshot originalBookSnapshot = bookRef.get().get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS);
            if (originalBookSnapshot.exists()) {
                originalBookTitle = originalBookSnapshot.getString("title");
            }

            Map<String, Object> updatedBookData = new HashMap<>();
            updatedBookData.put("title", updatedBook.getTitle());
            updatedBookData.put("author", updatedBook.getAuthor());
            updatedBookData.put("edition", updatedBook.getEdition());
            updatedBookData.put("ISBN", updatedBook.getISBN());
            updatedBookData.put("condition", updatedBook.getCondition());
            updatedBookData.put("description", updatedBook.getDescription());
            updatedBookData.put("price", updatedBook.getPrice());
            updatedBookData.put("isDigital", updatedBook.isDigital());
            updatedBookData.put("digitalCopyPath", updatedBook.getDigitalCopyPath());
            updatedBookData.put("ownedBy", updatedBook.getOwnedBy() != null ? updatedBook.getOwnedBy() : new ArrayList<>());
            updatedBookData.put("userId", updatedBook.getUserId());
            updatedBookData.put("courseId", updatedBook.getCourseId());

            ApiFuture<WriteResult> writeResult = bookRef.update(updatedBookData);
            logger.info("Book updated at: {}", writeResult.get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS).getUpdateTime().toString());

            // --- Generate notification for ALL students about the update ---
            if (originalBookTitle != null && !originalBookTitle.equals(updatedBook.getTitle())) {
                List<String> allStudentIds = getAllStudentIds();
                logger.info("Notifying {} students about the update of book: {} to {}", allStudentIds.size(), originalBookTitle, updatedBook.getTitle());
                try {
                    notificationService.notifyStudentBookUpdated(allStudentIds, originalBookTitle, updatedBook.getTitle(), bookId);
                } catch (InterruptedException | ExecutionException e) {
                    logger.error("Error sending 'book_updated' notifications for book ID {}: {}", bookId, e.getMessage(), e);
                }
            } else if (originalBookTitle != null) {
                List<String> allStudentIds = getAllStudentIds();
                logger.info("Notifying {} students about the update of book: {}", allStudentIds.size(), originalBookTitle);
                try {
                    notificationService.notifyStudentBookUpdated(allStudentIds, originalBookTitle, updatedBook.getTitle(), bookId);
                } catch (InterruptedException | ExecutionException e) {
                    logger.error("Error sending 'book_updated' notifications for book ID {}: {}", bookId, e.getMessage(), e);
                }
            }

            return writeResult.get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS).getUpdateTime().toString();
        } catch (Exception e) {
            logger.error("Error updating book: {}", e.getMessage(), e);
            throw e;
        }
    }


    public DocumentSnapshot getBookDocumentSnapshot(String bookId) throws ExecutionException, InterruptedException, TimeoutException {
        DocumentReference bookRef = firestore.collection(BOOKS_COLLECTION).document(bookId);
        return bookRef.get().get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS);
    }

    public double purchaseBook(String bookId, String buyerEmail) throws InterruptedException, ExecutionException, TimeoutException {
        logger.info("Purchasing book with bookId: {} for email: {}", bookId, buyerEmail);

        try {
            return firestore.runTransaction(transaction -> {

                // --- Read book document ---
                DocumentReference bookRef = firestore.collection("Books").document(bookId);
                DocumentSnapshot bookDoc = transaction.get(bookRef).get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS);
                if (!bookDoc.exists()) {
                    logger.warn("Book not found with ID: {}", bookId);
                    return -1.0;
                }

                // --- Read buyer document ---
                QuerySnapshot userQuery = firestore.collection("Users")
                        .whereEqualTo("email", buyerEmail).get().get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS);
                if (userQuery.isEmpty()) {
                    logger.warn("Buyer not found with email: {}", buyerEmail);
                    return -1.0;
                }
                DocumentSnapshot buyerDoc = userQuery.getDocuments().get(0);
                DocumentReference buyerRef = buyerDoc.getReference();
                String buyerId = buyerDoc.getId();

                // --- Optionally read seller document ---
                String sellerId = bookDoc.getString("userId");
                DocumentReference sellerRef = null;
                DocumentSnapshot sellerSnapshot = null;
                if (sellerId != null) {
                    sellerRef = firestore.collection("Users").document(sellerId);
                    sellerSnapshot = transaction.get(sellerRef).get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS);
                }

                // --- Perform all writes after reads ---
                Double bookPrice = bookDoc.getDouble("price");
                Double userBalance = buyerDoc.getDouble("balance");

                if (bookPrice == null || userBalance == null || userBalance < bookPrice) {
                    logger.warn("Invalid price or insufficient funds for buyer: {}", buyerEmail);
                    return -1.0;
                }

                double newBalance = userBalance - bookPrice;
                transaction.update(buyerRef, "balance", newBalance);
                logger.info("Buyer balance updated for buyer: {}", buyerEmail);

                List<String> ownedBy = (List<String>) bookDoc.get("ownedBy");
                if (ownedBy == null) {
                    ownedBy = new ArrayList<>();
                }
                ownedBy.add(buyerEmail);
                transaction.update(bookRef, "ownedBy", ownedBy);
                logger.info("Book ownership updated for book: {}", bookId);

                if (sellerRef != null && sellerSnapshot != null && sellerSnapshot.exists()) {
                    transaction.update(sellerRef, "listings", FieldValue.arrayRemove(bookId));
                    logger.debug("Book ID {} removed from listings of seller with ID: {}", bookId, sellerId);
                }

                // --- Create a new transaction record ---
                Map<String, Object> transactionData = new HashMap<>();
                transactionData.put("order status", "completed");
                transactionData.put("bookId", bookId); // <--- Now storing just the book ID
                transactionData.put("userId", buyerId); // Still storing user ID

                transaction.set(firestore.collection("Transactions").document(), transactionData);
                logger.info("New transaction record created for book {} and buyer {}", bookId, buyerEmail);

                return newBalance;

            }).get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            logger.error("Timeout occurred during purchase: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error purchasing book: {}", e.getMessage(), e);
            throw e;
        } finally {
            // Notify buyer after purchase
            try {
                QuerySnapshot userQuery = firestore.collection("Users")
                        .whereEqualTo("email", buyerEmail).get().get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS);
                if (!userQuery.isEmpty()) {
                    String studentId = userQuery.getDocuments().get(0).getId();
                    DocumentSnapshot bookSnapshot = getBookDocumentSnapshot(bookId);
                    if (bookSnapshot.exists()) {
                        String bookTitle = bookSnapshot.getString("title");
                        try {
                            notificationService.notifyStudentBookPurchased(studentId, bookTitle, bookId);
                        } catch (InterruptedException | ExecutionException e) {
                            logger.error("Error sending purchase notification to student {}: {}", studentId, e.getMessage(), e);
                        }
                    }
                }
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                logger.error("Error retrieving data for purchase notification: {}", e.getMessage(), e);
            }
        }
    }



    public RestBooks getBookById(String bookId) throws InterruptedException, ExecutionException, TimeoutException {
        DocumentSnapshot document = getBookDocumentSnapshot(bookId);
        if (document.exists()) {
            return documentSnapshotToRestBooks(document);
        } else {
            return null;
        }
    }

    public List<RestBooks> findBooksOwnedByUserEmail(String email) throws InterruptedException, ExecutionException, TimeoutException {
        logger.info("Finding books owned by email: {}", email);
        CollectionReference booksCollection = firestore.collection(BOOKS_COLLECTION);
        Query query = booksCollection.whereArrayContains("ownedBy", email);
        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        List<QueryDocumentSnapshot> documents = querySnapshot.get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS).getDocuments();

        logger.debug("Number of documents retrieved for owned books: {}", documents.size());

        List<RestBooks> ownedBooks = new ArrayList<>();

        for (QueryDocumentSnapshot document : documents) {
            if (document.exists()) {
                ownedBooks.add(documentSnapshotToRestBooks(document));
            }
        }

        return ownedBooks;
    }

    public boolean deleteBookByTitle(String title) throws ExecutionException, InterruptedException, TimeoutException {
        logger.info("Deleting book with title: {}", title);
        try {
            Query query = firestore.collection(BOOKS_COLLECTION).whereEqualTo("title", title);
            ApiFuture<QuerySnapshot> querySnapshot = query.get();
            List<QueryDocumentSnapshot> documents = querySnapshot.get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS).getDocuments();

            if (!documents.isEmpty()) {
                WriteBatch batch = firestore.batch();
                for (QueryDocumentSnapshot document : documents
                ) {
                    String bookId = document.getId();
                    String sellerEmail = document.getString("userId");
                    if (sellerEmail != null) {
                        batch.update(firestore.collection(USERS_COLLECTION).document(sellerEmail), "listings", FieldValue.arrayRemove(bookId));
                        logger.debug("Book ID {} (title: {}) removed from listings of seller: {}", bookId, title, sellerEmail);
                    }
                    batch.delete(document.getReference());
                }
                batch.commit().get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS);
                return true;
            } else {
                logger.warn("Book not found for deletion with title: {}", title);
                return false;
            }
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.error("Error deleting book with title: {}", title, e);
            throw e;
        }
    }

    public List<RestBooks> getCourseBooksByCourseName(String courseName) throws ExecutionException, InterruptedException, TimeoutException {
        ApiFuture<QuerySnapshot> courseQuery = firestore.collection(COURSES_COLLECTION)
                .whereEqualTo("Course Name", courseName)
                .get();
        List<QueryDocumentSnapshot> courseDocuments = courseQuery.get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS).getDocuments();

        if (courseDocuments.isEmpty()) {
            logger.warn("Course document does not exist for name: {}", courseName);
            return new ArrayList<>();
        }

        DocumentSnapshot courseDocument = courseDocuments.get(0);
        List<String> bookTitles = (List<String>) courseDocument.get("textbooks");

        if (bookTitles == null || bookTitles.isEmpty()) {
            logger.warn("No book titles found for course name: {}", courseName);
            return new ArrayList<>();
        }

        List<RestBooks> books = new ArrayList<>();
        for (String title : bookTitles) {
            ApiFuture<QuerySnapshot> bookQuery = firestore.collection(BOOKS_COLLECTION)
                    .whereEqualTo("title", title)
                    .get();
            List<QueryDocumentSnapshot> bookDocuments = bookQuery.get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS).getDocuments();

            if (!bookDocuments.isEmpty()) {
                books.add(documentSnapshotToRestBooks(bookDocuments.get(0)));
            }
        }
        return books;
    }

    public boolean removeBookByTitle(String bookTitle) throws InterruptedException, ExecutionException, TimeoutException {
        Query courseQuery = firestore.collection(COURSES_COLLECTION)
                .whereArrayContains("textbooks", bookTitle);
        ApiFuture<QuerySnapshot> courseQueryFuture = courseQuery.get();
        QuerySnapshot courseQuerySnapshot = courseQueryFuture.get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS);

        WriteBatch batch = firestore.batch();
        for (DocumentSnapshot courseDocument : courseQuerySnapshot.getDocuments()) {
            List<String> textbooks = (List<String>) courseDocument.get("textbooks");
            if (textbooks != null) {
                textbooks.remove(bookTitle);
                batch.update(courseDocument.getReference(), "textbooks", textbooks);
            }
        }
        batch.commit().get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS);

        Query bookQuery = firestore.collection(BOOKS_COLLECTION).whereEqualTo("title", bookTitle);
        ApiFuture<QuerySnapshot> bookQueryFuture = bookQuery.get();
        QuerySnapshot bookQuerySnapshot = bookQueryFuture.get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS);

        if (!bookQuerySnapshot.isEmpty()) {
            WriteBatch bookBatch = firestore.batch();
            for (DocumentSnapshot bookDocument : bookQuerySnapshot.getDocuments()) {
                String bookId = bookDocument.getId();
                String sellerEmail = bookDocument.getString("userId");
                if (sellerEmail != null) {
                    bookBatch.update(firestore.collection(USERS_COLLECTION).document(sellerEmail), "listings", FieldValue.arrayRemove(bookId));
                    logger.debug("Book ID {} (title: {}) removed from listings of seller: {}", bookId, bookTitle, sellerEmail);
                }
                bookBatch.delete(bookDocument.getReference());
            }
            bookBatch.commit().get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS);
            return true;
        } else {
            return false;
        }
    }

    public void rateBook(String bookTitle, String userEmail, int rating) throws InterruptedException, ExecutionException, TimeoutException {
        logger.info("Rating book with title: {} by user: {} with rating: {}", bookTitle, userEmail, rating);

        try {
            Query query = firestore.collection(BOOKS_COLLECTION).whereEqualTo("title", bookTitle);
            ApiFuture<QuerySnapshot> querySnapshot = query.get();
            List<QueryDocumentSnapshot> documents = querySnapshot.get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS).getDocuments();

            if (!documents.isEmpty()) {
                DocumentSnapshot bookDoc = documents.get(0);
                DocumentReference bookRef = bookDoc.getReference();

                double currentRating = bookDoc.getDouble("rating") != null ? bookDoc.getDouble("rating") : 0.0;
                long currentRatingCount = bookDoc.getLong("ratingCount") != null ? bookDoc.getLong("ratingCount") : 0;
                double newRating = (currentRating * currentRatingCount + rating) / (currentRatingCount + 1);
                long newRatingCount = currentRatingCount + 1;

                Map<String, Object> updatedBookData = new HashMap<>();
                updatedBookData.put("rating", newRating);
                updatedBookData.put("ratingCount", newRatingCount);

                bookRef.update(updatedBookData).get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS);

                logger.info("Book rating updated successfully for book: {}", bookTitle);
            } else {
                logger.warn("Book not found for rating with title: {}", bookTitle);
            }
        } catch (Exception e) {
            logger.error("Error rating book: {}", e.getMessage(), e);
            throw e;
        }
    }

    public List<RestBooks> getSellerListings(String sellerEmail) throws InterruptedException, ExecutionException, TimeoutException {
        logger.info("Getting listings for seller with email: {}", sellerEmail);
        CollectionReference booksCollection = firestore.collection(BOOKS_COLLECTION);
        Query query = booksCollection.whereEqualTo("addedBy", sellerEmail); // Changed to query 'addedBy'
        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        List<QueryDocumentSnapshot> documents = querySnapshot.get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS).getDocuments();

        List<RestBooks> sellerBooks = new ArrayList<>();

        for (QueryDocumentSnapshot document : documents) {
            if (document.exists()) {
                sellerBooks.add(documentSnapshotToRestBooks(document));
            }
        }
        logger.debug("Retrieved {} listings for seller: {}", sellerBooks.size(), sellerEmail);
        return sellerBooks;
    }

    private RestBooks documentSnapshotToRestBooks(DocumentSnapshot document) {
        String bookId = document.getId();
        List<String> ownedBy = (List<String>) document.get("ownedBy");
        if (ownedBy == null) {
            ownedBy = new ArrayList<>();
        }
        String userIdStr = document.getString("userId"); // Still retrieving userId (seller's document ID)
        String addedByStr = document.getString("addedBy"); // Retrieving the seller's email
        String courseIdStr = document.getString("courseId");
        Double priceDouble = document.getDouble("price");
        double price = (priceDouble != null) ? priceDouble : 0.0;
        Boolean isDigitalBoolean = document.getBoolean("isDigital");
        boolean isDigital = (isDigitalBoolean != null) ? isDigitalBoolean : false;
        Double ratingDouble = document.getDouble("rating");
        double rating = (ratingDouble != null) ? ratingDouble : 0.0;
        Long ratingCountLong = document.getLong("ratingCount");
        long ratingCount = (ratingCountLong != null) ? ratingCountLong : 0;

        return new RestBooks(
                document.getString("title"),
                document.getString("author"),
                document.getString("edition"),
                document.getString("ISBN"),
                document.getString("condition"),
                document.getString("description"),
                price,
                isDigital,
                document.getString("digitalCopyPath"),
                bookId,
                ownedBy,
                userIdStr,
                courseIdStr,
                rating,
                ratingCount,
                addedByStr
        );
    }

    public boolean removeBookFromListingByEmailAndBookId(String bookIdToRemove, String sellerEmail) throws InterruptedException, ExecutionException, TimeoutException {
        logger.info("Removing book with ID '{}' from listing of seller with email: {}", bookIdToRemove, sellerEmail);

        // 1. Find the seller's document ID
        ApiFuture<QuerySnapshot> userQuerySnapshot = firestore.collection(USERS_COLLECTION)
                .whereEqualTo("email", sellerEmail)
                .get();
        QuerySnapshot userSnapshot = userQuerySnapshot.get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS);

        if (userSnapshot != null && !userSnapshot.isEmpty()) {
            String sellerId = userSnapshot.getDocuments().get(0).getId();
            DocumentReference userRef = firestore.collection(USERS_COLLECTION).document(sellerId);

            // 2. Remove the bookId from the seller's 'listings' array
            ApiFuture<WriteResult> updateFuture = userRef.update("listings", FieldValue.arrayRemove(bookIdToRemove));
            updateFuture.get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS);

            logger.info("Book with ID '{}' removed from listing of seller ID: {}", bookIdToRemove, sellerId);
            return true;

        } else {
            logger.warn("Seller with email {} not found.", sellerEmail);
            return false;
        }
    }

    public String getBookTitle(String bookId) throws InterruptedException, ExecutionException, TimeoutException {
        logger.info("Attempting to retrieve book title for ID: {}", bookId);
        long startTime = System.currentTimeMillis();
        DocumentReference bookRef = firestore.collection(BOOKS_COLLECTION).document(bookId);
        ApiFuture<DocumentSnapshot> future = bookRef.get();
        DocumentSnapshot document = future.get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS);
        long endTime = System.currentTimeMillis();
        logger.info("Retrieving book title for ID {} took {} ms.", bookId, (endTime - startTime));
        if (document.exists()) {
            return document.getString("title"); // Assuming your book documents have a 'title' field
        } else {
            logger.warn("Book with ID {} not found.", bookId);
            return null;
        }
    }


    public RestBooks findBookById(String bookId) throws InterruptedException, ExecutionException, TimeoutException {
        logger.info("Finding book by ID: {}", bookId);
        DocumentReference docRef = firestore.collection(BOOKS_COLLECTION).document(bookId);
        ApiFuture<DocumentSnapshot> future = docRef.get();
        DocumentSnapshot document = future.get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS);
        if (document.exists()) {
            RestBooks book = document.toObject(RestBooks.class);
            if (book != null) {
                book.setBookId(document.getId()); // Ensure bookId is set
            }
            logger.info("Found book: {}", book);
            return book;
        } else {
            logger.warn("Book with ID {} not found.", bookId);
            return null;
        }
    }

    public boolean updateBookExchangeableStatus(String bookId, boolean isExchangeable) throws InterruptedException, ExecutionException, TimeoutException {
        logger.info("Updating exchangeable status for book ID {} to {}", bookId, isExchangeable);
        DocumentReference bookRef = firestore.collection(BOOKS_COLLECTION).document(bookId);
        ApiFuture<WriteResult> writeResult = bookRef.update("isExchangeable", isExchangeable);
        writeResult.get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS);
        logger.debug("Exchangeable status updated successfully for book ID {}", bookId);
        return true; // Consider adding more robust error handling
    }

    public List<RestBooks> findExchangeableBooks(String currentUserId) throws InterruptedException, ExecutionException, TimeoutException {
        logger.info("findExchangeableBooks method called with userId: {}", currentUserId);
        logger.info("Finding exchangeable books owned by students, excluding user ID: {}", currentUserId);
        CollectionReference booksCollection = firestore.collection(BOOKS_COLLECTION);
        List<RestBooks> exchangeableBooks = new ArrayList<>();

        Query query = booksCollection.whereNotEqualTo("userId", currentUserId); // Filter out current user's books
        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        List<QueryDocumentSnapshot> bookDocuments = querySnapshot.get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS).getDocuments();

        for (QueryDocumentSnapshot bookDoc : bookDocuments) {
            RestBooks book = documentSnapshotToRestBooks(bookDoc);
            // Fetch the owner's role
            DocumentSnapshot userSnapshot = firestore.collection(USERS_COLLECTION).document(book.getUserId()).get().get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS);
            if (userSnapshot.exists() && "student".equals(userSnapshot.getString("role"))) {
                exchangeableBooks.add(book);
            }
        }
        logger.debug("Found {} exchangeable books owned by students (excluding current user).", exchangeableBooks.size());
        return exchangeableBooks;
    }

    // New method to get the owner ID of a book
    public String getBookOwnerId(String bookId) throws InterruptedException, ExecutionException, TimeoutException {
        ApiFuture<DocumentSnapshot> future = firestore.collection(BOOKS_COLLECTION).document(bookId).get();
        DocumentSnapshot document = future.get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS);
        if (document.exists()) {
            return document.getString("userId"); // Assuming your book documents have a "userId" field indicating the owner
        } else {
            logger.warn("Book with ID {} not found while fetching owner ID.", bookId);
            return null;
        }
    }


}