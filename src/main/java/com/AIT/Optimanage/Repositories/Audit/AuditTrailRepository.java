package com.AIT.Optimanage.Repositories.Audit;

import com.AIT.Optimanage.Models.Audit.AuditTrail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditTrailRepository extends JpaRepository<AuditTrail, Integer> {
}
