package com.example.library.service;

import com.example.library.dto.BorrowerRequest;
import com.example.library.dto.BorrowerResponse;
import com.example.library.entity.Borrower;
import com.example.library.exception.DuplicateEmailException;
import com.example.library.repository.BorrowerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BorrowerServiceTest {

    @Mock
    private BorrowerRepository borrowerRepository;

    @InjectMocks
    private BorrowerService borrowerService;

    @Test
    void register_savesBorrower_whenEmailIsNew() {
        BorrowerRequest request = new BorrowerRequest("Alice", "alice@example.com");
        when(borrowerRepository.existsByEmail("alice@example.com")).thenReturn(false);
        when(borrowerRepository.save(any(Borrower.class))).thenAnswer(invocation -> {
            Borrower borrower = invocation.getArgument(0);
            borrower.setId(1L);
            return borrower;
        });

        BorrowerResponse response = borrowerService.register(request);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("Alice");
        assertThat(response.email()).isEqualTo("alice@example.com");
    }

    @Test
    void register_throwsDuplicateEmail_whenEmailAlreadyExists() {
        BorrowerRequest request = new BorrowerRequest("Alice", "alice@example.com");
        when(borrowerRepository.existsByEmail("alice@example.com")).thenReturn(true);

        assertThatThrownBy(() -> borrowerService.register(request))
                .isInstanceOf(DuplicateEmailException.class);

        verify(borrowerRepository, never()).save(any());
    }
}
