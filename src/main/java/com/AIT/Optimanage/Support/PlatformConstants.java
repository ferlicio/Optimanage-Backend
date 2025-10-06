package com.AIT.Optimanage.Support;

/**
 * Shared constants related to the Optimanage platform configuration.
 */
public final class PlatformConstants {

    private PlatformConstants() {
        // utility class
    }

    /**
     * Identifier of the platform/root organization. Users belonging to this
     * organization can perform cross-tenant management operations.
     */
    public static final Integer PLATFORM_ORGANIZATION_ID = 1;

    /**
     * Nome do plano base utilizado para organizações após o término do período trial.
     */
    public static final String VIEW_ONLY_PLAN_NAME = "Somente visualização";

}

