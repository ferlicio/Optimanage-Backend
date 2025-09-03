package com.AIT.Optimanage.Models.Audit;

import com.AIT.Optimanage.Models.User.User;
import com.AIT.Optimanage.Support.CurrentUser;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

import java.lang.reflect.Method;

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
        try {
            Method getOwner = entity.getClass().getMethod("getOwnerUser");
            Object owner = getOwner.invoke(entity);
            if (owner == null) {
                Method setOwner = entity.getClass().getMethod("setOwnerUser", User.class);
                User current = CurrentUser.get();
                if (current != null) {
                    setOwner.invoke(entity, current);
                }
            }
        } catch (Exception e) {
            // ignore entities without ownerUser or reflection issues
        }
    }
}
