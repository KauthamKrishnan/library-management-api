package com.example.library.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A library member who can borrow books. Email is unique and identifies a borrower.
 */
@Entity
@Table(name = "borrowers", uniqueConstraints = @UniqueConstraint(name = "uk_borrower_email", columnNames = "email"))
@Getter
@Setter
@NoArgsConstructor
public class Borrower {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String name;

    @Email
    @NotBlank
    @Column(nullable = false, unique = true)
    private String email;

    public Borrower(String name, String email) {
        this.name = name;
        this.email = email;
    }
}
