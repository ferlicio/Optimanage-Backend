package com.AIT.Optimanage.Support;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PaginationUtilsTest {

    @Nested
    @DisplayName("resolvePage")
    class ResolvePage {

        @Test
        @DisplayName("deve manter valores de página não negativos")
        void shouldKeepNonNegativePages() {
            assertThat(PaginationUtils.resolvePage(0)).isZero();
            assertThat(PaginationUtils.resolvePage(3)).isEqualTo(3);
        }

        @Test
        @DisplayName("deve retornar padrão quando página for nula ou negativa")
        void shouldReturnDefaultForNullOrNegative() {
            assertThat(PaginationUtils.resolvePage(null)).isZero();
            assertThat(PaginationUtils.resolvePage(-1)).isZero();
        }
    }

    @Nested
    @DisplayName("resolvePageSize")
    class ResolvePageSize {

        @Test
        @DisplayName("deve priorizar pageSize moderno quando válido")
        void shouldPreferModernPageSize() {
            assertThat(PaginationUtils.resolvePageSize(25, 50)).isEqualTo(25);
        }

        @Test
        @DisplayName("deve usar pagesize legado quando pageSize for nulo")
        void shouldFallbackToLegacyWhenModernMissing() {
            assertThat(PaginationUtils.resolvePageSize(null, 30)).isEqualTo(30);
        }

        @Test
        @DisplayName("deve retornar padrão quando valores inválidos")
        void shouldReturnDefaultWhenInvalid() {
            assertThat(PaginationUtils.resolvePageSize(null, null)).isEqualTo(20);
            assertThat(PaginationUtils.resolvePageSize(0, null)).isEqualTo(20);
            assertThat(PaginationUtils.resolvePageSize(-5, 0)).isEqualTo(20);
        }
    }
}
