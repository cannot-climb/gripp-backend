package kr.njw.gripp.global.controller;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class HttpErrorController implements ErrorController {
    @RequestMapping("/error")
    public Map<String, Object> error(HttpServletResponse response) {
        Map<String, Object> body = new HashMap<>();
        body.put("errors",
                List.of(response.getStatus() + " " + HttpStatus.valueOf(response.getStatus()).getReasonPhrase()));

        return body;
    }
}
