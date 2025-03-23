package edu.famu.thebookexchange.repository;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import edu.famu.thebookexchange.model.Rest.RestBooks;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Repository
public class BooksRepositoryImpl implements BooksRepository {

    private static final Logger logger = LoggerFactory.getLogger(BooksRepositoryImpl.class);
    private final Firestore firestore = FirestoreClient.getFirestore();
    private static final String BOOKS_COLLECTION = "Books";
    private static final long FIRESTORE_TIMEOUT = 5;

    @Override
    public CompletableFuture<List<RestBooks>> findAll() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                ApiFuture<QuerySnapshot> future = firestore.collection(BOOKS_COLLECTION).get();
                List<QueryDocumentSnapshot> documents = future.get().getDocuments();
                List<RestBooks> books = new ArrayList<>();
                for (QueryDocumentSnapshot document : documents) {
                    RestBooks book = new RestBooks();
                    book.setBookId(document.getId());

                    book.setTitle(document.getString("title"));
                    book.setAuthor(document.getString("author"));
                    book.setEdition(document.getString("edition"));
                    book.setISBN(document.getString("ISBN"));
                    book.setCondition(document.getString("condition"));
                    book.setDescription(document.getString("description"));
                    book.setPrice(document.getDouble("price") != null ? document.getDouble("price") : 0.0);
                    book.setDigital(document.getBoolean("is_digital") != null ? document.getBoolean("is_digital") : false);
                    book.setDigitalCopyPath(document.getString("digital_copy_path"));

                    book.setUserId(document.getString("userId")); // Store as String
                    book.setCourseId(document.getString("courseId")); // Store as String

                    List<String> ownedBy = (List<String>) document.get("ownedBy");
                    book.setOwnedBy(ownedBy);

                    books.add(book);
                }
                return books;
            } catch (Exception e) {
                logger.error("Error finding all books", e);
                return new ArrayList<>();
            }
        });
    }

    @Override
    public CompletableFuture<String> save(RestBooks book) {
        return CompletableFuture.supplyAsync(() -> {
            try {
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

                bookData.put("userId", book.getUserId()); // Store as String
                bookData.put("courseId", book.getCourseId()); // Store as String

                bookData.put("ownedBy", book.getOwnedBy());

                ApiFuture<DocumentReference> addedDocRef = firestore.collection(BOOKS_COLLECTION).add(bookData);
                return addedDocRef.get().getId();

            } catch (InterruptedException | ExecutionException e) {
                logger.error("Error saving book", e);
                return null;
            }
        });
    }

    @Override
    public CompletableFuture<Boolean> deleteByTitle(String title) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Query query = firestore.collection(BOOKS_COLLECTION).whereEqualTo("title", title);
                ApiFuture<QuerySnapshot> querySnapshot = query.get();
                List<QueryDocumentSnapshot> documents = querySnapshot.get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS).getDocuments();

                if (!documents.isEmpty()) {
                    for (QueryDocumentSnapshot document : documents) {
                        firestore.collection(BOOKS_COLLECTION).document(document.getId()).delete().get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS);
                    }
                    return true;
                } else {
                    return false;
                }
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                logger.error("Error deleting book by title", e);
                return false;
            }
        });
    }

    @Override
    public CompletableFuture<String> update(String bookId, RestBooks updatedBook) {
        return CompletableFuture.supplyAsync(() -> {
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
                updatedBookData.put("is_digital", updatedBook.isDigital());
                updatedBookData.put("digital_copy_path", updatedBook.getDigitalCopyPath());

                updatedBookData.put("userId", updatedBook.getUserId()); // Store as String
                updatedBookData.put("courseId", updatedBook.getCourseId()); // Store as String

                updatedBookData.put("ownedBy", updatedBook.getOwnedBy());

                ApiFuture<WriteResult> writeResult = bookRef.update(updatedBookData);
                writeResult.get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS);

                return bookId;
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                logger.error("Error updating book", e);
                return null;
            }
        });
    }

    @Override
    public CompletableFuture<Boolean> purchaseBook(String bookId, String userId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                DocumentReference bookRef = firestore.collection(BOOKS_COLLECTION).document(bookId);
                DocumentSnapshot document = bookRef.get().get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS);

                if (document.exists()) {
                    List<String> ownedBy = (List<String>) document.get("ownedBy");
                    if (ownedBy == null) {
                        ownedBy = new ArrayList<>();
                    }
                    ownedBy.add(userId);

                    bookRef.update("ownedBy", ownedBy).get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS);
                    return true;
                } else {
                    return false;
                }
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                logger.error("Error purchasing book", e);
                return false;
            }
        });
    }


    @Override
    public CompletableFuture<List<RestBooks>> findBooksOwnedByUser(String userId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Query query = firestore.collection(BOOKS_COLLECTION).whereArrayContains("ownedBy", userId);
                ApiFuture<QuerySnapshot> querySnapshot = query.get();
                List<QueryDocumentSnapshot> documents = querySnapshot.get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS).getDocuments();
                List<RestBooks> books = new ArrayList<>();
                for (QueryDocumentSnapshot document : documents) {
                    RestBooks book = new RestBooks();
                    book.setBookId(document.getId());

                    book.setTitle(document.getString("title"));
                    book.setAuthor(document.getString("author"));
                    book.setEdition(document.getString("edition"));
                    book.setISBN(document.getString("ISBN"));
                    book.setCondition(document.getString("condition"));
                    book.setDescription(document.getString("description"));
                    book.setPrice(document.getDouble("price") != null ? document.getDouble("price") : 0.0);
                    book.setDigital(document.getBoolean("is_digital") != null ? document.getBoolean("is_digital") : false);
                    book.setDigitalCopyPath(document.getString("digital_copy_path"));

                    book.setUserId(document.getString("userId")); // Store as String
                    book.setCourseId(document.getString("courseId")); // Store as String

                    List<String> ownedBy = (List<String>) document.get("ownedBy");
                    book.setOwnedBy(ownedBy);

                    books.add(book);
                }
                return books;
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                logger.error("Error finding books owned by user", e);
                return new ArrayList<>();
            }
        });
    }

    @Override
    public CompletableFuture<RestBooks> findById(String bookId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                DocumentSnapshot document = firestore.collection(BOOKS_COLLECTION).document(bookId).get().get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS);

                if (document.exists()) {
                    RestBooks book = new RestBooks();
                    book.setBookId(document.getId());
                    book.setTitle(document.getString("title"));
                    book.setAuthor(document.getString("author"));
                    book.setEdition(document.getString("edition"));
                    book.setISBN(document.getString("ISBN"));
                    book.setCondition(document.getString("condition"));
                    book.setDescription(document.getString("description"));
                    book.setPrice(document.getDouble("price") != null ? document.getDouble("price") : 0.0);
                    book.setDigital(document.getBoolean("is_digital") != null ? document.getBoolean("is_digital") : false);
                    book.setDigitalCopyPath(document.getString("digital_copy_path"));

                    book.setUserId(document.getString("userId")); // Store as String
                    book.setCourseId(document.getString("courseId")); // Store as String

                    List<String> ownedBy = (List<String>) document.get("ownedBy");
                    book.setOwnedBy(ownedBy);

                    return book;
                } else {
                    return null; // Or handle not found differently
                }
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                logger.error("Error finding book by ID", e);
                return null;
            }
        });
    }
}