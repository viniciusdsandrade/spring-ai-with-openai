package com.restful.open.ai.service;

import com.restful.open.ai.dto.ResumeMatchAnalysis;

public interface MatchAnalysisService {
    ResumeMatchAnalysis analyze(String normalizedJobDescription, String normalizedResumeText);
}
