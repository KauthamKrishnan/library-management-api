package com.example.library;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Full test tests that run the whole stack.
 */
@SpringBootTest
@AutoConfigureMockMvc
class LibraryIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void borrowAndReturnFlow_worksEndToEnd() throws Exception {
        Long borrowerId = createBorrower("Alice", "alice-it@example.com");
        Long bookId = createBook("978-0001", "Clean Code", "Robert Martin");

        mockMvc.perform(get("/api/v1/books"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/borrowers/{borrowerId}/borrow/{bookId}", borrowerId, bookId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(false))
                .andExpect(jsonPath("$.borrowerId").value(borrowerId));

        // A second borrow of the same copy must be rejected.
        mockMvc.perform(post("/api/v1/borrowers/{borrowerId}/borrow/{bookId}", borrowerId, bookId))
                .andExpect(status().isConflict());

        mockMvc.perform(post("/api/v1/borrowers/{borrowerId}/return/{bookId}", borrowerId, bookId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(true));

        // Returning an already returned copy must be rejected.
        mockMvc.perform(post("/api/v1/borrowers/{borrowerId}/return/{bookId}", borrowerId, bookId))
                .andExpect(status().isConflict());
    }

    @Test
    void duplicateEmail_returns409() throws Exception {
        createBorrower("Bob", "bob-it@example.com");

        mockMvc.perform(post("/api/v1/borrowers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Bob Two\",\"email\":\"bob-it@example.com\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void missingBorrower_returns404_onBorrow() throws Exception {
        Long bookId = createBook("978-0404", "The Pragmatic Programmer", "Andy Hunt");

        mockMvc.perform(post("/api/v1/borrowers/{borrowerId}/borrow/{bookId}", 999999, bookId))
                .andExpect(status().isNotFound());
    }

    @Test
    void sameIsbn_getsDifferentId_andConflictingIsbn_returns409() throws Exception {
        Long firstId = createBook("978-SAME", "Domain-Driven Design", "Eric Evans");
        Long secondId = createBook("978-SAME", "Domain-Driven Design", "Eric Evans");

        // Two copies of the same ISBN are distinct books with different ids.
        assertThat(secondId).isNotEqualTo(firstId);

        // Same ISBN with a different title/author is a conflict.
        mockMvc.perform(post("/api/v1/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"isbn\":\"978-SAME\",\"title\":\"Another Title\",\"author\":\"Eric Evans\"}"))
                .andExpect(status().isConflict());
    }

    private Long createBorrower(String name, String email) throws Exception {
        String body = mockMvc.perform(post("/api/v1/borrowers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"" + name + "\",\"email\":\"" + email + "\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).get("id").asLong();
    }

    private Long createBook(String isbn, String title, String author) throws Exception {
        String body = mockMvc.perform(post("/api/v1/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"isbn\":\"" + isbn + "\",\"title\":\"" + title + "\",\"author\":\"" + author + "\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).get("id").asLong();
    }
}
