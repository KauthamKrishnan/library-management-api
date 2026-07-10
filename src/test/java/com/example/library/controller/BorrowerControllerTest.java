package com.example.library.controller;

import com.example.library.dto.BookResponse;
import com.example.library.dto.BorrowerResponse;
import com.example.library.exception.BookAlreadyBorrowedException;
import com.example.library.exception.ResourceNotFoundException;
import com.example.library.service.BookService;
import com.example.library.service.BorrowerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BorrowerController.class)
class BorrowerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BorrowerService borrowerService;

    @MockitoBean
    private BookService bookService;

    @Test
    void register_returns201_withCreatedBorrower() throws Exception {
        when(borrowerService.register(any()))
                .thenReturn(new BorrowerResponse(1L, "Alice", "alice@example.com"));

        mockMvc.perform(post("/api/v1/borrowers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Alice\",\"email\":\"alice@example.com\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("alice@example.com"));
    }

    @Test
    void register_returns400_whenEmailInvalid() throws Exception {
        mockMvc.perform(post("/api/v1/borrowers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Alice\",\"email\":\"not-an-email\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void borrow_returns200_whenSuccessful() throws Exception {
        when(bookService.borrow(5L, 1L))
                .thenReturn(new BookResponse(1L, "111", "Clean Code", "Robert Martin", false, 5L));

        mockMvc.perform(post("/api/v1/borrowers/5/borrow/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(false))
                .andExpect(jsonPath("$.borrowerId").value(5));
    }

    @Test
    void borrow_returns409_whenAlreadyBorrowed() throws Exception {
        when(bookService.borrow(5L, 1L)).thenThrow(new BookAlreadyBorrowedException(1L));

        mockMvc.perform(post("/api/v1/borrowers/5/borrow/1"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void borrow_returns404_whenBorrowerMissing() throws Exception {
        when(bookService.borrow(5L, 1L)).thenThrow(new ResourceNotFoundException("Borrower", 5L));

        mockMvc.perform(post("/api/v1/borrowers/5/borrow/1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void returnBook_returns200_whenSuccessful() throws Exception {
        when(bookService.returnBook(5L, 1L))
                .thenReturn(new BookResponse(1L, "111", "Clean Code", "Robert Martin", true, null));

        mockMvc.perform(post("/api/v1/borrowers/5/return/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(true));
    }
}
