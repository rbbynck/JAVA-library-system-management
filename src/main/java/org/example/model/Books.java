package org.example.model;

import jakarta.persistence.*;

@Entity
public class Books {

    @Id
    @Column(name = "bookID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "bookTitle")
    private String bookTitle;
    @Column(name = "bookAuthor")
    private String bookAuthor;
    @Column(name = "bookSubject")
    private String bookSubject;
    @Column(name = "bookAvailability")
    private String bookAvailability;

    public Books(Long id, String bookTitle, String bookAuthor, String bookSubject, String bookAvailability) {
        this.id = id;
        this.bookTitle = bookTitle;
        this.bookAuthor = bookAuthor;
        this.bookSubject = bookSubject;
        this.bookAvailability = bookAvailability;
    }

    public Books(String bookTitle, String bookAuthor, String bookSubject, String bookAvailability) {
        this.bookTitle = bookTitle;
        this.bookAuthor = bookAuthor;
        this.bookSubject = bookSubject;
        this.bookAvailability = bookAvailability;
    }

    public Long getId() {
        return id;
    }

    public String getBookTitle() {
        return bookTitle;
    }

    public void setBookTitle(String bookTitle) {
        this.bookTitle = bookTitle;
    }

    public String getBookAuthor() {
        return bookAuthor;
    }

    public void setBookAuthor(String bookAuthor) {
        this.bookAuthor = bookAuthor;
    }

    public String getBookSubject() {
        return bookSubject;
    }

    public void setBookSubject(String bookSubject) {
        this.bookSubject = bookSubject;
    }

    public String getBookAvailability() {
        return bookAvailability;
    }

    public void setBookAvailability(String bookAvailability) {
        this.bookAvailability = bookAvailability;
    }

    @Override
    public String toString() {
        return "Books{" +
                "id=" + id +
                ", bookTitle='" + bookTitle + '\'' +
                ", bookAuthor='" + bookAuthor + '\'' +
                ", bookSubject='" + bookSubject + '\'' +
                ", bookAvailability='" + bookAvailability + '\'' +
                '}';
    }
}

