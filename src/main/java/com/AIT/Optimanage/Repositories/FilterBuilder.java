package com.AIT.Optimanage.Repositories;

import java.util.function.Function;
import org.springframework.data.jpa.domain.Specification;

/**
 * Utilitário para compor Specifications de forma fluida e condicional.
 * @param <T> entidade da especificação
 */
public class FilterBuilder<T> {
    private Specification<T> spec;

    private FilterBuilder(Specification<T> base) {
        this.spec = base;
    }

    public static <T> FilterBuilder<T> of(Specification<T> base) {
        return new FilterBuilder<>(base);
    }

    public <V> FilterBuilder<T> and(V value, Function<V, Specification<T>> mapper) {
        if (value != null) {
            spec = spec.and(mapper.apply(value));
        }
        return this;
    }

    public Specification<T> build() {
        return spec;
    }
}
