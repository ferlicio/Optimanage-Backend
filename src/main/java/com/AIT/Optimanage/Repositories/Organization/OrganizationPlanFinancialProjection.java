package com.AIT.Optimanage.Repositories.Organization;

public interface OrganizationPlanFinancialProjection {
    Integer getPlanId();
    String getPlanName();
    Float getPlanValue();
    Integer getPlanDurationDays();
    Long getOrganizationCount();
}
