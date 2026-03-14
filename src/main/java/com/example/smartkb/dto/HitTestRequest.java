package com.example.smartkb.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record HitTestRequest(
        @NotNull String queryText,
        @Positive int topN,
        double threshold
) {
    public int topN() {
        return topN > 0 ? topN : 5;
    }

    public double threshold() {
        return threshold >= 0 && threshold <= 1 ? threshold : 0.5;
    }
}
