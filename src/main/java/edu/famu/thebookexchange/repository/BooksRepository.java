package edu.famu.thebookexchange.repository;

import edu.famu.thebookexchange.model.Rest.RestBooks;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Repository
public interface BooksRepository {

    CompletableFuture<List<RestBooks>> findAll();

    CompletableFuture<String> save(RestBooks book);

    CompletableFuture<Boolean> deleteByTitle(String title);

    CompletableFuture<String> update(String bookId, RestBooks updatedBook);

    CompletableFuture<Boolean> purchaseBook(String bookId, String userId);

    CompletableFuture<List<RestBooks>> findBooksOwnedByUser(String userId);

    CompletableFuture<RestBooks> findById(String bookId); // Added findById method
}