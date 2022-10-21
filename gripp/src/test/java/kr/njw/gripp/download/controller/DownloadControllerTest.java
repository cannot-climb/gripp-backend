package kr.njw.gripp.download.controller;

import kr.njw.gripp.download.application.DownloadResourceFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.util.InMemoryResource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.nio.file.Path;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DownloadController.class)
class DownloadControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private DownloadResourceFactory downloadResourceFactory;

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @WithMockUser
    @Test
    void download() throws Exception {
        given(this.downloadResourceFactory.createResource(notNull())).willAnswer(
                invocation -> new FileSystemResource(invocation.getArgument(0, Path.class)));
        given(this.downloadResourceFactory.createResource(
                argThat(argument -> argument.toString().endsWith("yes")))).willReturn(new InMemoryResource("yes"));

        ResultActions perform = this.mockMvc.perform(get("/download/no"));
        ResultActions perform2 = this.mockMvc.perform(get("/download/테스트 yes"));

        perform.andExpect(status().isNotFound());
        perform2.andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition",
                        "attachment; filename*=UTF-8''%ED%85%8C%EC%8A%A4%ED%8A%B8%20yes"))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_OCTET_STREAM))
                .andExpect(content().string("yes"));
    }
}
