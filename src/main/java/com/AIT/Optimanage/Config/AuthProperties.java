package com.AIT.Optimanage.Config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "auth")
public class AuthProperties {
    private int maxFailedAttempts = 5;
    private long lockoutMinutes = 15;
    private long resetCodeExpirySeconds = 600;
}
