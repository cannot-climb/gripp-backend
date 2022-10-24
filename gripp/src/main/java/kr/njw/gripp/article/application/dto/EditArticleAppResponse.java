package kr.njw.gripp.article.application.dto;

import lombok.Data;

@Data
public class EditArticleAppResponse {
    private EditArticleAppResponseStatus status = EditArticleAppResponseStatus.SUCCESS;
}
