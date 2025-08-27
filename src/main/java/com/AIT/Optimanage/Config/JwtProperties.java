package com.AIT.Optimanage.Config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {

    private String primaryKey;
    private String rotationKey;
    private long expiration;
    private long refreshExpiration;

    /**
     * Switch the primary key to the rotation key.
     */
    public void switchKeys() {
        this.primaryKey = this.rotationKey;
    }
}
