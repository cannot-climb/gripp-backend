package kr.njw.gripp.auth.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class SignUpResponse {
    @Schema(allowableValues = "SUCCESS")
    private final String code = "SUCCESS";
}
