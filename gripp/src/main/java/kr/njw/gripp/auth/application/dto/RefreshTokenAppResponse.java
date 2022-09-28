package kr.njw.gripp.auth.application.dto;

import lombok.Data;

import java.util.Optional;

@Data
public class RefreshTokenAppResponse {
    private boolean success;
    private String accessToken;
    private String refreshToken;

    public Optional<String> getAccessToken() {
        return Optional.ofNullable(this.accessToken);
    }

    public Optional<String> getRefreshToken() {
        return Optional.ofNullable(this.refreshToken);
    }
}
