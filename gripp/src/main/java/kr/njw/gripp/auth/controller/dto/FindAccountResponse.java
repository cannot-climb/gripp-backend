package kr.njw.gripp.auth.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class FindAccountResponse {
    @Schema(description = "조회 결과 (true: 있음, false: 없음)", example = "false")
    private boolean result;
}
