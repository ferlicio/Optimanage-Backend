package com.AIT.Optimanage.Repositories.Organization;

import com.AIT.Optimanage.Models.Organization.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, Integer> {
}

