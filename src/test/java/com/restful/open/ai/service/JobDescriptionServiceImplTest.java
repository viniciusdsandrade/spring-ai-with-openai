package com.restful.open.ai.service;

import com.restful.open.ai.service.impl.JobDescriptionServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class JobDescriptionServiceImplTest {

    private final JobDescriptionServiceImpl service = new JobDescriptionServiceImpl();

    @Test
    @DisplayName("normalize: retorna string vazia quando descrição é null")
    void normalize_null_returnsEmptyString() {
        String result = service.normalize(null);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("normalize: comprime espaços em branco e remove espaços das bordas")
    void normalize_compressesWhitespaceAndTrims() {
        String raw = "  Vaga   para   backend \n Java\tcom   Spring  ";

        String result = service.normalize(raw);

        assertThat(result).isEqualTo("Vaga para backend Java com Spring");
    }

    @Test
    @DisplayName("normalize: mantém texto quando tamanho é menor ou igual ao limite")
    void normalize_keepsTextWhenWithinMaxLength() {
        String raw = "Descrição curta de vaga backend";

        String result = service.normalize(raw);

        assertThat(result).isEqualTo("Descrição curta de vaga backend");
    }
    
    @Test
    @DisplayName("normalize: corta texto quando tamanho excede o limite máximo")
    void normalize_truncatesWhenExceedsMaxLength() {
        int maxLength = 15000;
        String longText = "a".repeat(maxLength + 10);

        String result = service.normalize(longText);

        assertThat(result).hasSize(maxLength);
        assertThat(result).isEqualTo(longText.substring(0, maxLength));
    }
}
