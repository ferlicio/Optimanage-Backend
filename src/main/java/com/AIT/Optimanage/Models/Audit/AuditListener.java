package com.AIT.Optimanage.Models.Audit;

import com.AIT.Optimanage.Models.AuditableEntity;
import com.AIT.Optimanage.Models.User.User;
import com.AIT.Optimanage.Security.CurrentUser;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import java.time.LocalDateTime;

/**
 * Entity listener responsible for populating audit metadata prior to persistence operations.
 */
public class AuditListener {

    @PrePersist
    public void setCreationMetadata(Object entity) {
        if (entity instanceof AuditableEntity auditable) {
            LocalDateTime now = LocalDateTime.now();
            if (auditable.getCreatedAt() == null) {
                auditable.setCreatedAt(now);
            }
            auditable.setUpdatedAt(now);

            User current = CurrentUser.get();
            if (current != null) {
                if (auditable.getCreatedBy() == null) {
                    auditable.setCreatedBy(current.getId());
                }
                auditable.setUpdatedBy(current.getId());
            }
        }
    }

    @PreUpdate
    public void setUpdateMetadata(Object entity) {
        if (entity instanceof AuditableEntity auditable) {
            auditable.setUpdatedAt(LocalDateTime.now());
            User current = CurrentUser.get();
            if (current != null) {
                auditable.setUpdatedBy(current.getId());
            }
        }
    }
}
