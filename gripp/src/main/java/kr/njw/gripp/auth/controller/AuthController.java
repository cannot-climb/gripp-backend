package kr.njw.gripp.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.njw.gripp.auth.application.AccountApplication;
import kr.njw.gripp.auth.controller.dto.FindAccountResponse;
import kr.njw.gripp.auth.controller.dto.SignUpRequest;
import kr.njw.gripp.auth.controller.dto.SignUpResponse;
import kr.njw.gripp.global.dto.ErrorResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Tag(name = "Auth")
@RequiredArgsConstructor
@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AccountApplication accountApplication;

    @Operation(summary = "회원가입", description = "회원가입 API")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "가입 완료",
                    content = @Content(schema = @Schema(implementation = SignUpResponse.class))),
            @ApiResponse(responseCode = "400",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "가입 실패",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    })
    @PostMapping("/accounts")
    public ResponseEntity<?> signUp(@Valid @RequestBody SignUpRequest request) {
        boolean result = this.accountApplication.signUp(request);

        if (result) {
            return ResponseEntity.ok(new SignUpResponse());
        }

        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setErrors(List.of("fail to sign up"));
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    @Operation(summary = "회원조회", description = "회원조회 API")
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    content = @Content(schema = @Schema(implementation = FindAccountResponse.class))),
            @ApiResponse(responseCode = "400",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    })
    @GetMapping("/accounts/{username}")
    public ResponseEntity<FindAccountResponse> findAccount(
            @Parameter(description = "유저 아이디", example = "njw1204") @PathVariable("username") String username) {
        boolean result = this.accountApplication.isUsernameExisted(username);

        FindAccountResponse response = new FindAccountResponse();
        response.setResult(result);

        return ResponseEntity.ok(response);
    }
}
