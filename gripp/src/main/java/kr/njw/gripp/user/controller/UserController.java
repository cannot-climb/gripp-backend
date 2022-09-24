package kr.njw.gripp.user.controller;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Hidden
@RestController
@RequestMapping("/users")
public class UserController {
    @RequestMapping("/")
    public String index() {
        return "Hello";
    }
}
