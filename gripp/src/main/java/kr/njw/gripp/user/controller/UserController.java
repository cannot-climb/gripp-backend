package kr.njw.gripp.user.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/users")
public class UserController {
    @RequestMapping("/")
    @ResponseBody
    public String index() {
        return "Hello";
    }
}
