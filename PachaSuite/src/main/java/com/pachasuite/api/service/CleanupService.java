package com.pachasuite.api.service;

import com.pachasuite.api.repository.IdempotencyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

@Component
@EnableScheduling
@RequiredArgsConstructor
public class CleanupService {

    private final IdempotencyRepository idempotencyRepository;

    @Scheduled(cron = "0 0 2 * * *")
    public void cleanExpiredIdempotencyKeys() {
        idempotencyRepository.deleteByExpiresAtBefore(LocalDateTime.now());
        System.out.println("Logs de idempotencia expirados eliminados");
    }
}