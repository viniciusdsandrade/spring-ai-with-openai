package com.restful.open.ai.service.impl;

import com.restful.open.ai.dto.EmailDraft;
import com.restful.open.ai.service.EmailDraftService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class EmailDraftServiceImpl implements EmailDraftService {

    private final ChatClient chatClient;

    public EmailDraftServiceImpl(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @Override
    public EmailDraft createDraft(
            String jobDescription,
            String candidateName,
            String candidateEmail,
            int matchScore,
            String language
    ) {

        String templateText = """
                Você é um assistente que escreve e-mails de candidatura para vagas de tecnologia.
                Gere um e-mail com no máximo 3 parágrafos, em {language}, no formato JSON:
                {
                  "subject": "string",
                  "body": "string"
                }
                
                Dados do candidato:
                - Nome: {candidateName}
                - E-mail: {candidateEmail}
                - Score de compatibilidade com a vaga: {score}
                
                Descrição da vaga:
                {jobDescription}
                """;

        PromptTemplate template = new PromptTemplate(templateText);
        Prompt prompt = template.create(
                Map.of(
                        "language", language,
                        "candidateName", candidateName,
                        "candidateEmail", candidateEmail,
                        "score", matchScore,
                        "jobDescription", jobDescription
                )
        );


        return chatClient
                .prompt(prompt)
                .call()
                .entity(EmailDraft.class);
    }
}
