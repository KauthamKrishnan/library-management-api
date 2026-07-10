package com.example.library.controller;

import com.example.library.dto.BookResponse;
import com.example.library.service.BookService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookController.class)
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookService bookService;

    @Test
    void register_returns201_withCreatedBook() throws Exception {
        when(bookService.register(any()))
                .thenReturn(new BookResponse(1L, "111", "Clean Code", "Robert Martin", true, null));

        mockMvc.perform(post("/api/v1/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"isbn\":\"111\",\"title\":\"Clean Code\",\"author\":\"Robert Martin\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.available").value(true));
    }

    @Test
    void register_returns400_whenFieldsAreBlank() throws Exception {
        mockMvc.perform(post("/api/v1/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"isbn\":\"\",\"title\":\"\",\"author\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.fieldErrors").isArray());
    }

    @Test
    void listAll_returns200_withBooks() throws Exception {
        when(bookService.listAll())
                .thenReturn(List.of(new BookResponse(1L, "111", "Clean Code", "Robert Martin", true, null)));

        mockMvc.perform(get("/api/v1/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].isbn").value("111"))
                .andExpect(jsonPath("$[0].available").value(true));
    }
}
