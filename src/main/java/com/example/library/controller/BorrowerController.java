package com.example.library.controller;

import com.example.library.dto.BookResponse;
import com.example.library.dto.BorrowerRequest;
import com.example.library.dto.BorrowerResponse;
import com.example.library.service.BookService;
import com.example.library.service.BorrowerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/borrowers")
public class BorrowerController {

    private final BorrowerService borrowerService;
    private final BookService bookService;

    public BorrowerController(BorrowerService borrowerService, BookService bookService) {
        this.borrowerService = borrowerService;
        this.bookService = bookService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BorrowerResponse register(@Valid @RequestBody BorrowerRequest request) {
        return borrowerService.register(request);
    }

    @PostMapping("/{borrowerId}/borrow/{bookId}")
    public BookResponse borrow(@PathVariable Long borrowerId, @PathVariable Long bookId) {
        return bookService.borrow(borrowerId, bookId);
    }

    @PostMapping("/{borrowerId}/return/{bookId}")
    public BookResponse returnBook(@PathVariable Long borrowerId, @PathVariable Long bookId) {
        return bookService.returnBook(borrowerId, bookId);
    }
}
