package kr.njw.gripp.user.application.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Optional;

@Data
public class FindUserAppResponse {
    private boolean success;
    private String username;
    private int tier;
    private int score;
    private long rank;
    private int percentile;
    private int articleCount;
    private int articleCertifiedCount;
    private LocalDateTime registerDateTime;

    public Optional<String> getUsername() {
        return Optional.ofNullable(this.username);
    }

    public Optional<LocalDateTime> getRegisterDateTime() {
        return Optional.ofNullable(this.registerDateTime);
    }
}
