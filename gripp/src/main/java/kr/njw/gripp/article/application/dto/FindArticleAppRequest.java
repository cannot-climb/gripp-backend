package kr.njw.gripp.article.application.dto;

import lombok.Data;

@Data
public class FindArticleAppRequest {
    private String usernameRequestedBy;
    private Long articleId;
}
