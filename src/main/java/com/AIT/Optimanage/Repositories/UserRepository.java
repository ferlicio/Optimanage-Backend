package com.AIT.Optimanage.Repositories;

import com.AIT.Optimanage.Models.User.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    public Optional<User> findByEmail(String email);

    long countByOrganizationIdAndAtivoTrue(Integer organizationId);
}