package kr.njw.gripp.global.auth;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtAuthenticationProvider jwtAuthenticationProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String header = Objects.requireNonNullElse(request.getHeader(AUTHORIZATION_HEADER), "").trim();

        if (StringUtils.startsWithIgnoreCase(header, BEARER_PREFIX)) {
            String token = StringUtils.substringAfter(header, BEARER_PREFIX);
            Optional<Authentication> authentication = this.jwtAuthenticationProvider.getAuthentication(token);

            authentication.ifPresent(value -> SecurityContextHolder.getContext().setAuthentication(value));
        }

        filterChain.doFilter(request, response);
    }
}
