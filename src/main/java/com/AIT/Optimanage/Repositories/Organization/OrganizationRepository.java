package com.AIT.Optimanage.Repositories.Organization;

import com.AIT.Optimanage.Models.Organization.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, Integer> {

    @Modifying(clearAutomatically = true)
    @Query("update Organization o set o.organizationId = :organizationId where o.id = :organizationId")
    void updateOrganizationTenant(@Param("organizationId") Integer organizationId);

    @Query("""
            SELECT o
            FROM Organization o
            LEFT JOIN Venda v ON v.organizationId = o.id AND v.dataEfetuacao >= :cutoff
            WHERE (:excludedOrganizationId IS NULL OR o.id <> :excludedOrganizationId)
              AND v.id IS NULL
            """)
    List<Organization> findOrganizationsWithoutSalesSince(@Param("cutoff") LocalDate cutoff,
                                                          @Param("excludedOrganizationId") Integer excludedOrganizationId);
}

