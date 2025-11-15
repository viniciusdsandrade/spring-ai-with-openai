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
}
