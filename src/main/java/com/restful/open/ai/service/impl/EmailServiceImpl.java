package com.restful.open.ai.service.impl;

import com.restful.open.ai.dto.EmailDraft;
import com.restful.open.ai.service.EmailService;
import jakarta.mail.MessagingException;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import jakarta.mail.internet.MimeMessage;

import java.io.IOException;

import static java.util.Objects.requireNonNull;

@Service
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender javaMailSender;

    public EmailServiceImpl(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    @Override
    public String sendResumeEmail(
            String recruiterEmail,
            String candidateEmail,
            EmailDraft draft,
            MultipartFile resumeFile
    ) {

        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(recruiterEmail);
            helper.setReplyTo(candidateEmail);
            helper.setFrom(candidateEmail);
            helper.setSubject(draft.subject());
            helper.setText(draft.body(), false);

            if (resumeFile != null && !resumeFile.isEmpty()) {
                helper.addAttachment(
                        requireNonNull(resumeFile.getOriginalFilename()),
                        new ByteArrayResource(resumeFile.getBytes())
                );
            }

            javaMailSender.send(message);
            return message.getMessageID();
        } catch (IOException | MessagingException exception) {
            throw new RuntimeException("Error sending e-mail", exception);
        }
    }
}
