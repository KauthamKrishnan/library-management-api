package com.example.library.repository;

import com.example.library.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Long> {

    /**
     * Returns any existing copy registered under the given ISBN. Used to enforce that all
     * copies of an ISBN share the same title and author.
     */
    Optional<Book> findFirstByIsbn(String isbn);
}
