package com.rysoo.domain.model;

public record FactCheckViolation(
    String category,
    String claimedFact,
    String explanation
) {}
