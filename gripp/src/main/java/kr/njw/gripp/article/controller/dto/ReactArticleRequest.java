package kr.njw.gripp.article.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class ReactArticleRequest {
    @Schema(description = "좋아요 여부 (true: 좋아요 등록, false: 좋아요 해제)", example = "true")
    @NotNull(message = "must not be null")
    private Boolean favorite;
}
