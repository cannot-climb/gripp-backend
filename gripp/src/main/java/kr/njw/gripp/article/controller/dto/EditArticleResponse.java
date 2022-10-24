package kr.njw.gripp.article.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class EditArticleResponse {
    @Schema(description = "게시물 아이디", example = "42")
    private String articleId;
    @Schema(description = "제목", example = "Bell of the Wall")
    private String title;
    @Schema(description = "설명", example = "Lorem ipsum dolor sit amet, consectetur adipiscing elit.")
    private String description;
}
