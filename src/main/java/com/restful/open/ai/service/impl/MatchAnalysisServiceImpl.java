package com.restful.open.ai.service.impl;

import com.restful.open.ai.dto.ResumeMatchAnalysis;
import com.restful.open.ai.service.MatchAnalysisService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.embedding.EmbeddingModel;

import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class MatchAnalysisServiceImpl implements MatchAnalysisService {

    private final ChatClient chatClient;
    private final EmbeddingModel embeddingModel;

    public MatchAnalysisServiceImpl(ChatClient chatClient, EmbeddingModel embeddingModel) {
        this.chatClient = chatClient;
        this.embeddingModel = embeddingModel;
    }

    @Override
    public ResumeMatchAnalysis analyze(String normalizedJobDescription, String normalizedResumeText) {
        String templateText = """
                Você é um avaliador de compatibilidade entre vagas de tecnologia e currículos.
                Analise a vaga e o currículo abaixo e responda SOMENTE com um JSON válido
                com a seguinte estrutura (sem comentários):

                - score: número inteiro entre 0 e 1000
                - scoreExplanation: string explicando os principais motivos do score
                - keywords: lista de objetos com campos keyword, present, evidenceSnippet, importance (1-5)
                - suggestions: lista de objetos com campos section, currentWeakness, suggestedImprovement, priority (1-5)

                VAGA:
                {jobDescription}

                CURRICULO:
                {resume}
                """;

        PromptTemplate promptTemplate = new PromptTemplate(templateText);
        Prompt prompt = promptTemplate.create(
                Map.of(
                        "jobDescription", normalizedJobDescription,
                        "resume", normalizedResumeText
                )
        );

        ResumeMatchAnalysis llmAnalysis = chatClient
                .prompt(prompt)
                .call()
                .entity(ResumeMatchAnalysis.class);

        int finalScore = computeHybridScore(
                normalizedJobDescription,
                normalizedResumeText,
                llmAnalysis.score()
        );

        return new ResumeMatchAnalysis(
                finalScore,
                llmAnalysis.scoreExplanation(),
                llmAnalysis.keywords(),
                llmAnalysis.suggestions()
        );
    }

    private int computeHybridScore(String jobDescription, String resume, int llmScore) {
        try {
            EmbeddingResponse jobEmbeddingResponse =
                    embeddingModel.embedForResponse(List.of(jobDescription));

            EmbeddingResponse resumeEmbeddingResponse =
                    embeddingModel.embedForResponse(List.of(resume));

            float[] jobVector = jobEmbeddingResponse.getResult().getOutput();
            float[] resumeVector = resumeEmbeddingResponse.getResult().getOutput();

            double sim = cosineSimilarity(jobVector, resumeVector);
            int embeddingScore = (int) Math.round(sim * 1000.0);

            double finalScore = 0.6 * llmScore + 0.4 * embeddingScore;
            return (int) Math.round(finalScore);
        } catch (Exception exception) {
            return llmScore;
        }
    }

    private double cosineSimilarity(float[] v1, float[] v2) {
        if (v1.length != v2.length) throw new IllegalArgumentException("Embedding dimensions mismatch");


        double dot = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (int i = 0; i < v1.length; i++) {
            dot += v1[i] * v2[i];
            normA += v1[i] * v1[i];
            normB += v2[i] * v2[i];
        }

        if (normA == 0 || normB == 0) return 0.0;

        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }
}
