package com.AIT.Optimanage.Config;

import lombok.Getter;
import lombok.Setter;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {

    private final Map<String, String> keys = new HashMap<>();
    private String primaryKeyId;
    private String rotationKeyId;
    private long expiration;
    private long refreshExpiration;

    /**
     * Retrieve the key associated with the provided key id.
     *
     * @param kid the identifier of the key
     * @return the secret associated with the given id
     */
    public String getKey(String kid) {
        return keys.get(kid);
    }

    /**
     * Retrieve the currently configured primary key.
     *
     * @return the primary signing key
     */
    public String getPrimaryKey() {
        return getKey(primaryKeyId);
    }

    /**
     * Configure a new rotation key that will become the primary key after the
     * next scheduled rotation.
     *
     * @param kid the identifier for the rotation key
     * @param key the key to use for the next rotation
     */
    public void configureRotationKey(String kid, String key) {
        this.rotationKeyId = kid;
        this.keys.put(kid, key);
    }

    /**
     * Swap the current primary and rotation keys. The previous primary key
     * becomes the new rotation key so it can be reused if necessary.
     */
    public void switchKeys() {
        String previousPrimary = this.primaryKeyId;
        this.primaryKeyId = this.rotationKeyId;
        this.rotationKeyId = previousPrimary;
    }

}
