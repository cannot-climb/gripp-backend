package kr.njw.gripp.article.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class EditArticleRequest {
    @Schema(description = "제목 (200자 이하)", example = "Bell of the Wall")
    @NotBlank(message = "must not be blank")
    @Size(min = 1, max = 200, message = "size must be between 1 and 200")
    private String title;

    @Schema(description = "설명 (5000자 이하)", example = "Lorem ipsum dolor sit amet, consectetur adipiscing elit.")
    @NotBlank(message = "must not be blank")
    @Size(min = 1, max = 5000, message = "size must be between 1 and 5000")
    private String description;
}
