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
    private static final long FIRESTORE_TIMEOUT = 5; // Timeout in seconds

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
                Double price = document.getDouble("price");
                double actualPrice = (price != null) ? price : 0.0;

                Boolean isDigitalBoolean = document.getBoolean("is_digital");
                boolean actualIsDigital = (isDigitalBoolean != null) ? isDigitalBoolean : false; // Default to false

                RestBooks book = new RestBooks(
                        document.getString("title"),
                        document.getString("author"),
                        document.getString("edition"),
                        document.getString("ISBN"),
                        document.getString("condition"),
                        document.getString("description"),
                        actualPrice,
                        actualIsDigital, // Use actualIsDigital
                        document.getString("digital_copy_path"),
                        document.get("courseId", DocumentReference.class),
                        document.get("userId", DocumentReference.class)
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
        bookData.put("is_digital", book.isDigital());
        bookData.put("digital_copy_path", book.getDigitalCopyPath());
        bookData.put("courseId", book.getCourseId());
        bookData.put("userId", book.getUserId());

        ApiFuture<DocumentReference> writeResult = firestore.collection(BOOKS_COLLECTION).add(bookData);
        DocumentReference rs = writeResult.get();
        logger.info("Book added with ID: {}", rs.getId());
        return rs.getId();
    }

    public boolean deleteBookByTitle(String title) throws ExecutionException, InterruptedException, TimeoutException {
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

    public String updateBook(String bookId, RestBooks updatedBook) throws InterruptedException, ExecutionException, TimeoutException {
        DocumentReference bookRef = firestore.collection(BOOKS_COLLECTION).document(bookId);

        Map<String, Object> updatedBookData = new HashMap<>();
        updatedBookData.put("title", updatedBook.getTitle());
        updatedBookData.put("author", updatedBook.getAuthor());
        updatedBookData.put("edition", updatedBook.getEdition());
        updatedBookData.put("ISBN", updatedBook.getISBN());
        updatedBookData.put("condition", updatedBook.getCondition());
        updatedBookData.put("description", updatedBook.getDescription());
        updatedBookData.put("price", updatedBook.getPrice());
        updatedBookData.put("is_digital", updatedBook.isDigital());
        updatedBookData.put("digital_copy_path", updatedBook.getDigitalCopyPath());
        updatedBookData.put("courseId", updatedBook.getCourseId());
        updatedBookData.put("userId", updatedBook.getUserId());

        ApiFuture<WriteResult> writeResult = bookRef.update(updatedBookData);
        logger.info("Book updated at: {}", writeResult.get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS).getUpdateTime().toString());

        return writeResult.get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS).getUpdateTime().toString();
    }
}