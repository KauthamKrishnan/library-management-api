package com.example.library.service;

import com.example.library.dto.BookRequest;
import com.example.library.dto.BookResponse;
import com.example.library.entity.Book;
import com.example.library.entity.Borrower;
import com.example.library.exception.BookAlreadyBorrowedException;
import com.example.library.exception.BookNotBorrowedByBorrowerException;
import com.example.library.exception.BookNotBorrowedException;
import com.example.library.exception.IsbnConflictException;
import com.example.library.exception.ResourceNotFoundException;
import com.example.library.repository.BookRepository;
import com.example.library.repository.BorrowerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BookService {

    private final BookRepository bookRepository;
    private final BorrowerRepository borrowerRepository;

    public BookService(BookRepository bookRepository, BorrowerRepository borrowerRepository) {
        this.bookRepository = bookRepository;
        this.borrowerRepository = borrowerRepository;
    }

    /**
     * Registers a new copy. Every copy gets its own id, even when it shares an ISBN with
     * existing copies. If the ISBN already exists, the title and author must match.
     */
    @Transactional
    public BookResponse register(BookRequest request) {
        bookRepository.findFirstByIsbn(request.isbn()).ifPresent(existing -> {
            if (!existing.getTitle().equals(request.title())
                    || !existing.getAuthor().equals(request.author())) {
                throw new IsbnConflictException(request.isbn(), existing.getTitle(), existing.getAuthor());
            }
        });
        Book saved = bookRepository.save(new Book(request.isbn(), request.title(), request.author()));
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<BookResponse> listAll() {
        return bookRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional
    public BookResponse borrow(Long borrowerId, Long bookId) {
        Borrower borrower = getBorrower(borrowerId);
        Book book = getBook(bookId);
        if (!book.isAvailable()) {
            throw new BookAlreadyBorrowedException(bookId);
        }
        book.setBorrower(borrower);
        return toResponse(bookRepository.save(book));
    }

    @Transactional
    public BookResponse returnBook(Long borrowerId, Long bookId) {
        Borrower borrower = getBorrower(borrowerId);
        Book book = getBook(bookId);
        if (book.isAvailable()) {
            throw new BookNotBorrowedException(bookId);
        }
        if (!book.getBorrower().getId().equals(borrower.getId())) {
            throw new BookNotBorrowedByBorrowerException(bookId, borrowerId);
        }
        book.setBorrower(null);
        return toResponse(bookRepository.save(book));
    }

    private Borrower getBorrower(Long borrowerId) {
        return borrowerRepository.findById(borrowerId)
                .orElseThrow(() -> new ResourceNotFoundException("Borrower", borrowerId));
    }

    private Book getBook(Long bookId) {
        return bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book", bookId));
    }

    private BookResponse toResponse(Book book) {
        Long borrowerId = book.isAvailable() ? null : book.getBorrower().getId();
        return new BookResponse(
                book.getId(),
                book.getIsbn(),
                book.getTitle(),
                book.getAuthor(),
                book.isAvailable(),
                borrowerId);
    }
}
