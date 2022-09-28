package kr.njw.gripp.auth.application.dto;

import lombok.Data;

@Data
public class DeleteRefreshTokenAppRequest {
    private String username;
    private String refreshToken;
}
