package kr.njw.gripp.global.controller;

import io.swagger.v3.oas.annotations.Hidden;
import kr.njw.gripp.global.dto.ErrorResponse;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

@Hidden
@RestController
public class HttpErrorController implements ErrorController {
    @RequestMapping("/error")
    public ErrorResponse error(HttpServletResponse response) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setErrors(
                List.of(response.getStatus() + " " + HttpStatus.valueOf(response.getStatus()).getReasonPhrase()));

        return errorResponse;
    }
}
