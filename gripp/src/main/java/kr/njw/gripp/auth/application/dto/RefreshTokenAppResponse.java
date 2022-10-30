package kr.njw.gripp.auth.application.dto;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;

@Data
public class RefreshTokenAppResponse {
    private boolean success;
    private String accessToken;
    private String refreshToken;

    public Optional<String> getAccessToken() {
        return Optional.ofNullable(StringUtils.defaultIfBlank(this.accessToken, null));
    }

    public Optional<String> getRefreshToken() {
        return Optional.ofNullable(StringUtils.defaultIfBlank(this.refreshToken, null));
    }
}
