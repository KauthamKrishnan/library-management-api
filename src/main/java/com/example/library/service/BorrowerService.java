package com.example.library.service;

import com.example.library.dto.BorrowerRequest;
import com.example.library.dto.BorrowerResponse;
import com.example.library.entity.Borrower;
import com.example.library.exception.DuplicateEmailException;
import com.example.library.repository.BorrowerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BorrowerService {

    private final BorrowerRepository borrowerRepository;

    public BorrowerService(BorrowerRepository borrowerRepository) {
        this.borrowerRepository = borrowerRepository;
    }

    @Transactional
    public BorrowerResponse register(BorrowerRequest request) {
        if (borrowerRepository.existsByEmail(request.email())) {
            throw new DuplicateEmailException(request.email());
        }
        Borrower saved = borrowerRepository.save(new Borrower(request.name(), request.email()));
        return toResponse(saved);
    }

    private BorrowerResponse toResponse(Borrower borrower) {
        return new BorrowerResponse(borrower.getId(), borrower.getName(), borrower.getEmail());
    }
}
