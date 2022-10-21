package kr.njw.gripp.global.auth;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.web.AuthenticationEntryPoint;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

class JwtAuthenticationEntryPointTest {
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private MockHttpServletRequest mockHttpServletRequest;
    private MockHttpServletResponse mockHttpServletResponse;

    @BeforeEach
    void setUp() {
        this.jwtAuthenticationEntryPoint = new JwtAuthenticationEntryPoint();
        this.mockHttpServletRequest = new MockHttpServletRequest();
        this.mockHttpServletResponse = new MockHttpServletResponse();
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void commence() throws IOException {
        this.jwtAuthenticationEntryPoint.commence(this.mockHttpServletRequest, this.mockHttpServletResponse,
                new BadCredentialsException(""));

        assertThat(AuthenticationEntryPoint.class.isAssignableFrom(JwtAuthenticationEntryPoint.class)).isTrue();
        assertThat(this.mockHttpServletResponse.getStatus()).isEqualTo(HttpServletResponse.SC_UNAUTHORIZED);
        assertThat(this.mockHttpServletResponse.getContentType()).isEqualTo("application/json;charset=UTF-8");
        assertThat(this.mockHttpServletResponse.getContentAsString().trim()).isEqualTo("""
                {"errors":["common error: 401 unauthorized"]}""");
    }
}
