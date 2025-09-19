package com.AIT.Optimanage.Repositories.Organization;

import com.AIT.Optimanage.Models.Organization.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, Integer> {

    @Modifying
    @Query("update Organization o set o.organizationId = :organizationId where o.id = :organizationId")
    void updateOrganizationId(Integer organizationId);
}

