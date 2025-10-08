package com.AIT.Optimanage.Support;

/**
 * Helper utilities to normalise pagination parameters received from controllers.
 */
public final class PaginationUtils {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_PAGE_SIZE = 20;

    private PaginationUtils() {
        // utility class
    }

    public static int resolvePage(Integer page) {
        if (page == null || page < 0) {
            return DEFAULT_PAGE;
        }
        return page;
    }

    public static int resolvePageSize(Integer pageSize, Integer legacyPageSize) {
        Integer value = pageSize != null ? pageSize : legacyPageSize;
        if (value == null || value <= 0) {
            return DEFAULT_PAGE_SIZE;
        }
        return value;
    }
}
