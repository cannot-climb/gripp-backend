package kr.njw.gripp.user.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class UserController {
    @RequestMapping("/")
    public String index() {
        return "Hello";
    }
}
