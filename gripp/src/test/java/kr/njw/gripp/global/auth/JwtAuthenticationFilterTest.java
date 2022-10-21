package kr.njw.gripp.global.auth;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {
    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    @Mock
    private JwtAuthenticationProvider jwtAuthenticationProvider;
    @Mock
    private FilterChain filterChain;
    private MockHttpServletResponse mockHttpServletResponse;

    @BeforeEach
    void setUp() {
        this.mockHttpServletResponse = new MockHttpServletResponse();
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void doFilterInternal() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer abc123");

        MockHttpServletRequest request2 = new MockHttpServletRequest();
        request2.addHeader("Authorization", "Bearer test");

        MockHttpServletRequest request3 = new MockHttpServletRequest();

        given(this.jwtAuthenticationProvider.getAuthentication(anyString())).willReturn(Optional.empty());
        given(this.jwtAuthenticationProvider.getAuthentication("abc123")).willReturn(
                Optional.of(new UsernamePasswordAuthenticationToken(null, null, null)));

        this.jwtAuthenticationFilter.doFilterInternal(request, this.mockHttpServletResponse, this.filterChain);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        SecurityContextHolder.clearContext();

        this.jwtAuthenticationFilter.doFilterInternal(request2, this.mockHttpServletResponse, this.filterChain);
        Authentication authentication2 = SecurityContextHolder.getContext().getAuthentication();
        SecurityContextHolder.clearContext();

        this.jwtAuthenticationFilter.doFilterInternal(request3, this.mockHttpServletResponse, this.filterChain);
        Authentication authentication3 = SecurityContextHolder.getContext().getAuthentication();

        then(this.jwtAuthenticationProvider).should(times(2)).getAuthentication(anyString());
        then(this.filterChain).should(times(3)).doFilter(any(), any());

        assertThat(OncePerRequestFilter.class.isAssignableFrom(JwtAuthenticationFilter.class)).isTrue();
        assertThat(authentication.isAuthenticated()).isTrue();
        assertThat(authentication2).isNull();
        assertThat(authentication3).isNull();
    }
}
