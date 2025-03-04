package edu.famu.thebookexchange.model.Abstracts;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Data
@NoArgsConstructor
public abstract class ABooks {
        @JsonProperty("title")
        private String title;

        @JsonProperty("author")
        private String author;

        @JsonProperty("edition")
        private String edition;

        @JsonProperty("ISBN")
        private String ISBN;

        @JsonProperty("condition")
        private String condition;

        @JsonProperty("description")
        private String description;

        @JsonProperty("price")
        private double price;

        @JsonProperty("is_digital")
        private boolean isDigital;

        @JsonProperty("digital_copy_path")
        private String digitalCopyPath;

        public ABooks(String title, String author, String edition, String ISBN, String condition, String description, double price, boolean isDigital, String digitalCopyPath) {
                this.title = title;
                this.author = author;
                this.edition = edition;
                this.ISBN = ISBN;
                this.condition = condition;
                this.description = description;
                this.price = price;
                this.isDigital = isDigital;
                this.digitalCopyPath = digitalCopyPath;
        }

        // Manual Getters
        public String getTitle() {
                return title;
        }

        public String getAuthor() {
                return author;
        }

        public String getEdition() {
                return edition;
        }

        public String getISBN() {
                return ISBN;
        }

        public String getCondition() {
                return condition;
        }

        public String getDescription() {
                return description;
        }

        public double getPrice() {
                return price;
        }

        public boolean isDigital() {
                return isDigital;
        }

        public String getDigitalCopyPath() {
                return digitalCopyPath;
        }

        // Manual Setters (if needed, although @Setter already provides them)
        public void setTitle(String title) {
                this.title = title;
        }

        public void setAuthor(String author) {
                this.author = author;
        }

        public void setEdition(String edition) {
                this.edition = edition;
        }

        public void setISBN(String ISBN) {
                this.ISBN = ISBN;
        }

        public void setCondition(String condition) {
                this.condition = condition;
        }

        public void setDescription(String description) {
                this.description = description;
        }

        public void setPrice(double price) {
                this.price = price;
        }

        public void setDigital(boolean isDigital) {
                this.isDigital = isDigital;
        }

        public void setDigitalCopyPath(String digitalCopyPath) {
                this.digitalCopyPath = digitalCopyPath;
        }
}