package com.restful.open.ai.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record SendEmailRequest(
        @NotBlank String jobDescription,
        @NotBlank String candidateName,
        @Email @NotBlank String candidateEmail,
        @Email @NotBlank String recruiterEmail,
        @NotBlank String language
) {
}
