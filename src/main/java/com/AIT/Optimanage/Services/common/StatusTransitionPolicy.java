package com.AIT.Optimanage.Services.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

public class StatusTransitionPolicy<S extends Enum<S>, C> {

    private final String entityLabel;
    private final Map<S, List<TransitionConstraint<S, C>>> constraints;

    private StatusTransitionPolicy(String entityLabel, Map<S, List<TransitionConstraint<S, C>>> constraints) {
        this.entityLabel = entityLabel;
        this.constraints = constraints;
    }

    public void validate(S currentStatus, S targetStatus, C context) {
        Objects.requireNonNull(currentStatus, "O status atual não pode ser nulo.");
        Objects.requireNonNull(targetStatus, "O status de destino não pode ser nulo.");
        if (currentStatus == targetStatus) {
            throw new IllegalStateException("A " + entityLabel + " já está neste status.");
        }

        List<TransitionConstraint<S, C>> targetConstraints = constraints.get(targetStatus);
        if (targetConstraints == null) {
            throw new IllegalArgumentException("Status desconhecido.");
        }

        for (TransitionConstraint<S, C> constraint : targetConstraints) {
            constraint.validate(currentStatus, targetStatus, context);
        }
    }

    public static <S extends Enum<S>, C> Builder<S, C> builder(Class<S> enumType, String entityLabel) {
        return new Builder<>(enumType, entityLabel);
    }

    public static final class Builder<S extends Enum<S>, C> {
        private final EnumMap<S, List<TransitionConstraint<S, C>>> constraints;
        private final String entityLabel;

        private Builder(Class<S> enumType, String entityLabel) {
            this.constraints = new EnumMap<>(enumType);
            this.entityLabel = entityLabel;
        }

        @SafeVarargs
        public final Builder<S, C> forTarget(S targetStatus, TransitionConstraint<S, C>... targetConstraints) {
            List<TransitionConstraint<S, C>> constraintsList = new ArrayList<>();
            if (targetConstraints != null && targetConstraints.length > 0) {
                constraintsList.addAll(Arrays.asList(targetConstraints));
            }
            this.constraints.put(targetStatus, Collections.unmodifiableList(constraintsList));
            return this;
        }

        public StatusTransitionPolicy<S, C> build() {
            return new StatusTransitionPolicy<>(entityLabel, Collections.unmodifiableMap(constraints));
        }
    }

    public interface TransitionCondition<S extends Enum<S>, C> {
        boolean test(S currentStatus, S targetStatus, C context);
    }

    public static class TransitionConstraint<S extends Enum<S>, C> {
        private final TransitionCondition<S, C> condition;
        private final Supplier<String> messageSupplier;

        private TransitionConstraint(TransitionCondition<S, C> condition, Supplier<String> messageSupplier) {
            this.condition = condition;
            this.messageSupplier = messageSupplier;
        }

        private void validate(S currentStatus, S targetStatus, C context) {
            if (!condition.test(currentStatus, targetStatus, context)) {
                throw new IllegalStateException(messageSupplier.get());
            }
        }
    }

    public static <S extends Enum<S>, C> TransitionConstraint<S, C> allowFrom(Set<S> allowedStatuses, String message) {
        Set<S> immutableAllowed = Set.copyOf(allowedStatuses);
        return new TransitionConstraint<>((current, target, context) -> immutableAllowed.contains(current), () -> message);
    }

    public static <S extends Enum<S>, C> TransitionConstraint<S, C> forbidFrom(Set<S> forbiddenStatuses, String message) {
        Set<S> immutableForbidden = Set.copyOf(forbiddenStatuses);
        return new TransitionConstraint<>((current, target, context) -> !immutableForbidden.contains(current), () -> message);
    }

    public static <S extends Enum<S>, C> TransitionConstraint<S, C> requireCondition(TransitionCondition<S, C> condition, String message) {
        return new TransitionConstraint<>(condition, () -> message);
    }

    public static <S extends Enum<S>, C> TransitionConstraint<S, C> impossible(String message) {
        return new TransitionConstraint<>((current, target, context) -> false, () -> message);
    }
}
