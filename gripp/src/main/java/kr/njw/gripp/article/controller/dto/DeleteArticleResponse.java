package kr.njw.gripp.article.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class DeleteArticleResponse {
    @Schema(description = "게시물 아이디", example = "42")
    private String articleId;
}
