package com.AIT.Optimanage.Repositories.Organization;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.AIT.Optimanage.Models.Organization.TrialType;

public interface OrganizationOnboardingProjection {
    LocalDateTime getCreatedAt();
    LocalDate getDataAssinatura();
    LocalDate getTrialInicio();
    LocalDate getTrialFim();
    TrialType getTrialTipo();
}
