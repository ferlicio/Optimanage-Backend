package com.AIT.Optimanage.Config;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * Scheduler responsible for rotating the JWT signing keys.
 */
@Component
@RequiredArgsConstructor
public class JwtKeyRotationScheduler {

    private final JwtService jwtService;

    /**
     * Rotate the primary and secondary JWT signing keys according to the
     * configured cron expression.
     */
    @Scheduled(cron = "${schedule.key-rotation.cron}")
    public void rotateKeys() {
        jwtService.switchKeys();
    }
}
