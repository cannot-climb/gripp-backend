package kr.njw.gripp.article.application.dto;

import lombok.Data;

@Data
public class DeleteArticleAppRequest {
    private String username;
    private Long articleId;
}
