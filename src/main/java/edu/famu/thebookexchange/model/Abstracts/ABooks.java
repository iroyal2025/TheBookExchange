// edu.famu.thebookexchange.model.Abstracts.ABooks.java
package edu.famu.thebookexchange.model.Abstracts;

public abstract class ABooks {

        private String title;
        private String author;
        private String edition;
        private String ISBN;
        private String condition;
        private String description;
        private double price;
        private boolean isDigital;
        private String digitalCopyPath;

        public ABooks() {
                this.title = "";
                this.author = "";
                this.edition = "";
                this.ISBN = "";
                this.condition = "";
                this.description = "";
                this.price = 0.0;
                this.isDigital = false;
                this.digitalCopyPath = "";
        }

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

        public String getTitle() {
                return title;
        }

        public void setTitle(String title) {
                this.title = title;
        }

        public String getAuthor() {
                return author;
        }

        public void setAuthor(String author) {
                this.author = author;
        }

        public String getEdition() {
                return edition;
        }

        public void setEdition(String edition) {
                this.edition = edition;
        }

        public String getISBN() {
                return ISBN;
        }

        public void setISBN(String ISBN) {
                this.ISBN = ISBN;
        }

        public String getCondition() {
                return condition;
        }

        public void setCondition(String condition) {
                this.condition = condition;
        }

        public String getDescription() {
                return description;
        }

        public void setDescription(String description) {
                this.description = description;
        }

        public double getPrice() {
                return price;
        }

        public void setPrice(double price) {
                this.price = price;
        }

        public boolean isDigital() {
                return isDigital;
        }

        public void setDigital(boolean digital) {
                isDigital = digital;
        }

        public String getDigitalCopyPath() {
                return digitalCopyPath;
        }

        public void setDigitalCopyPath(String digitalCopyPath) {
                this.digitalCopyPath = digitalCopyPath;
        }
}