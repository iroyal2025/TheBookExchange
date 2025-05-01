package edu.famu.thebookexchange.model.Rest;

public class RestPurchasedBook {
    private String title;
    private String author;
    private Double price;

    public RestPurchasedBook() {
        // Default constructor
    }

    public RestPurchasedBook(String title, String author, Double price) {
        this.title = title;
        this.author = author;
        this.price = price;
    }

    // Getters
    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public Double getPrice() {
        return price;
    }

    // Setters
    public void setTitle(String title) {
        this.title = title;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setPrice(Double price) {
        this.price = price;
    }
}