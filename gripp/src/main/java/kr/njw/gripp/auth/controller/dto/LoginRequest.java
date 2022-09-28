package kr.njw.gripp.auth.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
public class LoginRequest {
    @Schema(description = "유저 비밀번호", example = "pass1234")
    @NotEmpty(message = "must not be empty")
    private String password;
}
