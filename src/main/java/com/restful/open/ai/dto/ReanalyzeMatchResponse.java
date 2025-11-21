package com.restful.open.ai.dto;

import java.util.List;

public record ReanalyzeMatchResponse(
        int oldScore,
        int newScore,
        String scoreExplanation,
        List<KeywordMatch> keywords,
        List<SectionSuggestion> suggestions
) {
}