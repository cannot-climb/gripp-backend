package kr.njw.gripp.article.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ReactArticleResponse {
    @Schema(description = "좋아요 여부", example = "true")
    private boolean favorite;
}
