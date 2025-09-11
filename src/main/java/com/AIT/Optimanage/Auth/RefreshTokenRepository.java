package com.AIT.Optimanage.Auth;

import com.AIT.Optimanage.Models.User.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Integer> {
    Optional<RefreshToken> findByToken(String token);
    void deleteByUser(User user);
    void deleteByExpiryDateBefore(Instant expiryDate);

    @Modifying(clearAutomatically = true)
    @Query("update RefreshToken t set t.revoked = true where t.token = :token and t.revoked = false")
    int revokeIfNotRevoked(@Param("token") String token);
}