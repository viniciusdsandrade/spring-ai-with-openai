package com.restful.open.ai.dto;

public record SectionSuggestion(
        String section,
        String currentWeakness,
        String suggestedImprovement,
        int priority // 1-5
) {}
