package com.AIT.Optimanage.Auth;

import java.time.Instant;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RefreshTokenCleanupScheduler {

    private final RefreshTokenRepository refreshTokenRepository;

    @Scheduled(cron = "${schedule.token-cleanup.cron}")
    public void removeExpiredTokens() {
        refreshTokenRepository.deleteByExpiryDateBefore(Instant.now());
    }
}

