package com.AIT.Optimanage.Repositories;

import com.AIT.Optimanage.Models.User.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    public Optional<User> findByEmail(String email);

    long countByOrganizationIdAndAtivoTrue(Integer organizationId);

    @Modifying(clearAutomatically = true)
    @Query("update User u set u.organizationId = :organizationId where u.id = :userId")
    void updateOrganizationTenant(@Param("userId") Integer userId, @Param("organizationId") Integer organizationId);
}