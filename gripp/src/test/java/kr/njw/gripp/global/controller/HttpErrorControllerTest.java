package kr.njw.gripp.global.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(HttpErrorController.class)
class HttpErrorControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @WithMockUser
    @Test
    void error() throws Exception {
        ResultActions perform = this.mockMvc.perform(get("/error"));

        perform.andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json("""
                        {
                            "errors": [
                                "common error: 200 ok"
                            ]
                        }"""));
    }
}
