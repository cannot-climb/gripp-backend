package kr.njw.gripp.article.application.dto;

import lombok.Data;

@Data
public class EditArticleAppRequest {
    private String username;
    private Long articleId;
    private String title;
    private String description;
}
