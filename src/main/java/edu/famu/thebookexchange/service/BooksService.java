package edu.famu.thebookexchange.service;

import edu.famu.thebookexchange.model.Rest.RestBooks;
import edu.famu.thebookexchange.repository.BooksRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
public class BooksService {

    private static final Logger logger = LoggerFactory.getLogger(BooksService.class);

    @Autowired
    private BooksRepository booksRepository;

    public List<RestBooks> getAllBooks() throws InterruptedException, ExecutionException, TimeoutException {
        logger.info("Retrieving all books from database...");
        CompletableFuture<List<RestBooks>> future = booksRepository.findAll();
        List<RestBooks> books = future.get(10, TimeUnit.SECONDS);
        logger.info("Books retrieved: {}", books);
        return books;
    }

    public String addBook(RestBooks book) throws InterruptedException, ExecutionException {
        logger.info("Adding book to database: {}", book);
        CompletableFuture<String> future = booksRepository.save(book);
        String bookId = future.get();
        logger.info("Book added with ID: {}", bookId);
        return bookId;
    }

    public boolean deleteBookByTitle(String title) throws ExecutionException, InterruptedException, TimeoutException {
        logger.info("Deleting book by title: {}", title);
        CompletableFuture<Boolean> future = booksRepository.deleteByTitle(title);
        boolean deleted = future.get(10, TimeUnit.SECONDS);
        logger.info("Book deleted: {}", deleted);
        return deleted;
    }

    public String updateBook(String bookId, RestBooks updatedBook) throws InterruptedException, ExecutionException, TimeoutException {
        logger.info("Updating book with ID: {}, Data: {}", bookId, updatedBook);
        CompletableFuture<String> future = booksRepository.update(bookId, updatedBook);
        String updateResult = future.get(10, TimeUnit.SECONDS);
        logger.info("Book update result: {}", updateResult);
        return updateResult;
    }

    public boolean purchaseBook(String bookId, String userId) throws InterruptedException, ExecutionException, TimeoutException {
        logger.info("Purchasing book with ID: {}, User ID: {}", bookId, userId);
        CompletableFuture<Boolean> future = booksRepository.purchaseBook(bookId, userId);
        boolean purchaseResult = future.get(10, TimeUnit.SECONDS);
        logger.info("Book purchase result: {}", purchaseResult);
        return purchaseResult;
    }

    public List<RestBooks> findBooksOwnedByUser(String userId) throws InterruptedException, ExecutionException, TimeoutException {
        logger.info("Finding books owned by user with ID: {}", userId);
        CompletableFuture<List<RestBooks>> future = booksRepository.findBooksOwnedByUser(userId);
        List<RestBooks> ownedBooks = future.get(10, TimeUnit.SECONDS);
        logger.info("Books owned by user: {}", ownedBooks);
        return ownedBooks;
    }

    public RestBooks getBookById(String bookId) throws InterruptedException, ExecutionException, TimeoutException {
        logger.info("Retrieving book by ID: {}", bookId);
        CompletableFuture<RestBooks> future = booksRepository.findById(bookId);
        RestBooks book = future.get(10, TimeUnit.SECONDS);
        logger.info("Book retrieved: {}", book);
        return book;
    }
}