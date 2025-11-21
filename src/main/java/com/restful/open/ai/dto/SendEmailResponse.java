package com.restful.open.ai.dto;

public record SendEmailResponse(
        boolean success,
        String messageId,
        String message
) {
}
