package kr.njw.gripp.auth.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class RefreshTokenResponse {
    @Schema(description = "엑세스 토큰 (유효기간 약 30분 연장됨)",
            example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9" +
                    ".eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ" +
                    ".SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c")
    private String accessToken;
    @Schema(description = "리프레시 토큰 (유효기간은 연장되지 않음)",
            example = "4Hs3UYPeGWvvSLWB3cYOZoWzuwZstsvqJdOoHCn1JyspPiiWxTVmS1hcBWKaQQat")
    private String refreshToken;
}
