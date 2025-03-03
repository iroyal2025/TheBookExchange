package edu.famu.thebookexchange.service;

import com.google.cloud.firestore.QuerySnapshot;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import edu.famu.thebookexchange.model.Rest.RestBooks;
import edu.famu.thebookexchange.util.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

@Service
public class BooksService {

    private static final Logger logger = LoggerFactory.getLogger(BooksService.class);
    private static final Firestore db = FirestoreClient.getFirestore();
    private static final CollectionReference BooksCollection = db.collection("Books");
    private static final long FIRESTORE_TIMEOUT = 5; // Timeout in seconds



    public ResponseEntity<ApiResponse<List<RestBooks>>> getAllBooks() {
        try {


            logger.info("Retrieving all books from Firestore...");
            List<RestBooks> books = BooksCollection.get().get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS).toObjects(RestBooks.class);


            logger.info("Retrieved {} books from Firestore", books.size());
            if (books.isEmpty()) {
                logger.warn("No books found in Firestore collection");
            } else {
                logger.debug("Retrieved books: {}", books);
            }
            return ResponseEntity.ok(new ApiResponse<>(true, "Books retrieved successfully", books, null));
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.error("Error retrieving all books", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false, "Error retrieving books", null, e.getMessage()));
        }

    }

    public ResponseEntity<ApiResponse<RestBooks>> addBook(RestBooks book) {
        try {
            // No need to set bookId, Firestore will auto-generate it
            logger.info("Adding book to Firestore");
            BooksCollection.add(book).get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS);
            logger.info("Book added successfully to Firestore");
            return ResponseEntity.ok(new ApiResponse<>(true, "Book added successfully", book, null));
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.error("Error adding book", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false, "Error adding book", null, e.getMessage()));
        }
    }

    //update getBookById, updateBook, and deleteBook as needed.
    public ResponseEntity<ApiResponse<List<RestBooks>>> getBooksByUserId(String userId) {
        try {
            logger.info("Retrieving books by user ID: {}", userId);
            List<RestBooks> books = BooksCollection.get().get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS).toObjects(RestBooks.class).stream()
                    .filter(book -> book.getUserId().getId().equals(userId))
                    .collect(Collectors.toList());
            logger.info("Retrieved {} books by user ID: {}", books.size(), userId);
            return ResponseEntity.ok(new ApiResponse<>(true, "Books retrieved by user ID successfully", books, null));
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.error("Error retrieving books by user ID {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false, "Error retrieving books by user ID", null, e.getMessage()));
        }
    }

    public ResponseEntity<ApiResponse<List<RestBooks>>> getBooksByCourseId(String courseId) {
        try {
            logger.info("Retrieving books by course ID: {}", courseId);
            List<RestBooks> books = BooksCollection.get().get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS).toObjects(RestBooks.class).stream()
                    .filter(book -> book.getCourseId().getId().equals(courseId))
                    .collect(Collectors.toList());
            logger.info("Retrieved {} books by course ID: {}", books.size(), courseId);
            return ResponseEntity.ok(new ApiResponse<>(true, "Books retrieved by course ID successfully", books, null));
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.error("Error retrieving books by course ID {}", courseId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false, "Error retrieving books by course ID", null, e.getMessage()));
        }
    }

    public boolean deleteBookByTitle(String title) throws ExecutionException, InterruptedException, TimeoutException {
        try {
            Query query = BooksCollection.whereEqualTo("title", title);
            ApiFuture<QuerySnapshot> querySnapshot = query.get();
            List<QueryDocumentSnapshot> documents = querySnapshot.get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS).getDocuments();

            if (!documents.isEmpty()) {
                for (QueryDocumentSnapshot document : documents) {
                    BooksCollection.document(document.getId()).delete().get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS);
                    logger.info("Book deleted successfully with ID: {}", document.getId());
                }
                return true; // Deletion successful
            } else {
                logger.warn("Book not found for deletion with title: {}", title);
                return false; // Book not found
            }
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.error("Error deleting book with title: {}", title, e);
            throw e;
        }
    }

}