package com.AIT.Optimanage.Models;

/**
 * Contract for entities that belong to a specific organization.
 */
public interface OwnableEntity {
    Integer getOrganizationId();
    void setOrganizationId(Integer organizationId);
}
