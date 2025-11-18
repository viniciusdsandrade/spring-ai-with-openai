package com.restful.open.ai.service;

import com.restful.open.ai.dto.EmailDraft;

public interface EmailDraftService {
    EmailDraft createDraft(
            String jobDescription,
            String candidateName,
            String candidateEmail,
            int matchScore,
            String language
    );
}
