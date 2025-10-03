package com.AIT.Optimanage.Repositories.Organization;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface OrganizationOnboardingProjection {
    LocalDateTime getCreatedAt();
    LocalDate getDataAssinatura();
}
