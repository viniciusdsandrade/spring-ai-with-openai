package com.restful.open.ai.service;


import com.restful.open.ai.dto.KeywordMatch;
import com.restful.open.ai.dto.ResumeMatchAnalysis;
import com.restful.open.ai.dto.SectionSuggestion;
import com.restful.open.ai.service.impl.MatchAnalysisServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.embedding.Embedding;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingResponse;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class MatchAnalysisServiceImplTest {
    @Mock
    private ChatClient chatClient;

    @Mock
    private ChatClient.ChatClientRequestSpec requestSpec;

    @Mock
    private ChatClient.CallResponseSpec callResponseSpec;

    @Mock
    private EmbeddingModel embeddingModel;

    @Mock
    private EmbeddingResponse jobEmbeddingResponse;

    @Mock
    private EmbeddingResponse resumeEmbeddingResponse;

    @Mock
    private Embedding jobEmbedding;

    @Mock
    private Embedding resumeEmbedding;

    @InjectMocks
    private MatchAnalysisServiceImpl matchAnalysisService;

    private static ResumeMatchAnalysis llmAnalysis(int score) {
        List<KeywordMatch> keywords = List.of(
                new KeywordMatch(
                        "Java",
                        true,
                        "Experiência com Java",
                        5
                ),
                new KeywordMatch(
                        "Kubernetes",
                        false,
                        "",
                        4
                )
        );
        List<SectionSuggestion> suggestions = List.of(
                new SectionSuggestion(
                        "Experiência",
                        "Não menciona métricas",
                        "Adicionar métricas de impacto",
                        5
                )
        );
        return new ResumeMatchAnalysis(
                score,
                "Score calculado pelo LLM",
                keywords,
                suggestions
        );
    }

    @Test
    @DisplayName("analyze: calcula score híbrido e preserva explicação/keywords/sugestões")
    void analyze_ok() {
        String job = "Vaga para backend Java com Spring e Kubernetes";
        String cv = "Desenvolvedor Java com experiência em Spring";

        when(chatClient.prompt(any(Prompt.class)))
                .thenReturn(requestSpec);
        when(requestSpec.call())
                .thenReturn(callResponseSpec);

        ResumeMatchAnalysis llmOut = llmAnalysis(600);
        when(callResponseSpec.entity(ResumeMatchAnalysis.class))
                .thenReturn(llmOut);

        float[] jobVector = new float[]{1.0f, 0.0f};
        float[] resumeVector = new float[]{1.0f, 0.0f};

        when(embeddingModel.embedForResponse(List.of(job)))
                .thenReturn(jobEmbeddingResponse);
        when(embeddingModel.embedForResponse(List.of(cv)))
                .thenReturn(resumeEmbeddingResponse);

        when(jobEmbeddingResponse.getResult())
                .thenReturn(jobEmbedding);
        when(resumeEmbeddingResponse.getResult())
                .thenReturn(resumeEmbedding);

        when(jobEmbedding.getOutput())
                .thenReturn(jobVector);
        when(resumeEmbedding.getOutput())
                .thenReturn(resumeVector);

        ResumeMatchAnalysis result = matchAnalysisService.analyze(job, cv);

        assertThat(result.score()).isEqualTo(760);
        assertThat(result.scoreExplanation()).isEqualTo("Score calculado pelo LLM");
        assertThat(result.keywords()).hasSize(2);
        assertThat(result.suggestions()).hasSize(1);
        assertThat(result.keywords())
                .extracting(KeywordMatch::keyword)
                .containsExactly("Java", "Kubernetes");

        verify(chatClient).prompt(any(Prompt.class));
        verify(requestSpec).call();
        verify(callResponseSpec).entity(ResumeMatchAnalysis.class);

        verify(embeddingModel).embedForResponse(List.of(job));
        verify(embeddingModel).embedForResponse(List.of(cv));
    }

    @Test
    @DisplayName("analyze: quando embeddings falham, usa apenas o score do LLM (fallback)")
    void analyze_embeddingError_fallsBackToLlmScore() {
        String job = "Vaga X";
        String cv = "Curriculo Y";

        when(chatClient.prompt(any(Prompt.class)))
                .thenReturn(requestSpec);
        when(requestSpec.call())
                .thenReturn(callResponseSpec);

        ResumeMatchAnalysis llmOut = llmAnalysis(850);
        when(callResponseSpec.entity(ResumeMatchAnalysis.class))
                .thenReturn(llmOut);

        when(embeddingModel.embedForResponse(any()))
                .thenThrow(new RuntimeException("Embedding error"));

        ResumeMatchAnalysis result = matchAnalysisService.analyze(job, cv);

        assertThat(result.score()).isEqualTo(850);
        assertThat(result.scoreExplanation()).isEqualTo("Score calculado pelo LLM");

        assertThat(result.keywords()).hasSize(2);
        assertThat(result.suggestions()).hasSize(1);

        verify(chatClient).prompt(any(Prompt.class));
        verify(requestSpec).call();
        verify(callResponseSpec).entity(ResumeMatchAnalysis.class);
        verify(embeddingModel).embedForResponse(any());
    }

    @Test
    @DisplayName("analyze: prompt inclui vaga e currículo normalizados")
    void analyze_buildsPromptWithJobAndResume() {
        String job = "Vaga para backend Java com Spring e Kubernetes";
        String cv = "Desenvolvedor Java com experiência em Spring";

        when(chatClient.prompt(any(Prompt.class)))
                .thenReturn(requestSpec);
        when(requestSpec.call())
                .thenReturn(callResponseSpec);
        when(callResponseSpec.entity(ResumeMatchAnalysis.class))
                .thenReturn(llmAnalysis(600));

        when(embeddingModel.embedForResponse(any()))
                .thenReturn(jobEmbeddingResponse, resumeEmbeddingResponse);
        when(jobEmbeddingResponse.getResult()).thenReturn(jobEmbedding);
        when(resumeEmbeddingResponse.getResult()).thenReturn(resumeEmbedding);
        when(jobEmbedding.getOutput()).thenReturn(new float[]{1.0f, 0.0f});
        when(resumeEmbedding.getOutput()).thenReturn(new float[]{1.0f, 0.0f});

        matchAnalysisService.analyze(job, cv);

        ArgumentCaptor<Prompt> captor = ArgumentCaptor.forClass(Prompt.class);
        verify(chatClient).prompt(captor.capture());

        Prompt captured = captor.getValue();
        List<Message> messages = captured.getInstructions();
        assertThat(messages).isNotEmpty();

        String content = messages.getFirst().getText();
        assertThat(content).contains(job);
        assertThat(content).contains(cv);
        // garante que placeholders foram resolvidos
        assertThat(content)
                .doesNotContain("{jobDescription}")
                .doesNotContain("{resume}");
    }

    @Test
    @DisplayName("computeHybridScore: combina scores LLM e embedding com similaridade parcial")
    void analyze_mediumSimilarity_combinesScores() {
        String job = "Vaga Y";
        String cv = "Curriculo Z";

        when(chatClient.prompt(any(Prompt.class)))
                .thenReturn(requestSpec);
        when(requestSpec.call())
                .thenReturn(callResponseSpec);
        when(callResponseSpec.entity(ResumeMatchAnalysis.class))
                .thenReturn(llmAnalysis(600));

        when(embeddingModel.embedForResponse(List.of(job)))
                .thenReturn(jobEmbeddingResponse);
        when(embeddingModel.embedForResponse(List.of(cv)))
                .thenReturn(resumeEmbeddingResponse);

        when(jobEmbeddingResponse.getResult())
                .thenReturn(jobEmbedding);
        when(resumeEmbeddingResponse.getResult())
                .thenReturn(resumeEmbedding);

        float[] jobVector = new float[]{1.0f, 0.0f};
        float[] resumeVector = new float[]{4.0f, 3.0f};
        when(jobEmbedding.getOutput()).thenReturn(jobVector);
        when(resumeEmbedding.getOutput()).thenReturn(resumeVector);

        ResumeMatchAnalysis result = matchAnalysisService.analyze(job, cv);

        assertThat(result.score()).isEqualTo(680);
    }
    @Test
    @DisplayName("computeHybridScore: vetor com norma zero resulta em similaridade 0")
    void analyze_zeroNormEmbedding_yieldsZeroSimilarity() {
        String job = "Vaga W";
        String cv = "Curriculo T";

        when(chatClient.prompt(any(Prompt.class)))
                .thenReturn(requestSpec);
        when(requestSpec.call())
                .thenReturn(callResponseSpec);
        when(callResponseSpec.entity(ResumeMatchAnalysis.class))
                .thenReturn(llmAnalysis(700));

        when(embeddingModel.embedForResponse(List.of(job)))
                .thenReturn(jobEmbeddingResponse);
        when(embeddingModel.embedForResponse(List.of(cv)))
                .thenReturn(resumeEmbeddingResponse);

        when(jobEmbeddingResponse.getResult())
                .thenReturn(jobEmbedding);
        when(resumeEmbeddingResponse.getResult())
                .thenReturn(resumeEmbedding);

        when(jobEmbedding.getOutput()).thenReturn(new float[]{0.0f, 0.0f});
        when(resumeEmbedding.getOutput()).thenReturn(new float[]{1.0f, 0.0f});

        ResumeMatchAnalysis result = matchAnalysisService.analyze(job, cv);

        assertThat(result.score()).isEqualTo(420);
    }

    @Test
    @DisplayName("computeHybridScore: mismatch de dimensão dos vetores faz fallback para score do LLM")
    void analyze_dimensionMismatch_fallsBackToLlmScore() {
        String job = "Vaga M";
        String cv = "Curriculo N";

        when(chatClient.prompt(any(Prompt.class)))
                .thenReturn(requestSpec);
        when(requestSpec.call())
                .thenReturn(callResponseSpec);
        when(callResponseSpec.entity(ResumeMatchAnalysis.class))
                .thenReturn(llmAnalysis(650));

        when(embeddingModel.embedForResponse(List.of(job)))
                .thenReturn(jobEmbeddingResponse);
        when(embeddingModel.embedForResponse(List.of(cv)))
                .thenReturn(resumeEmbeddingResponse);

        when(jobEmbeddingResponse.getResult())
                .thenReturn(jobEmbedding);
        when(resumeEmbeddingResponse.getResult())
                .thenReturn(resumeEmbedding);

        when(jobEmbedding.getOutput()).thenReturn(new float[]{1.0f, 2.0f});
        when(resumeEmbedding.getOutput()).thenReturn(new float[]{1.0f});

        ResumeMatchAnalysis result = matchAnalysisService.analyze(job, cv);

        assertThat(result.score()).isEqualTo(650);
    }
}
