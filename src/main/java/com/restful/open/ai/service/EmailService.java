package com.restful.open.ai.service;

import com.restful.open.ai.dto.EmailDraft;
import org.springframework.web.multipart.MultipartFile;

public interface EmailService {
    String sendResumeEmail(
            String recruiterEmail,
            String candidateEmail,
            EmailDraft draft,
            MultipartFile resumeFile
    );
}
