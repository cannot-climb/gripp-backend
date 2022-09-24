package kr.njw.gripp.auth.controller;

import kr.njw.gripp.auth.controller.dto.CreateUserRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {
    @PostMapping(value = "/users")
    public Map<String, Object> createUser(@Valid @RequestBody CreateUserRequest request) {
        HashMap<String, Object> map = new HashMap<>();

        map.put("result", "SUCCESS");
        System.out.println(request);

        return map;
    }
}
