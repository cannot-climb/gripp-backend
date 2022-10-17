package kr.njw.gripp.article.application.dto;

import lombok.Data;

@Data
public class ReactArticleAppResponse {
    private ReactArticleAppResponseStatus status = ReactArticleAppResponseStatus.SUCCESS;
    private boolean favorite;
}
