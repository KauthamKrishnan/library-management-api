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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private BorrowerRepository borrowerRepository;

    @InjectMocks
    private BookService bookService;

    private Borrower borrower(Long id, String name, String email) {
        Borrower borrower = new Borrower(name, email);
        borrower.setId(id);
        return borrower;
    }

    private Book book(Long id, String isbn, String title, String author) {
        Book book = new Book(isbn, title, author);
        book.setId(id);
        return book;
    }

    @Test
    void register_savesNewBook_whenIsbnIsNew() {
        BookRequest request = new BookRequest("111", "Clean Code", "Robert Martin");
        when(bookRepository.findFirstByIsbn("111")).thenReturn(Optional.empty());
        when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> {
            Book book = invocation.getArgument(0);
            book.setId(1L);
            return book;
        });

        BookResponse response = bookService.register(request);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.available()).isTrue();
        assertThat(response.borrowerId()).isNull();
    }

    @Test
    void register_savesNewCopy_whenIsbnMatchesTitleAndAuthor() {
        BookRequest request = new BookRequest("111", "Clean Code", "Robert Martin");
        when(bookRepository.findFirstByIsbn("111")).thenReturn(Optional.of(book(1L, "111", "Clean Code", "Robert Martin")));
        when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> {
            Book book = invocation.getArgument(0);
            book.setId(2L);
            return book;
        });

        BookResponse response = bookService.register(request);

        assertThat(response.id()).isEqualTo(2L);
    }

    @Test
    void register_throwsIsbnConflict_whenTitleOrAuthorDiffers() {
        BookRequest request = new BookRequest("111", "Different Title", "Robert Martin");
        when(bookRepository.findFirstByIsbn("111")).thenReturn(Optional.of(book(1L, "111", "Clean Code", "Robert Martin")));

        assertThatThrownBy(() -> bookService.register(request))
                .isInstanceOf(IsbnConflictException.class);

        verify(bookRepository, never()).save(any());
    }

    @Test
    void listAll_returnsMappedResponses() {
        when(bookRepository.findAll()).thenReturn(List.of(book(1L, "111", "Clean Code", "Robert Martin")));

        List<BookResponse> responses = bookService.listAll();

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).isbn()).isEqualTo("111");
        assertThat(responses.get(0).available()).isTrue();
    }

    @Test
    void borrow_setsBorrower_whenAvailable() {
        Borrower alice = borrower(5L, "Alice", "alice@example.com");
        Book book = book(1L, "111", "Clean Code", "Robert Martin");
        when(borrowerRepository.findById(5L)).thenReturn(Optional.of(alice));
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BookResponse response = bookService.borrow(5L, 1L);

        assertThat(response.available()).isFalse();
        assertThat(response.borrowerId()).isEqualTo(5L);
    }

    @Test
    void borrow_throwsAlreadyBorrowed_whenHeldByAnother() {
        Borrower alice = borrower(5L, "Alice", "alice@example.com");
        Book book = book(1L, "111", "Clean Code", "Robert Martin");
        book.setBorrower(borrower(6L, "Bob", "bob@example.com"));
        when(borrowerRepository.findById(5L)).thenReturn(Optional.of(alice));
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));

        assertThatThrownBy(() -> bookService.borrow(5L, 1L))
                .isInstanceOf(BookAlreadyBorrowedException.class);

        verify(bookRepository, never()).save(any());
    }

    @Test
    void borrow_throwsNotFound_whenBorrowerMissing() {
        when(borrowerRepository.findById(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.borrow(5L, 1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void borrow_throwsNotFound_whenBookMissing() {
        when(borrowerRepository.findById(5L)).thenReturn(Optional.of(borrower(5L, "Alice", "alice@example.com")));
        when(bookRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.borrow(5L, 1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void returnBook_clearsBorrower_whenHeldByBorrower() {
        Borrower alice = borrower(5L, "Alice", "alice@example.com");
        Book book = book(1L, "111", "Clean Code", "Robert Martin");
        book.setBorrower(alice);
        when(borrowerRepository.findById(5L)).thenReturn(Optional.of(alice));
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BookResponse response = bookService.returnBook(5L, 1L);

        assertThat(response.available()).isTrue();
        assertThat(response.borrowerId()).isNull();
    }

    @Test
    void returnBook_throwsNotBorrowed_whenAvailable() {
        Borrower alice = borrower(5L, "Alice", "alice@example.com");
        Book book = book(1L, "111", "Clean Code", "Robert Martin");
        when(borrowerRepository.findById(5L)).thenReturn(Optional.of(alice));
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));

        assertThatThrownBy(() -> bookService.returnBook(5L, 1L))
                .isInstanceOf(BookNotBorrowedException.class);
    }

    @Test
    void returnBook_throwsNotBorrowedByBorrower_whenHeldByAnother() {
        Borrower alice = borrower(5L, "Alice", "alice@example.com");
        Book book = book(1L, "111", "Clean Code", "Robert Martin");
        book.setBorrower(borrower(6L, "Bob", "bob@example.com"));
        when(borrowerRepository.findById(5L)).thenReturn(Optional.of(alice));
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));

        assertThatThrownBy(() -> bookService.returnBook(5L, 1L))
                .isInstanceOf(BookNotBorrowedByBorrowerException.class);
    }
}
