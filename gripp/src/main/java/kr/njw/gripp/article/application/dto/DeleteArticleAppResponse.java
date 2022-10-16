package kr.njw.gripp.article.application.dto;

import lombok.Data;

@Data
public class DeleteArticleAppResponse {
    private DeleteArticleAppResponseStatus status = DeleteArticleAppResponseStatus.SUCCESS;
}
