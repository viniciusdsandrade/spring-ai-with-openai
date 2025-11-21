package com.restful.open.ai.dto;

import java.util.List;

public record AnalyzeMatchResponse(
        int score,
        String scoreExplanation,
        List<KeywordMatch> keywords,
        List<SectionSuggestion> suggestions
) {
}