package kr.njw.gripp.auth.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class RefreshTokenRequest {
    @Schema(description = "리프레시 토큰", example = "4Hs3UYPeGWvvSLWB3cYOZoWzuwZstsvqJdOoHCn1JyspPiiWxTVmS1hcBWKaQQat")
    @NotBlank(message = "must not be blank")
    private String refreshToken;
}
