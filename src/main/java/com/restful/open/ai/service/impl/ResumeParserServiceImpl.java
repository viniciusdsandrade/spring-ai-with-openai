package com.restful.open.ai.service.impl;

import com.restful.open.ai.service.ResumeParserService;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class ResumeParserServiceImpl implements ResumeParserService {

    private final Tika tika;

    public ResumeParserServiceImpl(Tika tika) {
        this.tika = tika;
    }

    @Override
    public String extractText(MultipartFile file) {
        try {
            String text = tika.parseToString(file.getInputStream());
            return normalize(text);
        } catch (IOException | TikaException e) {
            throw new RuntimeException("Error parsing resume file", e);
        }
    }

    @Override
    public String normalize(String raw) {
        if (raw == null) return "";
        String cleaned = raw.replaceAll("\\s+", " ").trim();
        int maxLength = 15000;
        return cleaned.length() > maxLength ? cleaned.substring(0, maxLength) : cleaned;
    }
}
