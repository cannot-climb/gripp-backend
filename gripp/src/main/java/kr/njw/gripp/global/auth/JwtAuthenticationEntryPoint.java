package kr.njw.gripp.global.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.njw.gripp.global.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        int status = HttpServletResponse.SC_UNAUTHORIZED;
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setErrors(List.of(status + " " + HttpStatus.valueOf(status).getReasonPhrase()));

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(status);
        response.getWriter().write(this.objectMapper.writeValueAsString(errorResponse));
    }
}
