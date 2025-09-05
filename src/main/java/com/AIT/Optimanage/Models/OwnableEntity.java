package com.AIT.Optimanage.Models;

import com.AIT.Optimanage.Models.User.User;

/**
 * Contract for entities that are owned by a {@link User}.
 */
public interface OwnableEntity {
    User getOwnerUser();
    void setOwnerUser(User ownerUser);
}
