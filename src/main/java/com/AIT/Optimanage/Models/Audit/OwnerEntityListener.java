package com.AIT.Optimanage.Models.Audit;

import com.AIT.Optimanage.Models.OwnableEntity;
import com.AIT.Optimanage.Models.User.User;
import com.AIT.Optimanage.Support.CurrentUser;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

public class OwnerEntityListener {

    @PrePersist
    public void prePersist(Object entity) {
        setOwnerUser(entity);
    }

    @PreUpdate
    public void preUpdate(Object entity) {
        setOwnerUser(entity);
    }

    private void setOwnerUser(Object entity) {
        if (entity instanceof OwnableEntity) {
            OwnableEntity ownable = (OwnableEntity) entity;
            if (ownable.getOwnerUser() == null) {
                User current = CurrentUser.get();
                if (current != null) {
                    ownable.setOwnerUser(current);
                }
            }
        }
    }
}
