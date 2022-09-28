package kr.njw.gripp.auth.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
public class RefreshTokenRequest {
    @Schema(description = "리프레시 토큰", example = "4Hs3UYPeGWvvSLWB3cYOZoWzuwZstsvqJdOoHCn1JyspPiiWxTVmS1hcBWKaQQat")
    @NotEmpty(message = "must not be empty")
    private String refreshToken;
}
