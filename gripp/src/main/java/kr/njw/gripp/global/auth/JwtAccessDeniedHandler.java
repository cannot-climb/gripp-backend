package kr.njw.gripp.global.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.njw.gripp.global.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@Component
public class JwtAccessDeniedHandler implements AccessDeniedHandler {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        int status = HttpServletResponse.SC_FORBIDDEN;
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setErrors(List.of(status + " " + HttpStatus.valueOf(status).getReasonPhrase()));

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(status);
        response.getWriter().write(this.objectMapper.writeValueAsString(errorResponse));
    }
}
