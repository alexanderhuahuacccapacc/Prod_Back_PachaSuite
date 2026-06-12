package com.pachasuite.api.repository;

import com.pachasuite.api.entities.IdempotencyLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface IdempotencyRepository extends JpaRepository<IdempotencyLog, Long> {
    Optional<IdempotencyLog> findByIdempotencyKey(String key);
    void deleteByExpiresAtBefore(LocalDateTime date);
}