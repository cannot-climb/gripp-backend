package kr.njw.gripp.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.njw.gripp.auth.application.AccountApplication;
import kr.njw.gripp.auth.application.dto.*;
import kr.njw.gripp.auth.controller.dto.*;
import kr.njw.gripp.global.dto.ErrorResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Operation(summary = "회원가입", description = "회원가입 API")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "가입 완료",
                    content = @Content(schema = @Schema(implementation = SignUpResponse.class))),
            @ApiResponse(responseCode = "400", description = "가입 실패 (Bad Request) / ex: 아이디, 비밀번호 형식이 틀린 경우",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "가입 실패 (Conflict) / ex: 아이디가 중복된 경우",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    })
    @PostMapping("/accounts")
    public ResponseEntity<?> signUp(@Valid @RequestBody SignUpRequest request) {
        SignUpAppRequest appRequest = new SignUpAppRequest();
        appRequest.setUsername(request.getUsername());
        appRequest.setPassword(request.getPassword());

        boolean appResponse = this.accountApplication.signUp(appRequest);

        if (!appResponse) {
            ErrorResponse errorResponse = new ErrorResponse();
            errorResponse.setErrors(List.of("fail to sign up"));
            this.logger.warn("회원가입 실패 - " + request.getUsername());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
        }

        SignUpResponse response = new SignUpResponse();
        response.setUsername(appRequest.getUsername());
        this.logger.info("회원가입 완료 - " + request.getUsername());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "회원찾기", description = "회원찾기 API")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "찾기 완료",
                    content = @Content(schema = @Schema(implementation = FindAccountResponse.class))),
            @ApiResponse(responseCode = "400", description = "찾기 실패 (Bad Request)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "찾기 실패 (Not Found) / ex: 아이디가 존재하지 않는 경우",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    })
    @GetMapping("/accounts/{username}")
    public ResponseEntity<?> findAccount(
            @Parameter(description = "유저 아이디", example = "njw1204") @PathVariable("username") String username) {
        boolean appResponse = this.accountApplication.isUsernameExisted(username);

        if (!appResponse) {
            ErrorResponse errorResponse = new ErrorResponse();
            errorResponse.setErrors(List.of("fail to find account"));
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }

        FindAccountResponse response = new FindAccountResponse();
        response.setUsername(username);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "로그인", description = "로그인 API")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그인 완료",
                    content = @Content(schema = @Schema(implementation = LoginResponse.class))),
            @ApiResponse(responseCode = "400", description = "로그인 실패 (Bad Request)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "로그인 실패 (Unauthorized) / ex: 아이디, 비밀번호가 틀린 경우",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/accounts/{username}/tokens")
    public ResponseEntity<?> login(
            @Parameter(description = "유저 아이디", example = "njw1204") @PathVariable("username") String username,
            @Valid @RequestBody LoginRequest request) {
        LoginAppRequest appRequest = new LoginAppRequest();
        appRequest.setUsername(username);
        appRequest.setPassword(request.getPassword());

        LoginAppResponse appResponse = this.accountApplication.login(appRequest);

        if (!appResponse.isSuccess() || appResponse.getAccessToken().isEmpty() ||
                appResponse.getRefreshToken().isEmpty()) {
            ErrorResponse errorResponse = new ErrorResponse();
            errorResponse.setErrors(List.of("fail to login"));
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }

        LoginResponse response = new LoginResponse();
        response.setAccessToken(appResponse.getAccessToken().get());
        response.setRefreshToken(appResponse.getRefreshToken().get());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "토큰 갱신", description = "토큰 갱신 API")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "갱신 완료",
                    content = @Content(schema = @Schema(implementation = RefreshTokenResponse.class))),
            @ApiResponse(responseCode = "400", description = "갱신 실패 (Bad Request)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "갱신 실패 (Unauthorized) / ex: 리프레시 토큰이 틀렸거나 만료된 경우",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PatchMapping("/accounts/{username}/tokens")
    public ResponseEntity<?> refreshToken(
            @Parameter(description = "유저 아이디", example = "njw1204") @PathVariable("username") String username,
            @Valid @RequestBody RefreshTokenRequest request) {
        RefreshTokenAppRequest appRequest = new RefreshTokenAppRequest();
        appRequest.setUsername(username);
        appRequest.setRefreshToken(request.getRefreshToken());

        RefreshTokenAppResponse appResponse = this.accountApplication.refreshToken(appRequest);

        if (!appResponse.isSuccess() || appResponse.getAccessToken().isEmpty() ||
                appResponse.getRefreshToken().isEmpty()) {
            ErrorResponse errorResponse = new ErrorResponse();
            errorResponse.setErrors(List.of("fail to refresh"));
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }

        RefreshTokenResponse response = new RefreshTokenResponse();
        response.setAccessToken(appResponse.getAccessToken().get());
        response.setRefreshToken(appResponse.getRefreshToken().get());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "리프레시 토큰 폐기", description = "리프레시 토큰 폐기 API")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "폐기 완료",
                    content = @Content(schema = @Schema(implementation = DeleteRefreshTokenResponse.class))),
            @ApiResponse(responseCode = "400", description = "폐기 실패 (Bad Request)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "폐기 실패 (Not Found) / ex: 리프레시 토큰이 틀렸거나 만료된 경우",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/accounts/{username}/tokens/{refreshToken}")
    public ResponseEntity<?> deleteRefreshToken(
            @Parameter(description = "유저 아이디", example = "njw1204") @PathVariable("username") String username,
            @Parameter(description = "리프레시 토큰",
                    example = "4Hs3UYPeGWvvSLWB3cYOZoWzuwZstsvqJdOoHCn1JyspPiiWxTVmS1hcBWKaQQat")
            @PathVariable("refreshToken") String refreshToken) {
        DeleteRefreshTokenAppRequest appRequest = new DeleteRefreshTokenAppRequest();
        appRequest.setUsername(username);
        appRequest.setRefreshToken(refreshToken);

        boolean appResponse = this.accountApplication.deleteRefreshToken(appRequest);

        if (!appResponse) {
            ErrorResponse errorResponse = new ErrorResponse();
            errorResponse.setErrors(List.of("fail to delete"));
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }

        DeleteRefreshTokenResponse response = new DeleteRefreshTokenResponse();
        response.setUsername(username);
        return ResponseEntity.ok(response);
    }
}
