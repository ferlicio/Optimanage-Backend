package com.AIT.Optimanage.Repositories.Organization;

import com.AIT.Optimanage.Models.Organization.UserInvite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserInviteRepository extends JpaRepository<UserInvite, Integer> {
    Optional<UserInvite> findByCode(String code);
}
