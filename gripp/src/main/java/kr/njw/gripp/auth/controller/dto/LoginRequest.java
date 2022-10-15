package kr.njw.gripp.auth.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class LoginRequest {
    @Schema(description = "유저 비밀번호", example = "pass1234")
    @NotBlank(message = "must not be blank")
    private String password;
}
