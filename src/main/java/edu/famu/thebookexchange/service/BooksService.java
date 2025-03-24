package edu.famu.thebookexchange.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import edu.famu.thebookexchange.model.Rest.RestBooks;
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
public class BooksService {

    private static final Logger logger = LoggerFactory.getLogger(BooksService.class);
    private Firestore firestore;

    private static final String BOOKS_COLLECTION = "Books";
    private static final String COURSES_COLLECTION = "Courses";
    private static final long FIRESTORE_TIMEOUT = 5;

    public BooksService() {
        this.firestore = FirestoreClient.getFirestore();
    }

    public List<RestBooks> getAllBooks() throws InterruptedException, ExecutionException, TimeoutException {
        CollectionReference booksCollection = firestore.collection(BOOKS_COLLECTION);
        ApiFuture<QuerySnapshot> querySnapshot = booksCollection.get();
        List<QueryDocumentSnapshot> documents = querySnapshot.get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS).getDocuments();

        List<RestBooks> books = new ArrayList<>();

        for (QueryDocumentSnapshot document : documents) {
            if (document.exists()) {
                String bookId = document.getId();
                List<String> ownedBy = (List<String>) document.get("ownedBy");
                if (ownedBy == null) {
                    ownedBy = new ArrayList<>();
                }

                RestBooks book = new RestBooks(
                        document.getString("title"),
                        document.getString("author"),
                        document.getString("edition"),
                        document.getString("ISBN"),
                        document.getString("condition"),
                        document.getString("description"),
                        document.getDouble("price") != null ? document.getDouble("price") : 0.0,
                        document.getBoolean("isDigital") != null ? document.getBoolean("isDigital") : false,
                        document.getString("digitalCopyPath"),
                        bookId,
                        ownedBy,
                        document.get("userId", DocumentReference.class),
                        document.get("courseId", DocumentReference.class)
                );
                books.add(book);
            }
        }

        return books;
    }

    public String addBook(RestBooks book) throws InterruptedException, ExecutionException {
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
        bookData.put("ownedBy", book.getOwnedBy());
        bookData.put("userId", book.getUserId());
        bookData.put("courseId", book.getCourseId());

        ApiFuture<DocumentReference> writeResult = firestore.collection(BOOKS_COLLECTION).add(bookData);
        DocumentReference rs = writeResult.get();
        logger.info("Book added with ID: {}", rs.getId());
        return rs.getId();
    }

    public boolean deleteBook(String bookId) throws ExecutionException, InterruptedException, TimeoutException {
        logger.info("Deleting book with bookId: {}", bookId);
        try {
            ApiFuture<WriteResult> writeResult = firestore.collection(BOOKS_COLLECTION).document(bookId).delete();
            writeResult.get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS);
            logger.info("Book deleted successfully with ID: {}", bookId);
            return true;
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.error("Error deleting book with bookId: {}", bookId, e);
            throw e;
        }
    }

    public String updateBook(String bookId, RestBooks updatedBook) throws InterruptedException, ExecutionException, TimeoutException {
        logger.info("Updating book with bookId: {}", bookId);
        try {
            DocumentReference bookRef = firestore.collection(BOOKS_COLLECTION).document(bookId);

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
            updatedBookData.put("ownedBy", updatedBook.getOwnedBy());
            updatedBookData.put("userId", updatedBook.getUserId());
            updatedBookData.put("courseId", updatedBook.getCourseId());

            ApiFuture<WriteResult> writeResult = bookRef.update(updatedBookData);
            logger.info("Book updated at: {}", writeResult.get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS).getUpdateTime().toString());

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

    public double purchaseBook(String bookId, String email) throws InterruptedException, ExecutionException, TimeoutException {
        logger.info("Purchasing book with bookId: {} for email: {}", bookId, email);

        try {
            return firestore.runTransaction(transaction -> {
                DocumentSnapshot bookDoc = transaction.get(firestore.collection(BOOKS_COLLECTION).document(bookId)).get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS);
                if (!bookDoc.exists()) {
                    logger.warn("Book not found with ID: {}", bookId);
                    return -1.0; // Indicate failure
                }

                // Find user document using email
                QuerySnapshot userQuery = firestore.collection("Users").whereEqualTo("email", email).get().get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS);
                if (userQuery.isEmpty()) {
                    logger.warn("User not found with email: {}", email);
                    return -1.0; // Indicate failure
                }
                DocumentSnapshot userDoc = userQuery.getDocuments().get(0);

                double bookPrice = bookDoc.getDouble("price");
                double userBalance = userDoc.getDouble("balance");

                if (userBalance < bookPrice) {
                    logger.warn("Insufficient funds for user: {}", email);
                    return -1.0; // Indicate failure
                }

                // Update user balance
                double newBalance = userBalance - bookPrice;
                transaction.update(firestore.collection("Users").document(userDoc.getId()), "balance", newBalance);
                logger.info("User balance updated for user: {}", email);

                // Update book ownership
                List<String> ownedBy = (List<String>) bookDoc.get("ownedBy");
                if (ownedBy == null) {
                    ownedBy = new ArrayList<>();
                }
                ownedBy.add(email);
                transaction.update(firestore.collection(BOOKS_COLLECTION).document(bookId), "ownedBy", ownedBy);
                logger.info("Book ownership updated for book: {}", bookId);

                return newBalance; // Indicate success.
            }).get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS); // Retrieve Double from ApiFuture
        } catch (Exception e) {
            logger.error("Error purchasing book: {}", e.getMessage(), e);
            throw e;
        }
    }

    public RestBooks getBookById(String bookId) throws InterruptedException, ExecutionException, TimeoutException {
        DocumentSnapshot document = getBookDocumentSnapshot(bookId);
        if (document.exists()) {
            return new RestBooks(
                    document.getString("title"),
                    document.getString("author"),
                    document.getString("edition"),
                    document.getString("ISBN"),
                    document.getString("condition"),
                    document.getString("description"),
                    document.getDouble("price") != null ? document.getDouble("price") : 0.0,
                    document.getBoolean("isDigital") != null ? document.getBoolean("isDigital") : false,
                    document.getString("digitalCopyPath"),
                    bookId,
                    (List<String>) document.get("ownedBy"),
                    document.get("userId", DocumentReference.class),
                    document.get("courseId", DocumentReference.class)
            );
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

        logger.debug("Number of documents retrieved: {}", documents.size()); // Log the number of documents

        List<RestBooks> ownedBooks = new ArrayList<>();

        for (QueryDocumentSnapshot document : documents) {
            if (document.exists()) {
                logger.debug("Document ID: {}, Data: {}", document.getId(), document.getData()); // Log document ID and data

                String bookId = document.getId();
                RestBooks book = new RestBooks(
                        document.getString("title"),
                        document.getString("author"),
                        document.getString("edition"),
                        document.getString("ISBN"),
                        document.getString("condition"),
                        document.getString("description"),
                        document.getDouble("price") != null ? document.getDouble("price") : 0.0,
                        document.getBoolean("isDigital") != null ? document.getBoolean("isDigital") : false,
                        document.getString("digitalCopyPath"),
                        bookId,
                        (List<String>) document.get("ownedBy"),
                        document.get("userId", DocumentReference.class),
                        document.get("courseId", DocumentReference.class)
                );
                ownedBooks.add(book);
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
                for (QueryDocumentSnapshot document : documents) {
                    firestore.collection(BOOKS_COLLECTION).document(document.getId()).delete().get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS);
                    logger.info("Book deleted successfully with ID: {} and title: {}", document.getId(), title);
                }
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
        // Find the course document by course name
        ApiFuture<QuerySnapshot> courseQuery = firestore.collection(COURSES_COLLECTION)
                .whereEqualTo("Course Name", courseName)
                .get();
        List<QueryDocumentSnapshot> courseDocuments = courseQuery.get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS).getDocuments();

        if (courseDocuments.isEmpty()) {
            logger.warn("Course document does not exist for name: {}", courseName);
            return new ArrayList<>();
        }

        DocumentSnapshot courseDocument = courseDocuments.get(0); // Assuming course name is unique
        List<String> bookTitles = (List<String>) courseDocument.get("textbooks");

        if (bookTitles == null || bookTitles.isEmpty()) {
            logger.warn("No book titles found for course name: {}", courseName);
            return new ArrayList<>();
        }

        // Fetch books based on titles
        List<RestBooks> books = new ArrayList<>();
        for (String title : bookTitles) {
            ApiFuture<QuerySnapshot> bookQuery = firestore.collection(BOOKS_COLLECTION)
                    .whereEqualTo("title", title)
                    .get();
            List<QueryDocumentSnapshot> bookDocuments = bookQuery.get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS).getDocuments();

            if (!bookDocuments.isEmpty()) {
                DocumentSnapshot bookDocument = bookDocuments.get(0); // Assuming title is unique
                String bookId = bookDocument.getId();
                List<String> ownedBy = (List<String>) bookDocument.get("ownedBy");
                if (ownedBy == null) {
                    ownedBy = new ArrayList<>();
                }

                RestBooks book = new RestBooks(
                        bookDocument.getString("title"),
                        bookDocument.getString("author"),
                        bookDocument.getString("edition"),
                        bookDocument.getString("ISBN"),
                        bookDocument.getString("condition"),
                        bookDocument.getString("description"),
                        bookDocument.getDouble("price") != null ? bookDocument.getDouble("price") : 0.0,
                        bookDocument.getBoolean("isDigital") != null ? bookDocument.getBoolean("isDigital") : false,
                        bookDocument.getString("digitalCopyPath"),
                        bookId,
                        ownedBy,
                        bookDocument.get("userId", DocumentReference.class),
                        bookDocument.get("courseId", DocumentReference.class)
                );
                books.add(book);
            }
        }

        return books;
    }

}