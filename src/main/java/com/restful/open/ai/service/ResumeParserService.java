package com.restful.open.ai.service;

import org.springframework.web.multipart.MultipartFile;

public interface ResumeParserService {
    String extractText(MultipartFile file);

    String normalize(String raw);
}
