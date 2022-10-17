package kr.njw.gripp.article.application.dto;

import lombok.Data;

@Data
public class ReactArticleAppRequest {
    private String usernameRequestedBy;
    private Long articleId;
    private boolean favorite;
}
