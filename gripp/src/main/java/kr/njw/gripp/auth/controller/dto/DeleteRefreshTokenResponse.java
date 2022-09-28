package kr.njw.gripp.auth.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class DeleteRefreshTokenResponse {
    @Schema(description = "유저 아이디", example = "njw1204")
    private String username;
}
