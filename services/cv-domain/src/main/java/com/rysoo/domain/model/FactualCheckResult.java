package com.rysoo.domain.model;

import java.util.Collections;
import java.util.List;

public record FactualCheckResult(
    boolean passed,
    List<FactCheckViolation> violations
) {
    public static FactualCheckResult success() {
        return new FactualCheckResult(true, Collections.emptyList());
    }

    public static FactualCheckResult failure(List<FactCheckViolation> violations) {
        return new FactualCheckResult(false, violations);
    }

    public boolean isClean() {
        return passed;
    }
}
