package com.AIT.Optimanage.Repositories;

import com.AIT.Optimanage.Models.User.User;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    public Optional<User> findByEmail(String email);

    long countByOrganizationIdAndAtivoTrue(Integer organizationId);

    Page<User> findAllByOrganizationId(Integer organizationId, Pageable pageable);

    Optional<User> findByIdAndOrganizationId(Integer id, Integer organizationId);

    @Modifying(clearAutomatically = true)
    @Query("update User u set u.organizationId = :organizationId where u.id = :userId")
    void updateOrganizationTenant(@Param("userId") Integer userId, @Param("organizationId") Integer organizationId);
}