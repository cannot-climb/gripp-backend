package kr.njw.gripp.article.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.*;

@Data
public class WriteArticleRequest {
    @Schema(description = "영상 아이디 (영상 업로드 API에서 받은 값)", example = "7dc53df5-703e-49b3-8670-b1c468f47f1f")
    @NotBlank(message = "must not be blank")
    private String videoId;

    @Schema(description = "제목 (200자 이하)", example = "Bell of the Wall")
    @NotBlank(message = "must not be blank")
    @Size(min = 1, max = 200, message = "size must be between 1 and 200")
    private String title;

    @Schema(description = "설명 (5000자 이하)", example = "Lorem ipsum dolor sit amet, consectetur adipiscing elit.")
    @NotBlank(message = "must not be blank")
    @Size(min = 1, max = 5000, message = "size must be between 1 and 5000")
    private String description;

    @Schema(description = "난이도 (0 이상 19 이하)", example = "3")
    @NotNull(message = "must not be null")
    @Min(value = 0, message = "must be greater than or equal to 0")
    @Max(value = 19, message = "must be less than or equal to 19")
    private Integer level;

    @Schema(description = "벽각도 (0 이상 90 이하)", example = "45")
    @NotNull(message = "must not be null")
    @Min(value = 0, message = "must be greater than or equal to 0")
    @Max(value = 90, message = "must be less than or equal to 90")
    private Integer angle;
}
