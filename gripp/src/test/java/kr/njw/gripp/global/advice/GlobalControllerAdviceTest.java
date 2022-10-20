package kr.njw.gripp.global.advice;

import kr.njw.gripp.auth.application.AccountApplication;
import kr.njw.gripp.auth.controller.AuthController;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
class GlobalControllerAdviceTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private AccountApplication accountApplication;

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @WithMockUser
    @Test
    void handleValidationException() throws Exception {
        this.mockMvc.perform(post("/auth/accounts").content("{}").contentType(MediaType.APPLICATION_JSON).with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json("""
                        {
                            "errors": [
                                "username must not be blank",
                                "password must not be blank"
                            ]
                        }
                        """));
    }
}
