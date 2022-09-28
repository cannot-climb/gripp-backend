package kr.njw.gripp.auth.application.dto;

import lombok.Data;

@Data
public class RefreshTokenAppRequest {
    private String username;
    private String refreshToken;
}
