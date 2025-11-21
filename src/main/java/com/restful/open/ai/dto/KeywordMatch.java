package com.restful.open.ai.dto;

public record KeywordMatch(
        String keyword,
        boolean present,
        String evidenceSnippet,
        int importance // 1-5
) {}
