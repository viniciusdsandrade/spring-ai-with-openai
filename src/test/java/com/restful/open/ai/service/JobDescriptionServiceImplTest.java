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
}
