package kr.njw.gripp.global.auth;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

class JwtAccessDeniedHandlerTest {
    private JwtAccessDeniedHandler jwtAccessDeniedHandler;
    private MockHttpServletRequest mockHttpServletRequest;
    private MockHttpServletResponse mockHttpServletResponse;

    @BeforeEach
    void setUp() {
        this.jwtAccessDeniedHandler = new JwtAccessDeniedHandler();
        this.mockHttpServletRequest = new MockHttpServletRequest();
        this.mockHttpServletResponse = new MockHttpServletResponse();
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void handle() throws IOException {
        this.jwtAccessDeniedHandler.handle(this.mockHttpServletRequest, this.mockHttpServletResponse,
                new AccessDeniedException(""));

        assertThat(AccessDeniedHandler.class.isAssignableFrom(JwtAccessDeniedHandler.class)).isTrue();
        assertThat(this.mockHttpServletResponse.getStatus()).isEqualTo(HttpServletResponse.SC_FORBIDDEN);
        assertThat(this.mockHttpServletResponse.getContentType()).isEqualTo("application/json;charset=UTF-8");
        assertThat(this.mockHttpServletResponse.getContentAsString().trim()).isEqualTo("""
                {"errors":["common error: 403 forbidden"]}""");
    }
}
