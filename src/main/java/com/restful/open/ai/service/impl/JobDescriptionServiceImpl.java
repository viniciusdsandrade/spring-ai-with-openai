package com.restful.open.ai.service.impl;

import com.restful.open.ai.service.JobDescriptionService;
import org.springframework.stereotype.Service;

@Service
public class JobDescriptionServiceImpl implements JobDescriptionService {

    @Override
    public String normalize(String rawJobDescription) {
        if (rawJobDescription == null) return "";
        String cleaned = rawJobDescription.replaceAll("\\s+", " ").trim();
        int maxLength = 15_000;
        return cleaned.length() > maxLength
                ? cleaned.substring(0, maxLength)
                : cleaned;
    }
}
