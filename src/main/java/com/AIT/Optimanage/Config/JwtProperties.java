package com.AIT.Optimanage.Config;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Component
@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {

    private String primaryKey;
    private String rotationKey;
    private long expiration;
    private long refreshExpiration;

    /**
     * Configure a new rotation key that will become the primary key after the
     * next scheduled rotation.
     *
     * @param newRotationKey the key to use for the next rotation
     */
    public void configureRotationKey(String newRotationKey) {
        this.rotationKey = newRotationKey;
    }

    /**
     * Swap the current primary and rotation keys. The previous primary key
     * becomes the new rotation key so it can be reused if necessary.
     */
    public void switchKeys() {
        String previousPrimary = this.primaryKey;
        this.primaryKey = this.rotationKey;
        this.rotationKey = previousPrimary;
    }
    
}
