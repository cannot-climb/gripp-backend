package kr.njw.gripp.global.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class ErrorResponse {
    @ArraySchema(arraySchema = @Schema(description = "에러 메시지 목록",
            example = "[\"error 1\", \"error 2\" , \"error 3\"]"))
    private List<String> errors;
}
