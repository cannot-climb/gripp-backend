package kr.njw.gripp.auth.controller;

import kr.njw.gripp.auth.application.AccountApplication;
import kr.njw.gripp.auth.controller.dto.FindAccountResponse;
import kr.njw.gripp.auth.controller.dto.SignUpRequest;
import kr.njw.gripp.auth.controller.dto.SignUpResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AccountApplication accountApplication;

    @PostMapping("/accounts")
    public ResponseEntity<SignUpResponse> signUp(@Valid @RequestBody SignUpRequest request) {
        boolean result = this.accountApplication.signUp(request);

        SignUpResponse response = new SignUpResponse();
        response.setCode(result ? "SUCCESS" : "FAIL");

        return ResponseEntity.status(result ? HttpStatus.CREATED : HttpStatus.CONFLICT).body(response);
    }

    @GetMapping("/accounts/{username}")
    public ResponseEntity<FindAccountResponse> findAccount(@PathVariable("username") String username) {
        boolean result = this.accountApplication.isUsernameExisted(username);

        FindAccountResponse response = new FindAccountResponse();
        response.setResult(result);

        return ResponseEntity.ok(response);
    }
}
