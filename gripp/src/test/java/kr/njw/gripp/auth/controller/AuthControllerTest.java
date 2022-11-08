package kr.njw.gripp.auth.controller;

import kr.njw.gripp.auth.application.AccountApplication;
import kr.njw.gripp.auth.application.dto.LoginAppResponse;
import kr.njw.gripp.auth.application.dto.RefreshTokenAppResponse;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
class AuthControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private AccountApplication accountApplication;
    private Random random;

    @BeforeEach
    void setUp() {
        this.random = new Random(42);
    }

    @AfterEach
    void tearDown() {
    }

    @WithMockUser
    @Test
    void signUp() throws Exception {
        given(this.accountApplication.signUp(any())).willReturn(true);
        given(this.accountApplication.signUp(argThat(argument -> argument.getUsername().equals("njw1204"))))
                .willReturn(false);

        ResultActions perform = this.mockMvc.perform(
                post("/auth/accounts").with(csrf()).contentType(MediaType.APPLICATION_JSON).content("""
                        {
                            "username": "njw1204",
                            "password": "pass1234"
                        }"""));
        ResultActions perform2 = this.mockMvc.perform(
                post("/auth/accounts").with(csrf()).contentType(MediaType.APPLICATION_JSON).content("""
                        {
                            "username": "njw12044",
                            "password": "!s%sa1234"
                        }"""));

        then(this.accountApplication).should(times(1))
                .signUp(argThat(argument -> argument.getUsername().equals("njw12044") &&
                        argument.getPassword().equals("!s%sa1234")));

        perform.andExpect(status().isConflict());
        perform2.andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.username").value("njw12044"));
    }

    @WithMockUser
    @Test
    void findAccount() throws Exception {
        given(this.accountApplication.isUsernameExisted(any())).willReturn(false);
        given(this.accountApplication.isUsernameExisted("test")).willReturn(true);

        ResultActions perform = this.mockMvc.perform(get("/auth/accounts/njw1204"));
        ResultActions perform2 = this.mockMvc.perform(get("/auth/accounts/test"));

        perform.andExpect(status().isNotFound());
        perform2.andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.username").value("test"));
    }

    @WithMockUser
    @Test
    void login() throws Exception {
        LoginAppResponse appResponse = new LoginAppResponse();
        appResponse.setSuccess(true);
        appResponse.setAccessToken("test ACCESS 987");
        appResponse.setRefreshToken("test REFRESH 123");

        given(this.accountApplication.login(any())).willAnswer(invocation -> {
            LoginAppResponse failAppResponse = new LoginAppResponse();

            switch (this.random.nextInt(5)) {
                case 0 -> {
                    failAppResponse.setSuccess(false);
                    failAppResponse.setAccessToken("test ACCESS 987");
                    failAppResponse.setRefreshToken("test REFRESH 123");
                }
                case 1 -> {
                    failAppResponse.setSuccess(true);
                    failAppResponse.setAccessToken("");
                    failAppResponse.setRefreshToken("test REFRESH 123");
                }
                case 2 -> {
                    failAppResponse.setSuccess(true);
                    failAppResponse.setAccessToken(null);
                    failAppResponse.setRefreshToken("test REFRESH 123");
                }
                case 3 -> {
                    failAppResponse.setSuccess(true);
                    failAppResponse.setAccessToken("test ACCESS 987");
                    failAppResponse.setRefreshToken("");
                }
                default -> {
                    failAppResponse.setSuccess(true);
                    failAppResponse.setAccessToken("test ACCESS 987");
                    failAppResponse.setRefreshToken(null);
                }
            }

            return failAppResponse;
        });
        given(this.accountApplication.login(argThat(argument -> argument.getUsername().equals("njw") &&
                argument.getPassword().equals("jasdfio32")))).willReturn(appResponse);

        ResultActions perform = this.mockMvc.perform(
                post("/auth/accounts/njw/tokens").with(csrf()).contentType(MediaType.APPLICATION_JSON).content("""
                        {
                            "password": "jasdfio32"
                        }"""));
        ResultActions perform2 = this.mockMvc.perform(
                post("/auth/accounts/njw/tokens").with(csrf()).contentType(MediaType.APPLICATION_JSON).content("""
                        {
                            "password": "pass1234"
                        }"""));

        List<ResultActions> performs3 = new ArrayList<>();

        for (int i = 0; i < 100; i++) {
            performs3.add(this.mockMvc.perform(
                    post("/auth/accounts/%s/tokens".formatted(RandomStringUtils.randomAlphanumeric(1, 20)))
                            .with(csrf()).contentType(MediaType.APPLICATION_JSON).content("""
                                    {
                                        "password": "%s"
                                    }""".formatted(RandomStringUtils.randomAlphanumeric(1, 20)))));
        }

        perform.andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.accessToken").value(appResponse.getAccessToken().orElseThrow()))
                .andExpect(jsonPath("$.refreshToken").value(appResponse.getRefreshToken().orElseThrow()));
        perform2.andExpect(status().isUnauthorized());
        performs3.forEach(aPerform -> {
            try {
                aPerform.andExpect(status().isUnauthorized());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    @WithMockUser
    @Test
    void refreshToken() throws Exception {
        RefreshTokenAppResponse appResponse = new RefreshTokenAppResponse();
        appResponse.setSuccess(true);
        appResponse.setAccessToken("refresh ACCESS 987");
        appResponse.setRefreshToken("refresh REFRESH 123");

        given(this.accountApplication.refreshToken(any())).willAnswer(invocation -> {
            RefreshTokenAppResponse failAppResponse = new RefreshTokenAppResponse();

            switch (this.random.nextInt(5)) {
                case 0 -> {
                    failAppResponse.setSuccess(false);
                    failAppResponse.setAccessToken("refresh ACCESS 987");
                    failAppResponse.setRefreshToken("refresh REFRESH 123");
                }
                case 1 -> {
                    failAppResponse.setSuccess(true);
                    failAppResponse.setAccessToken("");
                    failAppResponse.setRefreshToken("refresh REFRESH 123");
                }
                case 2 -> {
                    failAppResponse.setSuccess(true);
                    failAppResponse.setAccessToken(null);
                    failAppResponse.setRefreshToken("refresh REFRESH 123");
                }
                case 3 -> {
                    failAppResponse.setSuccess(true);
                    failAppResponse.setAccessToken("refresh ACCESS 987");
                    failAppResponse.setRefreshToken("");
                }
                default -> {
                    failAppResponse.setSuccess(true);
                    failAppResponse.setAccessToken("refresh ACCESS 987");
                    failAppResponse.setRefreshToken(null);
                }
            }

            return failAppResponse;
        });
        given(this.accountApplication.refreshToken(argThat(argument -> argument.getUsername().equals("dsf243rt") &&
                argument.getRefreshToken().equals("dgf234")))).willReturn(appResponse);

        ResultActions perform = this.mockMvc.perform(
                patch("/auth/accounts/dsf243rt/tokens").with(csrf()).contentType(MediaType.APPLICATION_JSON).content("""
                        {
                            "refreshToken": "dgf234"
                        }"""));
        ResultActions perform2 = this.mockMvc.perform(
                patch("/auth/accounts/dsf243rt/tokens").with(csrf()).contentType(MediaType.APPLICATION_JSON).content("""
                        {
                            "refreshToken": "dgf234!"
                        }"""));

        List<ResultActions> performs3 = new ArrayList<>();

        for (int i = 0; i < 100; i++) {
            performs3.add(this.mockMvc.perform(
                    patch("/auth/accounts/%s/tokens".formatted(RandomStringUtils.randomAlphanumeric(1, 20)))
                            .with(csrf()).contentType(MediaType.APPLICATION_JSON).content("""
                                    {
                                        "refreshToken": "%s"
                                    }""".formatted(RandomStringUtils.randomAlphanumeric(1, 20)))));
        }

        perform.andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.accessToken").value(appResponse.getAccessToken().orElseThrow()))
                .andExpect(jsonPath("$.refreshToken").value(appResponse.getRefreshToken().orElseThrow()));
        perform2.andExpect(status().isUnauthorized());
        performs3.forEach(aPerform -> {
            try {
                aPerform.andExpect(status().isUnauthorized());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    @WithMockUser
    @Test
    void deleteRefreshToken() throws Exception {
        given(this.accountApplication.deleteRefreshToken(any())).willReturn(false);
        given(this.accountApplication.deleteRefreshToken(argThat(argument -> argument.getUsername().equals("test") &&
                argument.getRefreshToken().equals("njw1204")))).willReturn(true);

        ResultActions perform = this.mockMvc.perform(delete("/auth/accounts/njw1204/tokens/test").with(csrf()));
        ResultActions perform2 = this.mockMvc.perform(delete("/auth/accounts/test/tokens/njw1204").with(csrf()));

        perform.andExpect(status().isNotFound());
        perform2.andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.username").value("test"));
    }
}
