package com.restful.open.ai.service;

import com.restful.open.ai.service.impl.ResumeParserServiceImpl;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ResumeParserServiceImplTest {

    @Mock
    private MultipartFile multipartFile;

    @Mock
    private Tika tika;

    private ResumeParserServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ResumeParserServiceImpl(tika);
    }

    @Test
    @DisplayName("normalize: retorna string vazia quando texto é null")
    void normalize_null_returnsEmptyString() {
        String result = service.normalize(null);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("normalize: comprime espaços em branco e remove espaços das bordas")
    void normalize_compressesWhitespaceAndTrims() {
        String raw = "  Currículo   com \n várias \t linhas  ";

        String result = service.normalize(raw);

        assertThat(result).isEqualTo("Currículo com várias linhas");
    }

    @Test
    @DisplayName("normalize: corta texto quando tamanho excede o limite máximo")
    void normalize_truncatesWhenExceedsMaxLength() {
        int maxLength = 15000;
        String longText = "b".repeat(maxLength + 5);

        String result = service.normalize(longText);

        assertThat(result).hasSize(maxLength);
        assertThat(result).isEqualTo(longText.substring(0, maxLength));
    }

    @Test
    @DisplayName("extractText: utiliza Tika para extrair texto e aplica normalização")
    void extractText_parsesAndNormalizesText() throws Exception {
        String rawContent = "  Conteúdo   de currículo \n com \t múltiplas  linhas ";
        InputStream inputStream =
                new ByteArrayInputStream(rawContent.getBytes(StandardCharsets.UTF_8));

        when(multipartFile.getInputStream()).thenReturn(inputStream);
        when(tika.parseToString(any(InputStream.class))).thenReturn(rawContent);

        String result = service.extractText(multipartFile);

        assertThat(result).isEqualTo("Conteúdo de currículo com múltiplas linhas");
    }

    @Test
    @DisplayName("extractText: quando ocorre IOException, lança RuntimeException com mensagem apropriada")
    void extractText_whenIoError_throwsRuntimeException() throws Exception {
        when(multipartFile.getInputStream()).thenThrow(new IOException("I/O error"));

        assertThatThrownBy(() -> service.extractText(multipartFile))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Error parsing resume file");
    }
    
    @Test
    @DisplayName("extractText: quando ocorre TikaException, lança RuntimeException com mensagem apropriada")
    void extractText_whenTikaError_throwsRuntimeException() throws Exception {
        InputStream inputStream =
                new ByteArrayInputStream("qualquer coisa".getBytes(StandardCharsets.UTF_8));
        when(multipartFile.getInputStream()).thenReturn(inputStream);
        when(tika.parseToString(any(InputStream.class)))
                .thenThrow(new TikaException("parse error"));

        assertThatThrownBy(() -> service.extractText(multipartFile))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Error parsing resume file");
    }
}
